package kr.co.bnk.bnk_project.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.Map;
import java.util.List;

@Service
public class GeminiService {
    @Value("${spring.ai.google.ai.api-key}")
    private String apiKey;

    private final WebClient webClient;

    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String getAnalysis(String prompt) {
        if (apiKey == null || apiKey.isEmpty()) return "API 키 설정 오류";

        // 사용 모델명
        String modelId = "gemini-flash-latest";

        // v1beta 경로와 모델명을 결합합니다.
        String urlString = "https://generativelanguage.googleapis.com/v1beta/models/"
                + modelId + ":generateContent?key=" + apiKey;

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        try {
            return this.webClient.post()
                    .uri(java.net.URI.create(urlString)) // 인코딩 방지
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        try {
                            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                            List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");
                            return parts.get(0).get("text");
                        } catch (Exception e) {
                            return "응답 구조 분석 실패: " + response.toString();
                        }
                    })
                    .block();
        } catch (Exception e) {
            return "API 호출 실패: " + e.getMessage();
        }
    }

    public void checkAvailableModels() {
        String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
        try {
            String response = this.webClient.get()
                    .uri(java.net.URI.create(url))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            System.out.println("사용 가능한 모델 리스트: " + response);
        } catch (Exception e) {
            System.err.println("모델 목록 확인 실패: " + e.getMessage());
        }
    }
}