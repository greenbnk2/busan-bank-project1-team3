package kr.co.bnk.bnk_project.controller;

import kr.co.bnk.bnk_project.dto.ProductDTO;
import kr.co.bnk.bnk_project.service.FundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fund")
public class FundApiController {

    private final FundService fundService;

    @GetMapping("/list")
    public List<ProductDTO> getFundList() {
        return fundService.getProductList();
    }
    @GetMapping("/best")
    public List<ProductDTO> getFundBest() {
        return fundService.getFundYieldBest();
    }
}

