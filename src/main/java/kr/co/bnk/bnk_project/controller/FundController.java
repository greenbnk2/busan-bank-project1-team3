package kr.co.bnk.bnk_project.controller;

import kr.co.bnk.bnk_project.dto.ProductDTO;
import kr.co.bnk.bnk_project.service.FundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;


@RequestMapping("/fund")
@Controller
@RequiredArgsConstructor
public class FundController {

    private final FundService productService;

    @GetMapping("/depositGuide")
    public String depositGuide() {
        return "fund/depositGuide";
    }

    @GetMapping("/FAQ")
    public String faq() {
        return "faq";
    }

    @GetMapping("/productList")
    public String productList(Model model) {
        List<ProductDTO> list = productService.getProductList();
        model.addAttribute("productList", list);
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
    @GetMapping("/admin/info")
    public String InfoAndDisclosure() {
        return "admin/info&disclosures/disclosures";
    }
}
