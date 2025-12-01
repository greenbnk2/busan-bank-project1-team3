package kr.co.bnk.bnk_project.controller;


import kr.co.bnk.bnk_project.dto.CsDTO;
import kr.co.bnk.bnk_project.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatController {

    private final ChatBotService chatBotService;

    // 클라이언트가 보낸 질문을 받아 처리하는 엔드포인트
    @PostMapping("/ask")
    public ResponseEntity<CsDTO> askChatbot(@RequestBody CsDTO request) {

        // DTO에서 질문 내용을 꺼내서 서비스로 전달
        String question = request.getQuestion();

        // GeminiService를 호출하여 DB FAQ 기반의 답변을 받아옴
        String answer = chatBotService.getAnswer(question);

        // 답변을 다시 DTO에 담아 클라이언트에 반환
        // (CsDTO의 다른 필드들은 챗봇 기능에서 사용하지 않으므로 answer 필드만 채워서 반환)
        CsDTO response = CsDTO.builder()
                .answer(answer)
                .build();

        return ResponseEntity.ok(response);
    }
}