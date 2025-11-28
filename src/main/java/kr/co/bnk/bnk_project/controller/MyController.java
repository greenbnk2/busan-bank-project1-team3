package kr.co.bnk.bnk_project.controller;

import jakarta.validation.Valid;
import kr.co.bnk.bnk_project.dto.*;
import kr.co.bnk.bnk_project.security.MyUserDetails;
import kr.co.bnk.bnk_project.service.CsService;
import kr.co.bnk.bnk_project.service.FundService;
import kr.co.bnk.bnk_project.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/my")
public class MyController {

    private final MemberService memberService;
    private final FundService fundService;
    private final CsService csService;

    @GetMapping("/dashboard")
    public String myDashboard(@AuthenticationPrincipal MyUserDetails userDetails, Model model) {

        // 로그인한 사용자 PK 가져오기
        Long custNo = userDetails.getUserDTO().getCustNo();

        // 2. 서비스 호출
        List<UserFundDTO> fundList = fundService.getMyFundList(custNo);

        // 3. 상단 요약 정보 계산 (총 평가금액, 총 수익률 등)
        long totalEval = fundService.calculateTotalEval(fundList);
        double totalYield = fundService.calculateTotalYield(fundList);
        long totalDiff = totalEval - fundList.stream().mapToLong(UserFundDTO::getPurchaseAmount).sum();

        // 4. 모델에 담아서 뷰로 전달
        model.addAttribute("fundList", fundList);
        model.addAttribute("totalEval", totalEval);
        model.addAttribute("totalYield", String.format("%.2f", totalYield)); // 소수점 2자리 포맷
        model.addAttribute("totalDiff", totalDiff);

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

    // 1:1 문의
    @GetMapping("/qna/write")
    public String qnaWrite(Model model) {
        model.addAttribute("csDTO", new CsDTO());
        return "my/qna/write";
    }

    // 1:1 문의 등록 처리
    @PostMapping("/qna/write")
    public String qnaWritePro(@AuthenticationPrincipal MyUserDetails userDetails,
                              @Valid @ModelAttribute("csDTO") CsDTO csDTO,
                              BindingResult bindingResult,
                              Model model) {

        // 유효성 검사 실패 시 다시 입력 화면으로
        if (bindingResult.hasErrors()) {
            return "my/qna/write";
        }

        // 로그인한 사용자 ID 설정
        csDTO.setUserId(userDetails.getUsername());

        // 서비스 호출
        csService.registerInquiry(csDTO);

        // 저장 완료 후 목록 페이지로 이동
        return "redirect:/my/qna/list";
    }

    // 나의 문의 내역
    @GetMapping("/qna/list")
    public String qnaList(@AuthenticationPrincipal MyUserDetails userDetails,
                          PageRequestDTO pageRequestDTO,
                          Model model) {

        // 내 아이디를 sellerId 필드에 저장
        pageRequestDTO.setSellerId(userDetails.getUsername());

        // 서비스 호출
        PageResponseDTO<CsDTO> responseDTO = csService.getQnaPage(pageRequestDTO);

        // 화면으로 데이터 전달
        model.addAttribute("responseDTO", responseDTO);

        return "my/qna/list";
    }

    @GetMapping("/account")
    public String myAccountInquiry(@AuthenticationPrincipal MyUserDetails userDetails, Model model) {
        // 로그인한 사용자 정보 가져오기
        Long custNo = userDetails.getUserDTO().getCustNo();

        // DB에서 내 펀드 목록 가져오기
        List<UserFundDTO> accountList = fundService.getMyFundList(custNo);

        log.info("조회된 펀드 개수: {}", accountList != null ? accountList.size() : 0);

        model.addAttribute("accountList", accountList);

        return "my/check/fundAccountInquiry";
    }

    @GetMapping("/price")
    public String myPriceInquiry(Model model,
                                 @AuthenticationPrincipal MyUserDetails userDetails,
                                 @RequestParam(required = false, defaultValue = "customer") String queryType,
                                 @RequestParam(required = false) String fundCode,
                                 @RequestParam(required = false) String startDate,
                                 @RequestParam(required = false) String endDate) {

        Long custNo = userDetails.getUserDTO().getCustNo();
        List<FundMasterDTO> fundList = new ArrayList<>();

        // 펀드 목록 조회
        if ("customer".equals(queryType)) {
            // [고객보유상품] - 내 계좌에서 펀드 목록 가져와서 변환
            List<UserFundDTO> myFunds = fundService.getMyFundList(custNo);
            // UserFundDTO -> FundMasterDTO 변환 (드롭다운 표시용)
            for (UserFundDTO myFund : myFunds) {
                FundMasterDTO dto = new FundMasterDTO();
                dto.setFundCode(myFund.getFundCode());
                dto.setFundName(myFund.getFundName());
                fundList.add(dto);
            }
            // 중복 제거
            fundList = fundList.stream().distinct().collect(Collectors.toList());

        } else {
            // [전체상품] - 마스터 테이블에서 전체 조회
            fundList = fundService.getAllFunds();
        }

        model.addAttribute("fundList", fundList);

        // 2. 기준가격 이력 조회 (검색 실행 시)
        if (fundCode != null && startDate != null && endDate != null) {
            List<FundPriceDTO> priceList = fundService.getPriceHistory(fundCode, startDate, endDate);
            model.addAttribute("priceList", priceList);
        }

        // 3. [중요] 입력한 검색 조건 유지 (화면 리셋 방지)
        model.addAttribute("queryType", queryType);
        model.addAttribute("fundCode", fundCode);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "my/check/basicPriceInquiry";
    }

    @GetMapping("/yield")
    public String myYieldInquiry() {
        return "my/check/yieldInquiry";
    }

    @GetMapping("/report")
    public String myReportChange() {
        return "my/check/reportChange";
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