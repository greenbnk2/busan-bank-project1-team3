package kr.co.bnk.bnk_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class fundController {

    @GetMapping("/fund/depositGuide")
    public String depositGuide() {
        return "fund/depositGuide";
    }

    @GetMapping("/FAQ")
    public String faq() {
        return "faq";
    }

    @GetMapping("/productList")
    public String productList() {
        return "productList";
    }
    @GetMapping("/productDetail")
    public String productDetail() {
        return "productDetail";
    }

    @GetMapping("/investorInfo")
    public String investorInfo() {
        return "investorInfo";
    }

    @GetMapping("/fundSusi")
    public String fundSusi() {
        return "fundSusi";
    }

    @GetMapping("/fundSihwang")
    public String fundSihwang() {
        return "fundSihwang";
    }

    @GetMapping("/fundInformation")
    public String fundInfromation() {
        return "fundInformation";
    }

    @GetMapping("/fundGuide")
    public String fundGuide() {
        return "fundGuide";
    }

    @GetMapping("/dopisitGuide")
    public String dopisitGuide() {
        return "dopisitGuide";
    }

}
