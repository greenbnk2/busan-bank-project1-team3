package kr.co.bnk.bnk_project.controller.admin.member;

import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
import kr.co.bnk.bnk_project.dto.admin.AdminListDTO;
import kr.co.bnk.bnk_project.dto.admin.UserSearchDTO;
import kr.co.bnk.bnk_project.service.admin.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/member")
@RequiredArgsConstructor
public class AdminPermissionController {

    private final PermissionService permissionService;

    @GetMapping("/permission")
    public String permissionList(
            @RequestParam(value = "userSearchType", required = false) String userSearchType,
            @RequestParam(value = "userKeyword", required = false) String userKeyword,
            @RequestParam(value = "userPg", defaultValue = "1") int userPg,
            @RequestParam(value = "adminSearchType", required = false) String adminSearchType,
            @RequestParam(value = "adminKeyword", required = false) String adminKeyword,
            @RequestParam(value = "adminPg", defaultValue = "1") int adminPg,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        PageRequestDTO userRequest = new PageRequestDTO();
        userRequest.setPg(userPg);
        userRequest.setSize(size);
        userRequest.setSearchType(userSearchType);
        userRequest.setKeyword(userKeyword);

        PageRequestDTO adminRequest = new PageRequestDTO();
        adminRequest.setPg(adminPg);
        adminRequest.setSize(size);
        adminRequest.setSearchType(adminSearchType);
        adminRequest.setKeyword(adminKeyword);

        PageResponseDTO<UserSearchDTO> pageResponse = null;
        if (userKeyword != null && !userKeyword.isBlank()) {
            pageResponse = permissionService.getUserSearchPage(userRequest);
        }

        PageResponseDTO<AdminListDTO> adminPage = permissionService.getAdminList(adminRequest);

        model.addAttribute("userRequest", userRequest);
        model.addAttribute("adminRequest", adminRequest);
        model.addAttribute("pageResponse", pageResponse);
        model.addAttribute("adminPage", adminPage);
        return "admin/member/permission";
    }

    @PostMapping("/permission/add")
    public String addAdmin(@RequestParam("custNo") Long custNo,
                           @RequestParam("role") String role,
                           @RequestParam(value = "userSearchType", required = false) String userSearchType,
                           @RequestParam(value = "userKeyword", required = false) String userKeyword,
                           @RequestParam(value = "userPg", defaultValue = "1") int userPg,
                           @RequestParam(value = "adminSearchType", required = false) String adminSearchType,
                           @RequestParam(value = "adminKeyword", required = false) String adminKeyword,
                           @RequestParam(value = "adminPg", defaultValue = "1") int adminPg,
                           @RequestParam(value = "size", defaultValue = "10") int size,
                           RedirectAttributes redirectAttributes) {

        permissionService.addAdmin(custNo, role);

        redirectAttributes.addFlashAttribute("msg", "관리자가 추가되었습니다.");
        redirectAttributes.addAttribute("userSearchType", userSearchType);
        redirectAttributes.addAttribute("userKeyword", userKeyword);
        redirectAttributes.addAttribute("userPg", userPg);
        redirectAttributes.addAttribute("adminSearchType", adminSearchType);
        redirectAttributes.addAttribute("adminKeyword", adminKeyword);
        redirectAttributes.addAttribute("adminPg", adminPg);
        redirectAttributes.addAttribute("size", size);

        return "redirect:/admin/member/permission";
    }

    @PostMapping("/permission/role")
    @ResponseBody
    public ResponseEntity<?> updateAdminRole(
            @RequestParam("adminNo") Long adminNo,
            @RequestParam("role") String role
    ) {
        permissionService.updateAdminRole(adminNo, role);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "adminNo", adminNo,
                "role", role
        ));
    }


    @PostMapping("/permission/delete")
    @ResponseBody
    public ResponseEntity<?> deleteAdmin(
            @RequestParam("adminNo") Long adminNo
    ) {
        permissionService.deleteAdminRole(adminNo);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "adminNo", adminNo
        ));
    }
}


