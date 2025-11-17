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

    ///////////////////////////////////////
    ///////       공시자료      ////////////
    //////////////////////////////////////

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


    ///////////////////////////////////////
    ///////       펀드시황      ////////////
    //////////////////////////////////////

    @GetMapping("/fund-market")
    public String fundMarket(Model model) {

        List<InfoPostDTO>  dtoList = infoPostService.findAllFundMarket();
        model.addAttribute("dtoList",dtoList);

        // 모달 등록용 폼 객체
        InfoPostDTO postForm = new InfoPostDTO();
        postForm.setStatus("게시중");
        model.addAttribute("postForm",postForm);


        return "admin/info&disclosures/fund_market";
    }

    // 펀드 시황 등록 + 파일첨부
    @PostMapping("/market")
    public String createMarket(@ModelAttribute("postForm") InfoPostDTO infoPostDTO,
                              @RequestParam("attachment")MultipartFile attachment) {

        infoPostService.createMarket(infoPostDTO,attachment);

        return "redirect:/admin/info/market";
    }

    // 펀드 시황 상세
    @GetMapping("market/detail")
    public String MarketDetail(@RequestParam("postId") int postId, Model model){

        InfoPostDTO dto = infoPostService.findFundMarketById(postId);
        model.addAttribute("post",dto);

        return "admin/info&disclosures/fund_market";
    }

    // 펀드 시황 수정
    @PostMapping("/market/update")
    public String updateMarket(
            @RequestParam("id") int postId,
            @RequestParam("title") String title,
            @RequestParam("marketType") String marketType,
            @RequestParam("content") String content,
            @RequestParam(value = "attachment" , required = false) MultipartFile attachment
    ) {
        // DTO 생성
        InfoPostDTO dto = new InfoPostDTO();
        dto.setPostId(postId);
        dto.setTitle(title);
        dto.setFundMarketType(marketType);
        dto.setContent(content);

        infoPostService.updateGuide(dto,attachment);

        return "redirect:/admin/info/market";
    }

    // 펀드 시황 삭제
    @PostMapping("/market/delete")
    public String deleteMarket(@RequestParam("id") int postId){

        infoPostService.deleteMarket(postId);

        return  "redirect:/admin/info/market";
    }





    ///////////////////////////////////////
    ///////       가이드      ////////////
    //////////////////////////////////////

    // 펀드가이드 목록 전체
    @GetMapping("/guide")
    public String guide(Model model) {

        List<InfoPostDTO>  dtoList = infoPostService.findAllFundGuide();
        model.addAttribute("dtoList",dtoList);

        // 모달 등록용 폼 객체
        InfoPostDTO postForm = new InfoPostDTO();
        postForm.setStatus("노출중");
        model.addAttribute("postForm",postForm);

        return "admin/info&disclosures/guide";
    }

    // 펀드 가이드 등록 + 파일첨부
    @PostMapping("/guide")
    public String createGuide(@ModelAttribute("postForm") InfoPostDTO infoPostDTO,
                              @RequestParam("attachment")MultipartFile attachment) {

        infoPostService.createGuide(infoPostDTO,attachment);

        return "redirect:/admin/info/guide";
    }

    // 펀드 가이드 상세
    @GetMapping("guide/detail")
    public String guideDetail(@RequestParam("postId") int postId, Model model){

        InfoPostDTO dto = infoPostService.findFundGuideById(postId);
        model.addAttribute("post",dto);

        return "admin/info&disclosures/guide";
    }

    // 펀드 가이드 수정
    @PostMapping("/guide/update")
    public String updateGuide(
            @RequestParam("id") int postId,
            @RequestParam("title") String title,
            @RequestParam("guideType") String guideType,
            @RequestParam("content") String content,
            @RequestParam(value = "attachment" , required = false) MultipartFile attachment
    ) {
        // DTO 생성
        InfoPostDTO dto = new InfoPostDTO();
        dto.setPostId(postId);
        dto.setTitle(title);
        dto.setGuideType(guideType);
        dto.setContent(content);

        infoPostService.updateGuide(dto,attachment);

        return "redirect:/admin/info/guide";
    }

    // 펀드 가이드 삭제
    @PostMapping("/guide/delete")
    public String deleteGuide(@RequestParam("id") int postId){

        infoPostService.deleteGuide(postId);

        return  "redirect:/admin/info/guide";
    }





}

