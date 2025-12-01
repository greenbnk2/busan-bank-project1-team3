package kr.co.bnk.bnk_project.service;

import kr.co.bnk.bnk_project.entity.Cs;
import kr.co.bnk.bnk_project.repository.CsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatBotService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final CsRepository csRepository;

    public ChatBotService(WebClient.Builder webClientBuilder, CsRepository csRepository) {
        // Base URL을 도메인까지만 설정
        this.webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com/").build();
        this.csRepository = csRepository;
    }

    public String getAnswer(String userQuestion) {
        // API Key 값에 혹시 모를 공백 제거 (안전 장치)
        final String cleanedApiKey = this.apiKey != null ? this.apiKey.trim() : "";
        final String MODEL_NAME = "gemini-2.5-flash";
        final String API_PATH = "/v1beta/models/" + MODEL_NAME + ":generateContent";

        // 1. DB에서 카테고리가 8번(FAQ)인 데이터만 조회
        List<Cs> faqList = csRepository.findFaqListByCategoryId(8L);

        // 2. 조회된 CS 데이터를 프롬프트 문자열로 변환
        String faqString = faqList.stream()
                .map(cs -> String.format("Q: %s\nA: %s", cs.getQuestion(), cs.getAnswer()))
                .collect(Collectors.joining("\n\n"));

        if (faqList.isEmpty()) {
            faqString = "현재 등록된 FAQ가 없습니다.";
        }

        // 3. 시스템 프롬프트 구성 (규칙을 강화하고 명확하게 수정)
        String systemPrompt = String.format("""
                # ROLE
                당신은 BNK 프로젝트의 전문 AI 챗봇입니다. 당신의 유일한 임무는 아래 [FAQ 목록]에 기반하여 답변하는 것입니다.
                
                # INSTRUCTION
                1. 답변은 반드시 [FAQ 목록]에 있는 내용만을 사용해야 합니다.
                2. 사용자 질문과 **가장 유사한** FAQ의 답변을 찾아 친절하고 간결하게 제공하십시오.
                3. 만약 사용자 질문이 [FAQ 목록]의 어떤 질문과도 **연관성이 없거나** 유사하지 않다면, 오직 다음 문장만을 사용해 답변해야 합니다: '죄송합니다. 해당 내용은 고객센터로 직접 문의 부탁드립니다.'
                
                # [FAQ 목록] (반드시 이 목록 내에서만 답변을 생성해야 함)
                %s
                """, faqString);

        // 4. 요청 본문 구성
        Map<String, Object> requestBody = new HashMap<>();

        Map<String, Object> systemInstruction = Map.of(
                "parts", Map.of("text", systemPrompt)
        );
        requestBody.put("systemInstruction", systemInstruction);

        Map<String, Object> userContent = Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", userQuestion))
        );
        requestBody.put("contents", List.of(userContent));

        // 5. API 호출
        try {
            // ⭐️ 디버깅: 생성된 전체 시스템 프롬프트를 콘솔에 출력하여 DB 내용 확인
            System.out.println("DEBUG: 생성된 시스템 프롬프트 (FAQ 포함):\n" + systemPrompt);

            // ⭐️ 디버깅: 호출될 Full URL 다시 확인
            String fullUri = UriComponentsBuilder.fromUriString(API_PATH)
                    .queryParam("key", cleanedApiKey)
                    .build()
                    .toUriString();
            System.out.println("DEBUG: 호출될 Full URL: https://generativelanguage.googleapis.com" + fullUri);

            Map response = webClient.post()
                    .uri(API_PATH, uriBuilder -> uriBuilder
                            .queryParam("key", cleanedApiKey)
                            .build()
                    )
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return parseResponse(response);

        } catch (Exception e) {
            e.printStackTrace();
            return "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    private String parseResponse(Map response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "답변을 생성하지 못했습니다.";

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "응답 처리 중 오류가 발생했습니다. (API 응답 구조 확인 필요)";
        }
    }
}