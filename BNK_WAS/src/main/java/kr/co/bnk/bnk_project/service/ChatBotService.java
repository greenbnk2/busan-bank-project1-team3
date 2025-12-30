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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatBotService {

    @Value("${groq.api.key:}")
    private String groqApiKey;

    private final WebClient webClient;
    private final CsRepository csRepository;
    private final FundRepository fundRepository;
    private final RiskTestResultRepository riskTestResultRepository;

    // Groq API 사용 (무료, 빠른 응답)
    private static final String GROQ_API_BASE_URL = "https://api.groq.com";
    private static final String GROQ_API_PATH = "/openai/v1/chat/completions";
    // 최신 지원 모델: llama-3.3-70b-versatile, llama-3.1-8b-instant, mixtral-8x7b-32768
    private static final String MODEL_NAME = "llama-3.3-70b-versatile"; // 최신 Llama 3.3 모델

    // 생성자 주입
    public ChatBotService(WebClient.Builder webClientBuilder,
                          CsRepository csRepository,
                          FundRepository fundRepository,
                          RiskTestResultRepository riskTestResultRepository) {
        this.webClient = webClientBuilder.baseUrl(GROQ_API_BASE_URL).build();
        this.csRepository = csRepository;
        this.fundRepository = fundRepository;
        this.riskTestResultRepository = riskTestResultRepository;
    }

    // userId 대신 custNo를 파라미터로 받습니다 (세션에서 넘어온 고객 번호)
    public String getAnswer(String userQuestion, String custNo) {
        // API Key 값 안전 처리
        final String cleanedApiKey = (groqApiKey != null && !groqApiKey.trim().isEmpty()) 
                ? groqApiKey.trim() 
                : "";

        if (cleanedApiKey.isEmpty()) {
            return "죄송합니다. 챗봇 서비스 설정에 문제가 있습니다. 관리자에게 문의해주세요.";
        }

        // =================================================================================
        // 1. 지식 수집 단계 (DB 조회) - RAG 방식
        // 질문 유형에 따라 필요한 정보만 조회 (최적화)
        // =================================================================================

        // 사용자 질문 분석 (펀드 추천 관련 키워드 체크)
        String lowerQuestion = userQuestion.toLowerCase();
        boolean isFundRelated = lowerQuestion.contains("펀드") || lowerQuestion.contains("추천") 
                || lowerQuestion.contains("투자") || lowerQuestion.contains("상품") 
                || lowerQuestion.contains("종목") || lowerQuestion.contains("리스트");
        boolean isFaqRelated = lowerQuestion.contains("질문") || lowerQuestion.contains("faq") 
                || lowerQuestion.contains("자주") || lowerQuestion.contains("문의") 
                || lowerQuestion.contains("이용") || lowerQuestion.contains("가입") 
                || lowerQuestion.contains("해지") || lowerQuestion.contains("계좌");

        // [지식 1] FAQ 데이터 조회 (FAQ 관련 질문일 때만)
        String faqString = "관련 FAQ 정보가 없습니다.";
        if (isFaqRelated) { // FAQ 관련 질문일 때만 조회
            List<Cs> faqList = csRepository.findAllByCategoryIdAndAnswerIsNotNull(8L);
            if (!faqList.isEmpty()) {
                faqString = faqList.stream()
                        .map(cs -> String.format("Q: %s\nA: %s", cs.getQuestion(), cs.getAnswer()))
                        .collect(Collectors.joining("\n\n"));
            }
        }

        // [지식 2] 유저 투자 성향 및 추천 펀드 조회 (펀드 관련 질문일 때만)
        String userRiskProfile = "정보 없음 (비로그인 또는 검사 이력 없음)";
        String recommendedFundsString = "추천할 펀드가 없습니다. (투자 성향 정보 부재)";

        if (isFundRelated && custNo != null && !custNo.isEmpty()) {
            // 가장 최근 투자 성향 결과 조회 (custNo 사용)
            Optional<RiskTestResult> riskResult = riskTestResultRepository.findTopByCustNoOrderByTestDateDesc(custNo);

            if (riskResult.isPresent()) {
                String riskType = riskResult.get().getRiskType(); // 예: "공격투자형"
                userRiskProfile = riskType;

                // 성향에 맞는 펀드 조회
                List<FundMaster> funds = fundRepository.findAllByInvestGrade(riskType);

                if (!funds.isEmpty()) {
                    // DB 데이터를 AI가 읽기 편한 텍스트 형태로 변환
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
        } else if (isFundRelated && (custNo == null || custNo.isEmpty())) {
            // 펀드 관련 질문이지만 로그인하지 않은 경우
            recommendedFundsString = "펀드 추천을 받으시려면 로그인 후 투자 성향 테스트를 진행해주세요.";
        }

        // =================================================================================
        // 2. 프롬프트 구성 단계 (최적화된 시스템 프롬프트)
        // =================================================================================

        String systemPrompt = String.format("""
                당신은 BNK금융그룹의 펀드 투자 상담 전문가 '증권봇'입니다. 고객과 친근하게 대화하면서 정확한 정보를 제공하는 것이 목표입니다.
                
                ## 고객 정보
                - 고객 번호: %s
                - 투자 성향: %s
                
                ## 참고 지식
                
                ### 추천 펀드 상품
                %s
                
                ### 자주 묻는 질문 (FAQ)
                %s
                
                ## 답변 가이드
                
                1. **톤 & 매너**
                   - 항상 존댓말을 사용하며, 친근하고 다정한 말투로 답변하세요
                   - 딱딱한 금융 용어보다는 일반인이 이해하기 쉬운 표현을 사용하세요
                   - 적절한 이모티콘 사용 가능 (예: 😊, 💡, ⭐)
                   
                2. **펀드 상담 시**
                   - 고객의 투자 성향을 먼저 언급하며 맞춤 추천하세요
                   - 예: "고객님은 %s이시군요! 이런 상품들이 적합할 것 같아요 😊"
                   - 각 펀드의 특징을 고객 성향과 연결하여 설명하세요
                   - 투자 성향 정보가 없으면 테스트를 권유하되, 부담스럽지 않게 안내하세요
                   
                3. **FAQ 활용**
                   - FAQ에 있는 내용은 자연스럽게 재구성하여 답변하세요
                   - 단순 복사-붙여넣기가 아닌 대화 형식으로 풀어서 설명하세요
                   
                4. **일상 대화**
                   - 인사, 감사 인사, 날씨 등 일상적 대화에는 친근하게 응답하세요
                   - 펀드와 관련 없는 주제라도 정중하게 답변하되, 가능하면 펀드로 연결하세요
                   
                5. **모르는 내용**
                   - 확실하지 않은 정보는 추측하지 말고 정중히 안내하세요
                   - 예: "그 부분은 가까운 영업점이나 고객센터(1234-5678)에서 자세히 안내받으실 수 있어요 😊"
                   
                6. **기타**
                   - 답변은 2~3문단 정도로 간결하게 작성하세요
                   - 너무 길거나 복잡한 답변은 피하세요
                   - 고객이 이해하기 쉽게 핵심만 전달하세요
                """, 
                custNo != null && !custNo.isEmpty() ? custNo : "미로그인", 
                userRiskProfile,
                recommendedFundsString,
                faqString,
                userRiskProfile.contains("정보 없음") ? "투자 성향을 아직 테스트하지 않으신" : userRiskProfile);

        // =================================================================================
        // 3. Groq API 호출 (OpenAI 호환 형식)
        // =================================================================================

        // Messages 배열 구성
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // System message
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);
        
        // User message
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userQuestion);
        messages.add(userMessage);

        // Request body 구성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL_NAME);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7); // 창의성 조절 (0.0 ~ 1.0)
        requestBody.put("max_tokens", 1000); // 최대 토큰 수

        try {
            Map response = webClient.post()
                    .uri(GROQ_API_PATH)
                    .header("Authorization", "Bearer " + cleanedApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        return clientResponse.createException();
                    })
                    .bodyToMono(Map.class)
                    .block();

            return parseGroqResponse(response);

        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            String errorMessage = String.format("Groq API 호출 오류 [상태: %d]: %s", e.getStatusCode().value(), errorBody);
            System.err.println(errorMessage);
            
            if (e.getStatusCode().value() == 401) {
                return "죄송합니다. 챗봇 인증에 문제가 발생했습니다. API 키를 확인해주세요.";
            } else if (e.getStatusCode().value() == 403) {
                // 403 에러는 API 키 권한 문제 또는 네트워크 접근 제한
                System.err.println("Groq API 403 에러 - API 키 권한 문제 또는 네트워크 접근 제한 가능");
                System.err.println("API 키 확인 필요: application.yml의 groq.api.key 설정을 확인하세요.");
                return "죄송합니다. 챗봇 접근 권한에 문제가 발생했습니다. 관리자에게 문의해주세요.";
            } else if (e.getStatusCode().value() == 429) {
                return "죄송합니다. 현재 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.";
            } else if (e.getStatusCode().value() == 400) {
                // 400 에러는 모델 이름이나 요청 형식 문제일 수 있음
                System.err.println("Groq API 400 에러 상세: " + errorBody);
                return "죄송합니다. 챗봇 요청 처리에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
            }
            return "죄송합니다. 챗봇 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
        } catch (Exception e) {
            System.err.println("챗봇 서비스 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return "죄송합니다. 일시적인 시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    private String parseGroqResponse(Map response) {
        try {
            if (response == null) {
                System.err.println("Groq API 응답이 null입니다.");
                return "죄송합니다. 챗봇 응답을 받지 못했습니다. 잠시 후 다시 시도해주세요.";
            }
            
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                System.err.println("Groq API 응답에 choices가 없습니다. 응답: " + response);
                return "죄송합니다. 답변을 생성할 수 없습니다. 잠시 후 다시 시도해주세요.";
            }

            Map<String, Object> firstChoice = choices.get(0);
            if (firstChoice == null) {
                System.err.println("Groq API 응답의 첫 번째 choice가 null입니다.");
                return "죄송합니다. 답변을 생성할 수 없습니다. 잠시 후 다시 시도해주세요.";
            }
            
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            if (message == null) {
                System.err.println("Groq API 응답의 message가 null입니다. choice: " + firstChoice);
                return "죄송합니다. 답변을 생성할 수 없습니다. 잠시 후 다시 시도해주세요.";
            }
            
            String content = (String) message.get("content");
            if (content == null || content.trim().isEmpty()) {
                System.err.println("Groq API 응답의 content가 null이거나 비어있습니다. message: " + message);
                return "죄송합니다. 답변 내용이 비어있습니다. 잠시 후 다시 시도해주세요.";
            }
            
            return content.trim();
        } catch (Exception e) {
            System.err.println("Groq API 응답 파싱 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return "죄송합니다. 응답 처리 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }
}