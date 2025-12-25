package kr.co.bnk.bnk_project.controller.admin;

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
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {


    private final PermissionService permissionService;


    @GetMapping({"/", "/main"})
    public String adminMain() {
        return "admin/adminMain";
    }

    @GetMapping("/login")
    public String adminLoginPage() {
      return "admin/login";
    }





    /*-------------------------권한 변경-------------------------*/


    @GetMapping("/permission")
    public String permissionList(PageRequestDTO pageRequestDTO, Model model) {

        // 1) 위쪽 “검색결과” 테이블용 (회원검색)
        PageResponseDTO<UserSearchDTO> pageResponse = null;
        if (pageRequestDTO.getKeyword() != null &&
                !pageRequestDTO.getKeyword().isBlank()) {

            pageResponse = permissionService.getUserSearchPage(pageRequestDTO);
        }

        // 2) 아래쪽 “관리자 목록” 테이블용
        PageResponseDTO<AdminListDTO> adminPage =
                permissionService.getAdminList(pageRequestDTO);

        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("pageResponse", pageResponse);   // 검색 결과
        model.addAttribute("adminPage", adminPage);     // 관리자 목록

        return "admin/permission/permission";
    }


    @PostMapping("/permission/add")
    public String addAdmin(@RequestParam("custNo") Long custNo,
                           @RequestParam("role") String role,
                           PageRequestDTO pageRequestDTO,
                           RedirectAttributes redirectAttributes) {

        permissionService.addAdmin(custNo, role);

        // 메시지 (필요하면)
        redirectAttributes.addFlashAttribute("msg", "관리자가 추가되었습니다.");

        // 검색 조건 유지해서 다시 목록으로
        redirectAttributes.addAttribute("searchType", pageRequestDTO.getSearchType());
        redirectAttributes.addAttribute("keyword", pageRequestDTO.getKeyword());
        redirectAttributes.addAttribute("pg", pageRequestDTO.getPg());
        redirectAttributes.addAttribute("size", pageRequestDTO.getSize());

        return "redirect:/admin/permission";
    }


    // 관리자 권한 수정 (AJAX)
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
}

