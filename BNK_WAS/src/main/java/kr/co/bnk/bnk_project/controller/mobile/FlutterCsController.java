package kr.co.bnk.bnk_project.controller.mobile;

import kr.co.bnk.bnk_project.dto.CsDTO;
import kr.co.bnk.bnk_project.service.CsService;
import kr.co.bnk.bnk_project.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Flutter 앱 전용 고객센터 API 컨트롤러
 * Flutter API 요청 처리
 */
@RestController
@RequiredArgsConstructor
public class FlutterCsController {

    private final CsService csService;
    private final ChatBotService chatBotService;

    /**
     * FAQ 전체 목록 조회
     * Flutter API: FaqApi.fetchFaqs()
     * GET /faq
     */
    @GetMapping("/faq")
    public ResponseEntity<List<Map<String, String>>> getFaqs() {
        // 페이징 없이 전체 FAQ 조회
        kr.co.bnk.bnk_project.dto.PageRequestDTO pageRequestDTO = new kr.co.bnk.bnk_project.dto.PageRequestDTO();
        pageRequestDTO.setSize(1000); // 충분히 큰 값으로 전체 조회
        pageRequestDTO.setPg(1);
        pageRequestDTO.setCate("faq");

        var pageResponse = csService.getFaqPage(pageRequestDTO);
        List<CsDTO> faqList = pageResponse.getDtoList();

        // Flutter 형식으로 변환 (question, answer만)
        List<Map<String, String>> result = faqList.stream()
                .map(faq -> {
                    Map<String, String> item = new HashMap<>();
                    item.put("question", faq.getQuestion());
                    item.put("answer", faq.getAnswer() != null ? faq.getAnswer() : "");
                    return item;
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    /**
     * 내 문의 내역 조회
     * Flutter API: InquiryApi.fetchMyInquiries()
     * GET /inquiry/my?userId=xxx (또는 JWT 토큰에서 userId 추출)
     * TODO: JWT 토큰에서 userId 자동 추출 구현 필요
     */
    @GetMapping("/inquiry/my")
    public ResponseEntity<List<Map<String, Object>>> getMyInquiries(@RequestParam(required = false) String userId) {
        // userId가 없으면 빈 리스트 반환 (또는 에러 처리)
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        // Flutter용 메서드 사용 (CATEGORY JOIN 포함)
        List<CsDTO> inquiryList = csService.getMyInquiries(userId);

        // Flutter 형식으로 변환
        List<Map<String, Object>> result = inquiryList.stream()
                .map(inquiry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("csId", inquiry.getCsId());
                    item.put("categoryId", inquiry.getCategoryId());
                    item.put("categoryName", inquiry.getCategoryName() != null ? inquiry.getCategoryName() : "");
                    item.put("title", inquiry.getTitle());
                    item.put("question", inquiry.getQuestion());
                    item.put("answer", inquiry.getAnswer());
                    item.put("status", inquiry.getStatus() != null ? inquiry.getStatus() : "답변대기");
                    item.put("userId", inquiry.getUserId());
                    item.put("createdAt", inquiry.getCreatedAt());
                    item.put("answeredAt", inquiry.getAnsweredAt());
                    return item;
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    /**
     * 문의 등록
     * Flutter API: InquiryApi.submitInquiry()
     * POST /inquiry
     * Body: { "categoryId": 1, "title": "...", "question": "..." }
     * JWT 토큰에서 userId 추출 필요 (현재는 요청 body에 포함)
     */
    @PostMapping("/inquiry")
    public ResponseEntity<Map<String, Object>> createInquiry(@RequestBody Map<String, Object> request, 
                                                              @RequestParam(required = false) String userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            CsDTO csDTO = new CsDTO();
            
            // categoryId는 숫자로 변환 필요
            Object categoryIdObj = request.get("categoryId");
            if (categoryIdObj instanceof Integer) {
                csDTO.setCategoryId(((Integer) categoryIdObj).longValue());
            } else if (categoryIdObj instanceof Number) {
                csDTO.setCategoryId(((Number) categoryIdObj).longValue());
            }

            csDTO.setTitle((String) request.get("title"));
            csDTO.setQuestion((String) request.get("question"));
            
            // userId는 JWT에서 추출해야 하지만, 현재는 요청에서 받음
            if (userId == null) {
                userId = (String) request.get("userId");
            }
            csDTO.setUserId(userId);

            // Flutter용 메서드 사용 (동적 CATEGORY_ID)
            csService.registerInquiryFlutter(csDTO);

            response.put("success", true);
            response.put("message", "문의가 접수되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "문의 접수에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 챗봇 메시지 전송 및 응답 받기
     * Flutter API: ChatbotApi.sendMessage()
     * POST /chatbot/message
     * Body: { "message": "...", "conversationId": "..." (선택) }
     * custNo는 JWT에서 추출 필요 (현재는 쿼리 파라미터로 받음)
     */
    @PostMapping("/chatbot/message")
    public ResponseEntity<Map<String, String>> sendChatbotMessage(@RequestBody Map<String, String> request,
                                                                   @RequestParam(required = false) String custNo) {
        Map<String, String> response = new HashMap<>();

        try {
            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                response.put("reply", "메시지를 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            // custNo가 없으면 빈 문자열로 처리 (비로그인 사용자)
            String reply = chatBotService.getAnswer(message, custNo != null ? custNo : "");

            response.put("reply", reply);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("챗봇 컨트롤러 예외 발생: " + e.getMessage());
            e.printStackTrace();
            response.put("reply", "죄송합니다. 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return ResponseEntity.status(500).body(response);
        }
    }
}

