package kr.co.bnk.bnk_project.controller;

import jakarta.servlet.http.HttpSession;
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

    @PostMapping("/ask")
    public ResponseEntity<CsDTO> askChatbot(@RequestBody CsDTO request, HttpSession session) {

        String custNo = (String) session.getAttribute("custNo");

        String question = request.getQuestion();

        String answer = chatBotService.getAnswer(question, custNo);

        CsDTO response = CsDTO.builder()
                .answer(answer)
                .build();

        return ResponseEntity.ok(response);
    }
}