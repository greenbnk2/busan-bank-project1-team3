package kr.co.bnk.bnk_project.controller;

import kr.co.bnk.bnk_project.dto.BnkUserDTO;
import kr.co.bnk.bnk_project.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/login")
    public String login(){
        return "member/login";
    }

    @GetMapping("/type")
    public String registerType(){
        return "member/registerType";
    }

    @GetMapping("/terms")
    public String terms(){
        return "member/terms";
    }

    @GetMapping("/register")
    public String register(){
        return "member/register";
    }

    // 회원가입 처리
    @PostMapping("/register")
    public String register(@ModelAttribute BnkUserDTO dto,
                           RedirectAttributes redirectAttributes) {
        try {
            // 회원가입 처리
            memberService.registerUser(dto);

            // 완료 페이지로 이동 (이름, 아이디 전달)
            redirectAttributes.addAttribute("name", dto.getName());
            redirectAttributes.addAttribute("userId", dto.getUserId());

            return "redirect:/member/complete";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/member/register";
        }
    }

    // 회원가입 완료 페이지
    @GetMapping("/complete")
    public String complete(@RequestParam String name,
                           @RequestParam String userId,
                           Model model) {
        model.addAttribute("name", name);
        model.addAttribute("userId", userId);
        return "member/complete";
    }
}
