package kr.co.bnk.bnk_project.controller;

import jakarta.validation.Valid;
import kr.co.bnk.bnk_project.dto.BnkUserDTO;
import kr.co.bnk.bnk_project.dto.UserTermsDTO;
import kr.co.bnk.bnk_project.service.EmailService;
import kr.co.bnk.bnk_project.service.MemberService;
import kr.co.bnk.bnk_project.service.UserTermsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final UserTermsService userTermsService;
    private final EmailService emailService;

    @GetMapping("/login")
    public String login(){
        return "member/login";
    }

    @GetMapping("/type")
    public String registerType(){
        return "member/registerType";
    }

    @GetMapping("/terms")
    public String terms(Model model){

        UserTermsDTO memberTerms = userTermsService.getTerm("TERM001");
        if (memberTerms == null) {
            memberTerms = new UserTermsDTO();
            memberTerms.setTermId("TERM001");
            memberTerms.setTitle("회원약관");
            memberTerms.setContent("");
        }

        UserTermsDTO privacyTerms = userTermsService.getTerm("TERM002");
        if (privacyTerms == null) {
            privacyTerms = new UserTermsDTO();
            privacyTerms.setTermId("TERM002");
            privacyTerms.setTitle("개인정보처리위탁방침");
            privacyTerms.setContent("");
        }

        model.addAttribute("memberTerms", memberTerms);
        model.addAttribute("privacyTerms", privacyTerms);

        return "member/terms";
    }

    @GetMapping("/register")
    public String register(Model model){

        BnkUserDTO dto = new BnkUserDTO();

        // 서비스에서 랜덤 계좌번호 가져오기
        String randomAccount = memberService.generateAccountNum();

        // DTO에 계좌번호 세팅
        dto.setAccountNumber(randomAccount);

        // 모델에 담아서 뷰로 전달
        model.addAttribute("dto", dto);

        return "member/register";
    }

    // 회원가입 처리
    @PostMapping("/register")
    public String register(@ModelAttribute @Valid BnkUserDTO dto,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {

        // 검증 실패 시 폼으로 다시 보냄
        if (bindingResult.hasErrors()) {
            log.warn("회원가입 폼 검증 오류 발생: {}", bindingResult.getAllErrors());
            // DTO와 오류 정보를 Model에 담아 뷰 템플릿을 반환. (dto와 bindingResult는 자동으로 Model에 추가)
            return "member/register";
        }

        // 검증 성공 시
        try {
            // 회원가입 처리
            memberService.registerUser(dto);

            // 완료 페이지로 이동 (이름, 아이디 전달)
            redirectAttributes.addAttribute("name", dto.getName());
            redirectAttributes.addAttribute("userId", dto.getUserId());

            return "redirect:/member/complete";

        } catch (Exception e) {
            log.error("회원가입 진행 중 에러 발생: ", e);
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

    // 아이디 중복 확인
    @PostMapping("/api/check-userid")
    @ResponseBody // 뷰(html)가 아닌 JSON/Text 데이터를 반환
    public Map<String, Object> checkUserId(@RequestBody Map<String, String> payload) {
        // 1. 클라이언트가 보낸 userId 추출
        String userId = payload.get("userId");

        // 2. MemberService에 아이디 중복 확인 요청
        boolean isAvailable = memberService.isUserIdAvailable(userId);

        // 3. 응답 결과를 Map(JSON)으로 구성하여 반환
        Map<String, Object> response = new HashMap<>();
        response.put("available", isAvailable);
        response.put("message", isAvailable ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다.");

        return response;
    }

    // 이메일 인증 번호 발송
    @PostMapping("/api/send-email-code")
    @ResponseBody
    public Map<String, Object> sendEmailCode(@RequestBody Map<String, String> payload) {
        String email = payload.get("email"); // JS가 합쳐서 보낸 이메일
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isDuplicate = emailService.sendVerificationCode(email);

            if (isDuplicate) {
                response.put("status", "fail");
                response.put("message", "이미 가입된 이메일입니다.");
            } else {
                response.put("status", "success");
                response.put("message", "인증코드가 발송되었습니다.");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "발송 중 오류가 발생했습니다.");
        }

        return response;
    }

    // 이메일 인증 번호 확인
    @PostMapping("/api/verify-email-code")
    @ResponseBody
    public Map<String, Object> verifyEmailCode(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");
        boolean isVerified = emailService.verifyCode(code);

        Map<String, Object> response = new HashMap<>();
        if (isVerified) {
            response.put("status", "success");
        } else {
            response.put("status", "fail");
            response.put("message", "인증번호가 일치하지 않습니다.");
        }
        return response;
    }

    // 투자성향분석 설문 페이지
    @GetMapping("/survey")
    public String survey(Model model) {

        return "member/survey";
    }

    // 투자성향분석 결과 페이지 (임시)
    @PostMapping("/survey-result")
    public String surveyResult(Model model) {

        // 지금은 무조건 '적극투자형'으로 결과를 보여주도록 설정
        model.addAttribute("riskType", "적극투자형");
        model.addAttribute("score", "82");
        return "member/survey_result";
    }

}
