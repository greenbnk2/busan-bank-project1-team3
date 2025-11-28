package kr.co.bnk.bnk_project.controller.admin.settings;

import kr.co.bnk.bnk_project.dto.CsDTO;
import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
import kr.co.bnk.bnk_project.dto.UserTermsDTO;
import kr.co.bnk.bnk_project.dto.admin.FundCategoryDTO;
import kr.co.bnk.bnk_project.service.UserTermsService;
import kr.co.bnk.bnk_project.service.admin.FundCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final UserTermsService userTermsService;
    private final FundCategoryService fundCategoryService;



    @GetMapping("/category")
    public String categoryManagement(PageRequestDTO pageRequestDTO, Model model) {

        // 목록 + 페이징 정보
        PageResponseDTO<FundCategoryDTO> pageResponse = fundCategoryService.getCategoryPage(pageRequestDTO);

        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("pageResponse", pageResponse);

        return "admin/settings/category_management";
    }

    @PostMapping("/category/status")
    public String toggleCategoryStatus(@RequestParam String categoryCode,
                                       @RequestParam String status,
                                       @RequestParam(defaultValue = "1") int pg,
                                       @RequestParam(required = false) String keyword,
                                       RedirectAttributes redirectAttributes) {

        boolean success = fundCategoryService.updateCategoryStatus(categoryCode, status);
        redirectAttributes.addFlashAttribute("msg", success ? "상태가 변경되었습니다." : "상태 변경에 실패했습니다.");

        redirectAttributes.addAttribute("pg", pg);
        if (keyword != null && !keyword.isBlank()) {
            redirectAttributes.addAttribute("keyword", keyword);
        }
        return "redirect:/admin/settings/category";
    }

    @PostMapping("/category/register")
    public String registerCategory(FundCategoryDTO fundCategoryDTO) {

        fundCategoryService.createCategory(fundCategoryDTO);
        return "redirect:/admin/settings/category";
    }

    @PostMapping("/category/update")
    public String updateCategory(@ModelAttribute FundCategoryDTO dto,
                                 RedirectAttributes redirectAttributes) {
        fundCategoryService.updateCategory(dto);
        return "redirect:/admin/settings/category";
    }

    @PostMapping("/category/delete")
    public String deleteCategory(@RequestParam String categoryCode,
                                 RedirectAttributes redirectAttributes,
                                 PageRequestDTO pageRequestDTO) {
        fundCategoryService.deleteCategory(categoryCode);
        return "redirect:/admin/settings/category";
    }



    @GetMapping("/search-keyword")
    public String searchKeyword() {
        return "admin/settings/search_keyword";
    }




    /*------------------------------약관--------------------------------*/
    @GetMapping("/terms")
    public String termsPage(Model model) {

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

        return "admin/settings/terms_management";
    }


/*    @GetMapping("/edit")
    public String editTermsForm(@RequestParam("termId") String termId, Model model) {

        UserTermsDTO term = userTermsService.getTerm(termId);
        model.addAttribute("term", term);

        return "admin/settings/terms_edit";
    }
    */
    @PostMapping("/update")
    public String updateTerms(@ModelAttribute UserTermsDTO dto,
                              RedirectAttributes redirectAttributes) {

        userTermsService.updateTerm(dto);

        redirectAttributes.addFlashAttribute("msg", "약관이 수정되었습니다.");

        // 수정 후 다시 약관 관리 메인으로 리다이렉트
        return "redirect:/admin/settings/terms";
    }

}

