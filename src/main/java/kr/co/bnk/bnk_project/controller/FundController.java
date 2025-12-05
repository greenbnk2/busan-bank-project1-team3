package kr.co.bnk.bnk_project.controller;

import jakarta.servlet.http.HttpServletResponse;
import kr.co.bnk.bnk_project.dto.KeywordDTO;
import kr.co.bnk.bnk_project.dto.ProductDTO;
import kr.co.bnk.bnk_project.security.MyUserDetails;
import kr.co.bnk.bnk_project.service.FundService;
import kr.co.bnk.bnk_project.service.InvestmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping("/fund")
@Controller
@RequiredArgsConstructor
public class FundController {

    private final FundService productService;
    private final InvestmentService investmentService;

    @GetMapping("/depositGuide")
    public String depositGuide() {
        return "fund/depositGuide";
    }

    @GetMapping("/FAQ")
    public String faq() {
        return "faq";
    }

    @GetMapping("/productList")
    public String productList(Model model, Authentication authentication, HttpServletResponse response) throws Exception {

        if (authentication != null && authentication.isAuthenticated()) {
            MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
            Long custNo = userDetails.getUserDTO().getCustNo();

            // 1. 유효성 체크
            boolean isValid = investmentService.isRiskTestValid(custNo);
            if (!isValid) {
                // (알림창 띄우는 기존 코드 유지...)
                return null;
            }

            // 2. 유저의 투자성향 텍스트 가져오기 (예: "공격투자형")
            String userRiskType = investmentService.getUserRiskType(custNo);

            // 3. 성향에 맞는 상품만 조회하도록 서비스 호출
            List<ProductDTO> list = productService.getProductListByRisk(userRiskType);

            model.addAttribute("productList", list);
            model.addAttribute("userRiskType", userRiskType); // 화면 표시용

            return "productList";
        }

        // 비로그인 처리
        return "redirect:/member/login";
    }


    @GetMapping("/productDetail/{fundCode}")
    public String productDetail(@PathVariable("fundCode") String fundCode, Model model) {

        ProductDTO detail = productService.getProductDetail(fundCode);

        model.addAttribute("detail", detail);

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
    public String fundInformation(Model model) {

        List<ProductDTO> docs = productService.getFundDocuments();

        model.addAttribute("documents", docs);

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

    @GetMapping("/investTest")
    public String investTest() {
        return "investTest";
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "keyword", required = false) String keyword, Model model) {

        // 검색 로직 수행
        List<ProductDTO> resultList = productService.searchFunds(keyword);

        // 결과 리스트 화면으로 전달
        model.addAttribute("fundList", resultList);

        // 검색 결과 페이지에서도 모달에 띄울 '추천 키워드' 전달
        List<KeywordDTO> recommendedKeywords = productService.getRecommendedKeywords();
        model.addAttribute("keywordList", recommendedKeywords);

        return "searchResult";
    }

    @GetMapping("/nav/{fundCode}")
    @ResponseBody
    public Object getFundNavLast3Months(@PathVariable("fundCode") String fundCode) {

        var list = productService.getFundNavLast3Months(fundCode);

        // labels, data 배열 생성
        List<String> labels = new java.util.ArrayList<>();
        List<Double> data = new java.util.ArrayList<>();

        list.forEach(row -> {
            labels.add(row.getBaseDate());
            data.add(row.getNavPerUnit());
        });

        // JSON 형태로 리턴
        return java.util.Map.of(
                "labels", labels,
                "data", data
        );
    }
}

