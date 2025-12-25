package kr.co.bnk.bnk_project.controller.admin.member;

import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
import kr.co.bnk.bnk_project.dto.admin.MemberListDTO;
import kr.co.bnk.bnk_project.service.admin.AdminMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/member")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    @GetMapping("/list")
    public String memberList(PageRequestDTO pageRequestDTO, Model model) {

        PageResponseDTO<MemberListDTO> pageResponse =
                adminMemberService.getMemberPage(pageRequestDTO);

        // 뷰에 전달
        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("pageResponse", pageResponse);

        return "admin/member/list";
    }


    /** 회원 수정 페이지 */
    @GetMapping("/detail")
    public String editMember(@RequestParam("custNo") int custNo, Model model) {

        MemberListDTO pageResponse = adminMemberService.getMember(custNo);

        if (pageResponse == null) {
            return "redirect:/admin/member/list";
        }

        model.addAttribute("pageResponse", pageResponse);

        return "admin/member/detail";
    }

    /** 회원 수정 처리 */
    @PostMapping("/update")
    public String updateMember(MemberListDTO dto) {

        adminMemberService.updateMember(dto);

        // 수정 후 목록으로 리다이렉트
        return "redirect:/admin/member/list";
    }

    /** 회원 상세 페이지 */
    @GetMapping("/list-detail")
    public String memberDetail(@RequestParam("custNo") Long custNo, Model model) {
        // TODO: 실제 회원 정보, 계좌정보, 직장정보, 부가정보를 서비스에서 가져와야 함
        // 임시로 하드코딩된 데이터 사용
        MemberListDTO member = adminMemberService.getMember(custNo.intValue());
        
        if (member == null) {
            return "redirect:/admin/member/list";
        }
        
        model.addAttribute("member", member);
        // TODO: 계좌정보, 직장정보, 부가정보 추가
        // model.addAttribute("account", account);
        // model.addAttribute("jobInfo", jobInfo);
        // model.addAttribute("additionalInfo", additionalInfo);
        
        return "admin/member/list-detail";
    }

    @PostMapping("/status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@RequestParam("custNo") int custNo,
                                          @RequestParam("statusCode") String statusCode) {

        adminMemberService.updateMemberStatus(custNo, statusCode);
        return ResponseEntity.ok().body("success");
    }


}
