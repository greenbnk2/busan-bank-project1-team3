package kr.co.bnk.bnk_project.controller.admin.info;

import kr.co.bnk.bnk_project.dto.admin.InfoPostDTO;
import kr.co.bnk.bnk_project.service.admin.InfoPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin/info")
@RequiredArgsConstructor
public class AdminInfoController {

    private final InfoPostService infoPostService;

    // 공시자료 목록 전체
    @GetMapping("/disclosures")
    public String list(Model model) {

        List<InfoPostDTO> dtoList = infoPostService.findAllInfoPost();
        model.addAttribute("dtoList",dtoList);

        // 모달 등록용 폼 객체
        InfoPostDTO postForm = new InfoPostDTO();
        postForm.setStatus("PUBLISHED");
        model.addAttribute("postForm",postForm);

        return "admin/info&disclosures/disclosures_documents";
    }

    // 공시자료등록 + 파일첨부
    @PostMapping("/disclosures")
    public String createDisclosure(@ModelAttribute("postForm") InfoPostDTO infoPostDTO,
                                   @RequestParam("attachment")MultipartFile attachment) {

        infoPostService.createDisclosure(infoPostDTO,attachment);
        return "redirect:/admin/info/disclosures";
    }

    // 공시자료 상세
    @GetMapping("disclosures/detail")
    public String detail(@RequestParam("postId") int postId, Model model){

        InfoPostDTO dto = infoPostService.findInfoPostById(postId);
        model.addAttribute("post",dto);

        return "admin/info&disclosures/disclosures_documents";

    }

    // 공시자료 수정
    @PostMapping("/disclosures/update")
    public String updateDisclosure(
            @RequestParam("id") int postId,
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam("content") String content,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment
    ) {
        // DTO  생성
        InfoPostDTO dto = new InfoPostDTO();
        dto.setPostId(postId);
        dto.setTitle(title);
        dto.setDisclosureType(category);
        dto.setContent(content);

        // 서비스 호출
        infoPostService.updateDisclosure(dto,attachment);

        return "redirect:/admin/info/disclosures";
    }

    // 공시자료 삭제
    @PostMapping("/disclosures/delete")
    public String deleteDisclosure(@RequestParam("id") int postId){

        infoPostService.deleteDisclosure(postId);

        return "redirect:/admin/info/disclosures";
    }


    @GetMapping("/ad-hoc")
    public String adHocDisclosure() {
        return "admin/info&disclosures/ad-hoc_disclosure";
    }

    @GetMapping("/fund-info")
    public String fundInfo() {
        return "admin/info&disclosures/fund_info";
    }

    @GetMapping("/fund-market")
    public String fundMarket() {
        return "admin/info&disclosures/fund_market";
    }

    @GetMapping("/guide")
    public String guide() {
        return "admin/info&disclosures/guide";
    }
}

