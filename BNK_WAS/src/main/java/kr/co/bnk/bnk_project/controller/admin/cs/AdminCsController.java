package kr.co.bnk.bnk_project.controller.admin.cs;

import kr.co.bnk.bnk_project.dto.CsDTO;
import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
import kr.co.bnk.bnk_project.service.CsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin/cs")
@RequiredArgsConstructor
public class AdminCsController {


    private final CsService csService;


    /*-------------------------목록 조회-------------------------*/
    @GetMapping("/faq")
    public String faqManagement(PageRequestDTO pageRequestDTO, Model model) {

        // 목록 + 페이징 정보
        PageResponseDTO<CsDTO> pageResponse = csService.getFaqPage(pageRequestDTO);

        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("pageResponse", pageResponse);

        return "admin/cs/faq";
    }

    @GetMapping("/qna")
    public String qnaManagement(PageRequestDTO pageRequestDTO, Model model) {

        if (pageRequestDTO.getPg() <= 0) pageRequestDTO.setPg(1);
        if (pageRequestDTO.getSize() <= 0) pageRequestDTO.setSize(10);

        pageRequestDTO.setCate("qna");   // 선택사항, 너가 쓰면 유지

        PageResponseDTO<CsDTO> pageResponse = csService.getQnaPage(pageRequestDTO);

        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("pageResponse", pageResponse);

        return "admin/cs/qna";
    }



    /*-------------------------faq 등록-------------------------*/

    @GetMapping("/faq/register")
    public String faqRegister() {
        return "admin/cs/faq-register";
    }

    @PostMapping("/faq/register")
    public String registerFaq(CsDTO csDTO) {

        // 카테고리 ID = FAQ (네가 정한 카테고리 번호로 넣기)
        csDTO.setCategoryId(8L); // FAQ 카테고리

        // 상태는 자동으로 답변완료
        csDTO.setStatus("답변완료");

        csService.insertFaq(csDTO);

        return "redirect:/admin/cs/faq";
    }


    /*-------------------------qna 상세-------------------------*/
    @GetMapping("/qna/detail")
    public String qnaDetail(@RequestParam("id") Long csId, Model model) {

        CsDTO qna = csService.getQnaDetail(csId);

        if (qna == null) {
            // 잘못된 ID로 들어온 경우 목록으로 돌려보내기
            return "redirect:/admin/cs/qna-detail";
        }

        model.addAttribute("qna", qna);
        return "admin/cs/qna-detail";
    }

    /*-------------------------수정-------------------------*/
    // FAQ 수정 폼
    @GetMapping("/faq/edit")
    public String editFaqForm(@RequestParam("id") Long csId, Model model) {

        CsDTO faq = csService.getFaq(csId);
        if (faq == null) {
            return "redirect:/admin/cs/faq";
        }

        model.addAttribute("faq", faq);
        return "admin/cs/faq-edit";   // 수정 화면 템플릿
    }

    // FAQ 수정 처리
    @PostMapping("/faq/edit")
    public String editFaqSubmit(CsDTO csDTO) {

        csService.updateFaq(csDTO);

        return "redirect:/admin/cs/faq";
    }

    // QnA 답변 페이지 이동
    @GetMapping("/qna/edit")
    public String qnaAnswerForm(@RequestParam("id") Long csId, Model model) {

        CsDTO qna = csService.getQna(csId);
        if (qna == null) {
            return "redirect:/admin/cs/qna";
        }

        model.addAttribute("qna", qna);
        return "admin/cs/qna-edit";
    }

    // QnA 답변 저장
    @PostMapping("/qna/edit")
    public String qnaAnswerSubmit(CsDTO csDTO) {

        csService.updateQnaAnswer(csDTO);
        return "redirect:/admin/cs/qna";
    }



    /*-------------------------삭제-------------------------*/

    @PostMapping("/faq/delete")
    public String deleteFaq(@RequestParam("id") Long csId,
                            PageRequestDTO pageRequestDTO,
                            RedirectAttributes ra) {

        csService.deleteFaq(csId);
        ra.addFlashAttribute("msg", "FAQ가 삭제되었습니다.");

        // 현재 검색조건/페이지 유지하려면 쿼리스트링 붙여도 됨
        return "redirect:/admin/cs/faq";
    }

    // QnA 삭제
    @PostMapping("/qna/delete")
    public String deleteQna(@RequestParam("id") Long csId,
                            PageRequestDTO pageRequestDTO,
                            RedirectAttributes ra) {

        csService.deleteQna(csId);
        ra.addFlashAttribute("msg", "QnA가 삭제되었습니다.");

        // 검색 + 페이징 유지하려면 쿼리스트링 붙여도 됨
        return "redirect:/admin/cs/qna";
    }

}

