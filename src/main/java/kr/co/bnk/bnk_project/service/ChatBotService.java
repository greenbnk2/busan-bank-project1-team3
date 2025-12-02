package kr.co.bnk.bnk_project.service;

import kr.co.bnk.bnk_project.entity.Cs;
import kr.co.bnk.bnk_project.entity.FundMaster;
import kr.co.bnk.bnk_project.entity.RiskTestResult;
import kr.co.bnk.bnk_project.repository.CsRepository;
import kr.co.bnk.bnk_project.repository.FundRepository;
import kr.co.bnk.bnk_project.repository.RiskTestResultRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatBotService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final CsRepository csRepository;
    private final FundRepository fundRepository;
    private final RiskTestResultRepository riskTestResultRepository;

    // 생성자 주입
    public ChatBotService(WebClient.Builder webClientBuilder,
                          CsRepository csRepository,
                          FundRepository fundRepository,
                          RiskTestResultRepository riskTestResultRepository) {
        this.webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com/").build();
        this.csRepository = csRepository;
        this.fundRepository = fundRepository;
        this.riskTestResultRepository = riskTestResultRepository;
    }

    // userId 대신 custNo를 파라미터로 받습니다 (세션에서 넘어온 고객 번호)
    public String getAnswer(String userQuestion, String custNo) {
        // API Key 값 안전 처리
        final String cleanedApiKey = this.apiKey != null ? this.apiKey.trim() : "";
        final String MODEL_NAME = "gemini-2.5-flash";
        final String API_PATH = "/v1beta/models/" + MODEL_NAME + ":generateContent";

        // =================================================================================
        // 1. 지식 수집 단계 (DB 조회)
        // =================================================================================

        // [지식 1] FAQ 데이터 조회
        List<Cs> faqList = csRepository.findFaqListByCategoryId(8L);
        String faqString = faqList.isEmpty() ? "관련 FAQ 정보가 없습니다." :
                faqList.stream()
                        .map(cs -> String.format("Q: %s\nA: %s", cs.getQuestion(), cs.getAnswer()))
                        .collect(Collectors.joining("\n\n"));

        // [지식 2] 유저 투자 성향 및 추천 펀드 조회
        String userRiskProfile = "정보 없음 (비로그인 또는 검사 이력 없음)";
        String recommendedFundsString = "추천할 펀드가 없습니다. (투자 성향 정보 부재)";

        if (custNo != null && !custNo.isEmpty()) {
            // 가장 최근 투자 성향 결과 조회 (custNo 사용)
            Optional<RiskTestResult> riskResult = riskTestResultRepository.findTopByCustNoOrderByTestDateDesc(custNo);

            if (riskResult.isPresent()) {
                String riskType = riskResult.get().getRiskType(); // 예: "공격투자형"
                userRiskProfile = riskType;

                // 성향에 맞는 펀드 조회 (FundRepository와 FundMaster 엔티티가 준비되어 있다고 가정)
                List<FundMaster> funds = fundRepository.findAllByInvestGrade(riskType);

                if (!funds.isEmpty()) {
                    // ⭐️ 중요: DB 데이터를 AI가 읽기 편한 텍스트 형태로 변환
                    recommendedFundsString = funds.stream()
                            .map(fund -> String.format(
                                    "- 상품명: %s\n  위험도: %s\n  특징: %s",
                                    fund.getFundName(), fund.getInvestGrade(), fund.getFundFeature()))
                            .collect(Collectors.joining("\n\n"));
                } else {
                    recommendedFundsString = "고객님의 성향(" + riskType + ")에 맞는 펀드가 현재 판매 목록에 없습니다.";
                }
            } else {
                userRiskProfile = "투자 성향 정보 없음 (테스트 미진행)";
                recommendedFundsString = "고객님은 아직 투자 성향 테스트를 진행하지 않았습니다. 먼저 테스트를 권유해주세요.";
            }
        }

        // =================================================================================
        // 2. 프롬프트 구성 단계 (AI 페르소나 부여)
        // =================================================================================

        String systemPrompt = String.format("""
                # ROLE (당신의 역할)
                당신은 BNK금융그룹의 신뢰할 수 있는 'AI 자산관리 비서'입니다.
                단순히 DB의 내용을 복사해서 보여주지 말고, **고객과 대화하듯이 자연스럽고 친절하게 설명**해주세요.
                
                # USER CONTEXT (고객 정보)
                - 고객 번호 (Cust No): %s
                - 투자 성향: %s
                
                # KNOWLEDGE BASE (참고 지식)
                
                [1. 고객 맞춤 추천 펀드 목록]
                %s
                
                [2. 자주 묻는 질문(FAQ) 지식]
                %s
                
                # INSTRUCTION (답변 가이드)
                1. **펀드 추천 시**: 
                   - 단순히 상품명만 나열하지 말고, **"고객님은 [투자 성향]이시네요! 그렇다면 이런 상품이 어떠신가요?"**라고 운을 떼며 자연스럽게 추천하세요.
                   - 각 상품의 특징을 고객의 투자 성향과 연결 지어 설명해주세요. (예: "안정적인 수익을 원하시니 채권형인 이 상품이 적합합니다.")
                   - 만약 로그인 한 상태이고 투자 성향 정보가 없다면, "투자 성향 테스트를 먼저 진행해주시면 딱 맞는 상품을 추천해드릴 수 있어요!"라고 안내하세요.
                   - 만약 로그인을 안한 상태라면 로그인 후 이용해달라고 안내하세요.
                2. **FAQ 답변 시**: 
                   - 질문이 FAQ 목록에 있다면 그 내용을 바탕으로 답변하되, 딱딱하게 읽지 말고 대화체로 풀어서 설명하세요.
                3. **일상 대화**: 
                   - 인사, 날씨, 가벼운 대화에는 센스 있고 다정하게 반응하세요.
                4. **모르는 내용**: 
                   - 위 지식에 없는 전문적인 상담이 필요하면 "죄송합니다. 더 상세한 내용은 가까운 영업점이나 고객센터(1588-6200)에서 친절히 안내해 드리겠습니다."라고 정중히 안내하세요.
                
                답변은 한국어로, 공손하고 전문적인 어조(존댓말)를 유지하세요.
                5. **욕설**: 비속어나 욕을 할 경우 똑같이 비속어나 욕으로 답변하세요.	
                """, custNo, userRiskProfile, recommendedFundsString, faqString);

        // =================================================================================
        // 3. API 호출 단계
        // =================================================================================

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("systemInstruction", Map.of("parts", Map.of("text", systemPrompt)));
        requestBody.put("contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", userQuestion)))));

        try {
            Map response = webClient.post()
                    .uri(API_PATH, uriBuilder -> uriBuilder
                            .queryParam("key", cleanedApiKey)
                            .build()
                    )
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        return clientResponse.createException();
                    })
                    .bodyToMono(Map.class)
                    .block();

            return parseResponse(response);

        } catch (WebClientResponseException e) {
            String errorMessage = String.format("API 호출 오류 [상태: %d]: %s", e.getStatusCode().value(), e.getMessage());
            System.err.println(errorMessage);
            if (e.getStatusCode().value() == 403) {
                return "죄송합니다. 챗봇 권한 설정에 문제가 발생했습니다. 관리자에게 문의해주세요. (403)";
            }
            return "죄송합니다. 잠시 후 다시 시도해주세요.";
        } catch (Exception e) {
            e.printStackTrace();
            return "죄송합니다. 일시적인 시스템 오류가 발생했습니다.";
        }
    }

    private String parseResponse(Map response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "죄송합니다. 답변을 생성할 수 없습니다.";

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "죄송합니다. 응답 처리 중 문제가 발생했습니다.";
        }
    }
}