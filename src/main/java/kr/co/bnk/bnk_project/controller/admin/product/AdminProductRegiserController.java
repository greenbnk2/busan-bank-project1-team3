package kr.co.bnk.bnk_project.controller.admin.product;

import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
import kr.co.bnk.bnk_project.dto.admin.AdminFundMasterDTO;
import kr.co.bnk.bnk_project.dto.admin.ProductListDTO;
import kr.co.bnk.bnk_project.service.admin.AdminFundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class AdminProductRegiserController {

    private final AdminFundService adminFundService;

    /*펀드 신규 등록 화면*/
    @GetMapping("/register")
    public String showFundRegister(PageRequestDTO pageRequestDTO, Model model) {

        AdminFundMasterDTO fund = adminFundService.getPendingFund(pageRequestDTO);

        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("fund", fund);

        return "admin/product/adminproduct-register";
    }


    @PostMapping("/register")
    public String registerFund(AdminFundMasterDTO formDto) {

        adminFundService.updateFundAndChangeStatus(formDto);

        return "redirect:/admin/product/pending";
    }


    /* 자동완성 API
     */
    @GetMapping("/autocomplete")
    @ResponseBody
    public List<Map<String, String>> autocompleteFund(
            @RequestParam String searchType,
            @RequestParam String keyword) {

        List<AdminFundMasterDTO> list = adminFundService.getFundSuggestions(searchType, keyword);

        return list.stream()
                .map(f -> {
                    Map<String, String> m = new HashMap<>();
                    m.put("fundCode", f.getFundCode());
                    m.put("fundName", f.getFundName());
                    return m;
                })
                .toList();
    }

/*
    @GetMapping("/edit")
    public String productEdit(@RequestParam(required = false) Long id) {
        return "admin/product/adminproduct-edit";
    }
*/



    @GetMapping("/edit")
    public String productEdit(@RequestParam String fundCode, Model model) {

        AdminFundMasterDTO fund = adminFundService.getPendingFundEdit(fundCode);

        model.addAttribute("fund", fund);

        return "admin/product/adminproduct-edit";
    }

    @PostMapping("/edit")
    public String updateFund(AdminFundMasterDTO formDto) {

        adminFundService.updateFund(formDto);

        return "redirect:/admin/product/pending";
    }



    // 펀드 목록 조회
    @GetMapping("/pending")
    public String productList(PageRequestDTO pageRequestDTO , Model model) {

        PageResponseDTO<ProductListDTO> pageResponse = adminFundService.getProductPage(pageRequestDTO);
        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("pageResponse", pageResponse);
        model.addAttribute("dtoList", pageResponse.getDtoList());



        return "admin/product/adminproduct-pending";
    }




    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopFund(@RequestParam String fundCode) {
        adminFundService.stopFund(fundCode);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "운용중단 처리되었습니다."
        ));
    }

    @PostMapping("/resume")
    public ResponseEntity<Map<String, Object>> resumeFund(@RequestParam String fundCode) {
        adminFundService.resumeFund(fundCode);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "운용재개 처리되었습니다."
        ));
    }

}
