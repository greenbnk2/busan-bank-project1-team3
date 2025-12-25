package kr.co.bnk.bnk_project.controller.admin.product;

import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
import kr.co.bnk.bnk_project.dto.admin.FundListDetailDTO;
import kr.co.bnk.bnk_project.dto.admin.ProductListDTO;
import kr.co.bnk.bnk_project.service.admin.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/*
    날짜 : 2025/11/26
    이름 : 이종봉
    내용 : 펀드 목록 상세(돋보기)
 */
@Slf4j
@Controller
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    // 펀드 목록 조회
    @GetMapping
    public String productList(PageRequestDTO pageRequestDTO , Model model) {

        PageResponseDTO<ProductListDTO> pageResponse = productService.getProductPage(pageRequestDTO);
        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("pageResponse", pageResponse);
        model.addAttribute("dtoList", pageResponse.getDtoList());



        return "admin/product/adminproduct";
    }

    // 펀드 목록 상세(돋보기)
    @GetMapping("/detail")
    @ResponseBody
    public FundListDetailDTO detail(@RequestParam String fundCode){
        return productService.getProductDetail(fundCode);
    }



    @GetMapping("/documents")
    public String productDocuments() {
        return "admin/product/adminproduct-documents";
    }

    @GetMapping("/status")
    public String productStatus(PageRequestDTO pageRequestDTO, Model model) {
        model.addAttribute("pageRequestDTO", pageRequestDTO);
        return "admin/product/adminproduct-status";
    }
}

