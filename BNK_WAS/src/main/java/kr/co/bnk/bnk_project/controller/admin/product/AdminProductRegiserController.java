package kr.co.bnk.bnk_project.controller.admin.product;

import kr.co.bnk.bnk_project.dto.CsDTO;
import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
import kr.co.bnk.bnk_project.dto.admin.AdminFundMasterDTO;
import kr.co.bnk.bnk_project.dto.admin.ApprovalDTO;
import kr.co.bnk.bnk_project.dto.admin.FundDocumentDTO;
import kr.co.bnk.bnk_project.dto.admin.ProductListDTO;
import kr.co.bnk.bnk_project.security.AdminUserDetails;
import kr.co.bnk.bnk_project.service.admin.AdminFundService;
import kr.co.bnk.bnk_project.service.admin.ApprovalService;
import kr.co.bnk.bnk_project.service.admin.EditLockService;
import kr.co.bnk.bnk_project.service.admin.FundCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class AdminProductRegiserController {

    private final AdminFundService adminFundService;
    private final ApprovalService approvalService;
    private final EditLockService editLockService;
    private final FundCategoryService fundCategoryService;

    private void populateCategories(Model model) {
        model.addAttribute("categoryList", fundCategoryService.getAllCategories());
    }

    /*펀드 신규 등록 화면*/
    @GetMapping("/register")
    public String showFundRegister(PageRequestDTO pageRequestDTO, Model model) {

        // 검색어가 있는 경우
        if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isBlank()) {
            // searchType 기본값 세팅
            if (pageRequestDTO.getSearchType() == null || pageRequestDTO.getSearchType().isBlank()) {
                pageRequestDTO.setSearchType("code");
            }

            // 먼저 이미 등록된 펀드인지 확인
            AdminFundMasterDTO registeredFund = adminFundService.getRegisteredFund(
                    pageRequestDTO.getSearchType(), 
                    pageRequestDTO.getKeyword()
            );

            if (registeredFund != null) {
                // 이미 등록된 펀드인 경우
                model.addAttribute("errorMessage", "이미 등록된 펀드입니다.");
                model.addAttribute("pageRequestDTO", pageRequestDTO);
                model.addAttribute("fund", null);
                populateCategories(model);
                return "admin/product/adminproduct-register";
            }

            // 대기 상태인 펀드 조회
            AdminFundMasterDTO fund = adminFundService.getPendingFund(pageRequestDTO);
            model.addAttribute("fund", fund);
        } else {
            model.addAttribute("fund", null);
        }

        model.addAttribute("pageRequestDTO", pageRequestDTO);
        populateCategories(model);

        return "admin/product/adminproduct-register";
    }


    @PostMapping("/register")
    public String registerFund(AdminFundMasterDTO formDto,
                               @RequestParam(value = "termsDoc", required = false) MultipartFile termsDoc,
                               @RequestParam(value = "investmentDoc", required = false) MultipartFile investmentDoc,
                               @RequestParam(value = "simpleInvestmentDoc", required = false) MultipartFile simpleInvestmentDoc,
                               RedirectAttributes redirectAttributes) {

        // 등록 전에 이미 등록된 펀드인지 확인
        if (formDto.getFundCode() != null && !formDto.getFundCode().isBlank()) {
            // 펀드코드로 이미 등록된 펀드 확인
            AdminFundMasterDTO registeredFund = adminFundService.getRegisteredFund("code", formDto.getFundCode());
            if (registeredFund != null) {
                redirectAttributes.addFlashAttribute("errorMessage", "이미 등록된 펀드입니다.");
                redirectAttributes.addFlashAttribute("showPopup", true);
                redirectAttributes.addAttribute("searchType", "code");
                redirectAttributes.addAttribute("keyword", formDto.getFundCode());
                return "redirect:/admin/product/register";
            }
        }

        // 펀드명으로도 확인 (펀드명이 있는 경우)
        if (formDto.getFundName() != null && !formDto.getFundName().isBlank()) {
            AdminFundMasterDTO registeredFund = adminFundService.getRegisteredFund("name", formDto.getFundName());
            if (registeredFund != null) {
                redirectAttributes.addFlashAttribute("errorMessage", "이미 등록된 펀드입니다.");
                redirectAttributes.addFlashAttribute("showPopup", true);
                redirectAttributes.addAttribute("searchType", "name");
                redirectAttributes.addAttribute("keyword", formDto.getFundName());
                return "redirect:/admin/product/register";
            }
        }

        // 등록 시도 전에 다시 한 번 중복 확인 (이중 체크)
        if (formDto.getFundCode() != null && !formDto.getFundCode().isBlank()) {
            AdminFundMasterDTO registeredFund = adminFundService.getRegisteredFund("code", formDto.getFundCode());
            if (registeredFund != null) {
                redirectAttributes.addFlashAttribute("errorMessage", "이미 등록된 펀드입니다.");
                redirectAttributes.addFlashAttribute("showPopup", true);
                redirectAttributes.addAttribute("searchType", "code");
                redirectAttributes.addAttribute("keyword", formDto.getFundCode());
                return "redirect:/admin/product/register";
            }
        }

        if (formDto.getFundName() != null && !formDto.getFundName().isBlank()) {
            AdminFundMasterDTO registeredFund = adminFundService.getRegisteredFund("name", formDto.getFundName());
            if (registeredFund != null) {
                redirectAttributes.addFlashAttribute("errorMessage", "이미 등록된 펀드입니다.");
                redirectAttributes.addFlashAttribute("showPopup", true);
                redirectAttributes.addAttribute("searchType", "name");
                redirectAttributes.addAttribute("keyword", formDto.getFundName());
                return "redirect:/admin/product/register";
            }
        }

        // 등록 시도 (oper_status = '대기'인 경우에만 업데이트됨)
        boolean success = adminFundService.updateFundAndChangeStatus(formDto);

        if (!success) {
            // 등록 실패 (이미 등록된 펀드)
            redirectAttributes.addFlashAttribute("errorMessage", "이미 등록된 펀드입니다.");
            redirectAttributes.addFlashAttribute("showPopup", true);
            if (formDto.getFundCode() != null && !formDto.getFundCode().isBlank()) {
                redirectAttributes.addAttribute("searchType", "code");
                redirectAttributes.addAttribute("keyword", formDto.getFundCode());
            } else if (formDto.getFundName() != null && !formDto.getFundName().isBlank()) {
                redirectAttributes.addAttribute("searchType", "name");
                redirectAttributes.addAttribute("keyword", formDto.getFundName());
            }
            return "redirect:/admin/product/register";
        }

        // 문서 3종 처리
        adminFundService.registerFundDocuments(
                formDto.getFundCode(),
                termsDoc,
                investmentDoc,
                simpleInvestmentDoc
        );

        // 등록 성공 - 펀드 관리 페이지로 이동
        redirectAttributes.addFlashAttribute("successMessage", "펀드가 성공적으로 등록되었습니다.");
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

    /* 펀드 중복 확인 API (등록 전 확인용) */
    @PostMapping("/check-duplicate")
    @ResponseBody
    public Map<String, Object> checkDuplicateFund(
            @RequestParam(required = false) String fundCode,
            @RequestParam(required = false) String fundName) {

        Map<String, Object> response = new HashMap<>();
        
        // 펀드코드로 확인
        if (fundCode != null && !fundCode.isBlank()) {
            AdminFundMasterDTO registeredFund = adminFundService.getRegisteredFund("code", fundCode);
            if (registeredFund != null) {
                response.put("duplicate", true);
                response.put("message", "이미 등록된 펀드입니다.");
                return response;
            }
        }

        // 펀드명으로 확인
        if (fundName != null && !fundName.isBlank()) {
            AdminFundMasterDTO registeredFund = adminFundService.getRegisteredFund("name", fundName);
            if (registeredFund != null) {
                response.put("duplicate", true);
                response.put("message", "이미 등록된 펀드입니다.");
                return response;
            }
        }

        response.put("duplicate", false);
        response.put("message", "등록 가능한 펀드입니다.");
        return response;
    }

/*
    @GetMapping("/edit")
    public String productEdit(@RequestParam(required = false) Long id) {
        return "admin/product/adminproduct-edit";
    }
*/



    @GetMapping("/edit")
    public String productEdit(@RequestParam String fundCode, Model model, HttpSession session) {
        // 세션 ID 가져오기
        String sessionId = session.getId();
        
        // 현재 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = "알 수 없음";
        if (authentication != null && authentication.getPrincipal() instanceof AdminUserDetails) {
            AdminUserDetails adminUserDetails = (AdminUserDetails) authentication.getPrincipal();
            userId = adminUserDetails.getDisplayName();
        }

        // 펀드 정보 조회 (잠금 상태와 관계없이)
        AdminFundMasterDTO fund = adminFundService.getPendingFundEdit(fundCode);
        model.addAttribute("fund", fund);
        model.addAttribute("sessionId", sessionId);
        populateCategories(model);

        // === 문서 3종 조회해서 모델에 담기 (조회용) ===
        List<FundDocumentDTO> docs = adminFundService.getFundDocuments(fundCode);
        FundDocumentDTO termsDoc = null;
        FundDocumentDTO investDoc = null;
        FundDocumentDTO summaryDoc = null;

        for (FundDocumentDTO d : docs) {
            if ("TERMS".equals(d.getDocType())) {
                termsDoc = d;
            } else if ("INVEST".equals(d.getDocType())) {
                investDoc = d;
            } else if ("SUMMARY".equals(d.getDocType())) {
                summaryDoc = d;
            }
        }

        model.addAttribute("termsDoc", termsDoc);
        model.addAttribute("investDoc", investDoc);
        model.addAttribute("summaryDoc", summaryDoc);
        
        // 잠금 시도
        String lockResult = editLockService.tryLock(fundCode, sessionId, userId);
        
        // 잠금 실패 시 (다른 사용자가 잠금 중)
        if (lockResult != null) {
            model.addAttribute("lockError", true);
            model.addAttribute("lockedBy", lockResult);
        } else {
            model.addAttribute("lockError", false); // 명시적으로 false 설정
        }

        return "admin/product/adminproduct-edit";
    }

    @PostMapping("/edit")
    public String updateFund(AdminFundMasterDTO formDto,
                             @RequestParam(required = false) MultipartFile termsDoc,
                             @RequestParam(required = false) MultipartFile investmentDoc,
                             @RequestParam(required = false) MultipartFile simpleInvestmentDoc,
                             HttpSession session) {

        String fundCode = formDto.getFundCode();
        String sessionId = session.getId();

        // 잠금 확인
        String lockCheck = editLockService.checkLock(fundCode, sessionId);
        if (lockCheck != null) {
            // 다른 사용자가 잠금 중이면 저장 실패
            return "redirect:/admin/product/edit?fundCode=" + fundCode + "&error=locked";
        }

        // 현재 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = "system";
        if (authentication != null && authentication.getPrincipal() instanceof AdminUserDetails) {
            AdminUserDetails adminUserDetails = (AdminUserDetails) authentication.getPrincipal();
            userId = adminUserDetails.getDisplayName();
        }

        // 수정 저장 (FUND_MASTER_REVISION에 INSERT)
        adminFundService.updateFund(formDto, userId);

        // 2) ★ 문서 3종 교체 (파일 올라온 것만)
        adminFundService.updateFundDocuments(
                formDto.getFundCode(),
                termsDoc,
                investmentDoc,
                simpleInvestmentDoc
        );

        // 잠금 해제
        editLockService.unlock(fundCode, sessionId);

        return "redirect:/admin/product/pending";
    }



    // 펀드 목록 조회
    @GetMapping("/pending")
    public String productList(PageRequestDTO pageRequestDTO, Model model, HttpSession session) {

        PageResponseDTO<ProductListDTO> pageResponse = adminFundService.getProductPage(pageRequestDTO);
        
        // 각 펀드의 잠금 상태 확인
        String currentSessionId = session.getId();
        Map<String, Boolean> lockStatusMap = new HashMap<>();
        for (ProductListDTO dto : pageResponse.getDtoList()) {
            String lockCheck = editLockService.checkLock(dto.getFundCode(), currentSessionId);
            // lockCheck가 null이 아니면 다른 사용자가 잠금 중
            lockStatusMap.put(dto.getFundCode(), lockCheck != null);
        }
        
        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("pageResponse", pageResponse);
        model.addAttribute("dtoList", pageResponse.getDtoList());
        model.addAttribute("lockStatusMap", lockStatusMap);
        populateCategories(model);

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


    /*---------------------------------------------*/
    /*--------------------결제-------------------------*/

    @PostMapping("/approval")
    public String insertApproval(ApprovalDTO approvalDTO) {
        approvalService.insertApproval(approvalDTO);
        return "redirect:/admin/product/pending";
    }

    /*---------------------------------------------*/
    /*--------------------잠금 관리 API-------------------------*/

    /**
     * 잠금 해제 API (페이지 이탈 시 호출)
     */
    @PostMapping("/edit/unlock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unlock(@RequestParam(required = false) String fundCode, HttpSession session) {
        if (fundCode == null || fundCode.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "fundCode가 없습니다."
            ));
        }
        
        String sessionId = session.getId();
        boolean success = editLockService.unlock(fundCode, sessionId);
        
        return ResponseEntity.ok(Map.of(
            "success", success,
            "message", success ? "잠금이 해제되었습니다." : "잠금 해제에 실패했습니다."
        ));
    }
    
    /**
     * 잠금 해제 API (GET 방식도 지원 - sendBeacon용)
     */
    @GetMapping("/edit/unlock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unlockGet(@RequestParam(required = false) String fundCode, HttpSession session) {
        if (fundCode == null || fundCode.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "fundCode가 없습니다."
            ));
        }
        
        String sessionId = session.getId();
        boolean success = editLockService.unlock(fundCode, sessionId);
        
        return ResponseEntity.ok(Map.of(
            "success", success,
            "message", success ? "잠금이 해제되었습니다." : "잠금 해제에 실패했습니다."
        ));
    }

    /**
     * 잠금 갱신 API (30분 자동 해제 방지)
     */
    @PostMapping("/edit/keep-lock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> keepLock(@RequestParam String fundCode, HttpSession session) {
        String sessionId = session.getId();
        boolean success = editLockService.keepLock(fundCode, sessionId);
        
        return ResponseEntity.ok(Map.of(
            "success", success,
            "message", success ? "잠금이 갱신되었습니다." : "잠금 갱신에 실패했습니다."
        ));
    }

    /**
     * 잠금 상태 확인 API (여러 펀드의 잠금 상태를 한번에 확인)
     */
    @PostMapping("/pending/check-locks")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkLocks(@RequestParam("fundCodes") List<String> fundCodes, HttpSession session) {
        String sessionId = session.getId();

        Map<String, Boolean> lockStatusMap = new HashMap<>();
        
        for (String fundCode : fundCodes) {
            String lockCheck = editLockService.checkLock(fundCode, sessionId);
            // lockCheck가 null이 아니면 다른 사용자가 잠금 중
            boolean isLocked = lockCheck != null;
            lockStatusMap.put(fundCode, isLocked);
        }
        
        return ResponseEntity.ok(lockStatusMap);
    }

    /*------------------------------------------------*/
    /*--------------------예약----------------------------*/

    @PostMapping("/reserve")
    public String setFundReserveTime(
        @RequestParam String fundCode,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime date) {
        adminFundService.setFundReserveTime(fundCode, date);
        return "redirect:/admin/product/pending";
    }

}
