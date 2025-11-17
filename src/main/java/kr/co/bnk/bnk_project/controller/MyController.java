package kr.co.bnk.bnk_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/my")
public class MyController {

    @GetMapping("/dashboard")
    public String myDashboard() {
        // templates/my/dashboard.html 파일을 렌더링합니다.
        return "my/dashboard";
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