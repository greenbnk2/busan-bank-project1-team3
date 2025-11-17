package kr.co.bnk.bnk_project.controller.admin.settings;

import kr.co.bnk.bnk_project.dto.UserTermsDTO;
import kr.co.bnk.bnk_project.service.UserTermsService;
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


    @GetMapping("/category")
    public String categoryManagement() {
        return "admin/settings/category_management";
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

