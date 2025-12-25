package kr.co.bnk.bnk_project.controller.admin.approval;

import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
import kr.co.bnk.bnk_project.dto.admin.ApprovalDTO;
import kr.co.bnk.bnk_project.dto.admin.FieldChangeDTO;
import kr.co.bnk.bnk_project.security.AdminUserDetails;
import kr.co.bnk.bnk_project.service.admin.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/approval")
@RequiredArgsConstructor
public class AdminApprovalController {

    private final ApprovalService approvalService;

    @GetMapping
    public String approvalManagement(PageRequestDTO pageRequestDTO, Model model) {

        // 목록 + 페이징 정보
        PageResponseDTO<ApprovalDTO> pageResponse = approvalService.selectpendingApproval(pageRequestDTO);

        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("pageResponse", pageResponse);

        return "admin/approval_management/approval_management";
    }

    @GetMapping("/history")
    public String approvalHistory(PageRequestDTO pageRequestDTO, Model model) {

        // 결재 이력 목록 + 페이징 정보
        PageResponseDTO<ApprovalDTO> pageResponse = approvalService.selectApprovalHistory(pageRequestDTO);

        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("pageResponse", pageResponse);

        return "admin/approval_management/approval_history";
    }

    @PostMapping("/approve")
    public String approve(@ModelAttribute ApprovalDTO approvalDTO,
                          @AuthenticationPrincipal AdminUserDetails loginAdmin) {

        if (loginAdmin != null) {
            approvalDTO.setApprover(loginAdmin.getDisplayName());
        }

        approvalService.approvalFund(approvalDTO);

        return "redirect:/admin/approval";
    }

    @GetMapping("/detail/{apprNo}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getApprovalDetail(@PathVariable Long apprNo) {
        ApprovalDTO approval = approvalService.getApprovalDetail(apprNo);
        Map<String, Object> response = new HashMap<>();
        response.put("approval", approval);

        // 수정일 때만 변경사항 조회
        if (approval != null && "수정".equals(approval.getApprType())) {
            List<FieldChangeDTO> changes = approvalService.compareRevisionWithMaster(approval.getFundCode());
            response.put("changes", changes);
        } else {
            response.put("changes", List.of());
        }

        return ResponseEntity.ok(response);
    }

}

