package kr.co.bnk.bnk_project.controller;

import jakarta.validation.Valid;
import kr.co.bnk.bnk_project.dto.MemberUpdateDTO;
import kr.co.bnk.bnk_project.security.MyUserDetails;
import kr.co.bnk.bnk_project.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/my")
public class MyController {

    private final MemberService memberService;

    @GetMapping("/dashboard")
    public String myDashboard() {
        // templates/my/dashboard.html 파일을 렌더링합니다.
        return "my/dashboard";
    }

    // 회원정보 수정 화면
    @GetMapping("/info")
    public String myInfoForm(@AuthenticationPrincipal MyUserDetails userDetails, Model model) {

        // 세션에서 PK 추출
        Long custNo = userDetails.getUserDTO().getCustNo();

        // DB에서 최신 정보 조회
        MemberUpdateDTO dto = memberService.getMemberInfo(custNo);

        model.addAttribute("dto", dto);
        return "my/info";
    }

    // 회원정보 수정
    @PostMapping("/info")
    public String myInfoUpdate(@AuthenticationPrincipal MyUserDetails userDetails,
                               @Valid @ModelAttribute("dto") MemberUpdateDTO dto,
                               BindingResult bindingResult,
                               Model model) {

        // 유효성 검사 실패 시 다시 폼으로
        if (bindingResult.hasErrors()) {
            return "my/info";
        }

        // 세션의 PK를 DTO에 강제 주입 (파라미터 변조 방지)
        dto.setCustNo(userDetails.getUserDTO().getCustNo());

        // 업데이트 실행
        memberService.updateMemberInfo(dto);

        // 완료 후 대시보드로 이동
        return "redirect:/my/dashboard";
    }

    @GetMapping("/account")
    public String myAccountInquiry() {
        return "my/fundAccountInquiry";
    }

    @GetMapping("/price")
    public String myPriceInquiry() {
        return "my/basicPriceInquiry";
    }

    @GetMapping("/yield")
    public String myYieldInquiry() {
        return "my/yieldInquiry";
    }

    @GetMapping("/report")
    public String myReportChange() {
        return "my/reportChange";
    }

    @GetMapping("/newFundReservationCancel")
    public String newReservationCancel() {
        return "my/newFundReservationCancel";
    }

    @GetMapping("/lateNewFundReservationCancel")
    public String lateNewReservationCancel() {
        return "my/lateNewFundReservationCancel";
    }

    @GetMapping("/additionalInvestment")
    public String additionalInvestment() {
        return "my/additionalInvestmentReservation";
    }

    @GetMapping("/additionalInvestmentHistory")
    public String additionalInvestmentHistory() {
        return "my/additionalInvestmentHistory";
    }
}