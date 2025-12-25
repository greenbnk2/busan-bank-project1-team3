package kr.co.bnk.bnk_project.controller.mobile;

import kr.co.bnk.bnk_project.dto.mobile.MockAccountDTO;
import kr.co.bnk.bnk_project.mapper.mobile.MockAccountMapper;
import kr.co.bnk.bnk_project.service.mobile.MockInvestmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mock/account")
public class MockInvestmentController {


    private final MockInvestmentService mockInvestmentService;
    private final MockAccountMapper mockAccountMapper;

    // 계좌 보유 여부 확인
    @GetMapping("/check/{custNo}")
    public ResponseEntity<Map<String, Object>> checkAccount(@PathVariable Long custNo) {
        Map<String, Object> response = new HashMap<>();
        MockAccountDTO account = mockInvestmentService.getAccountByCustNo(custNo);

        if (account != null) {
            response.put("hasAccount", true);
            response.put("account", account);
        } else {
            response.put("hasAccount", false);
        }
        return ResponseEntity.ok(response);
    }

    // 투자성향
    @GetMapping("/risk-type/{custNo}")
    public ResponseEntity<Map<String, String>> getRiskType(@PathVariable Long custNo) {
        String riskType = mockInvestmentService.getRiskType(custNo);
        Map<String, String> response = new HashMap<>();
        response.put("riskType", riskType != null ? riskType : "미설정");
        return ResponseEntity.ok(response);
    }

    // 모의투자 계좌 개설
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createAccount(@RequestBody MockAccountDTO dto) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean success = mockInvestmentService.createAccount(dto);
            if (success) {
                response.put("success", true);
                response.put("message", "모의투자 계좌가 성공적으로 개설되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "계좌 개설에 실패했습니다.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오류 발생: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary/{custNo}")
    public ResponseEntity<Map<String, Object>> getSummary(@PathVariable Long custNo) {
        // 기본 자산 정보 조회
        Map<String, Object> summary = mockAccountMapper.getDashboardSummary(custNo);

        // 보유 펀드 내역 조회
        List<Map<String, Object>> funds = mockAccountMapper.getHoldingsByCustNo(custNo);

        // 결과 합치기
        Map<String, Object> response = new HashMap<>();
        if (summary != null) {
            response.putAll(summary);
            response.put("funds", funds != null ? funds : new ArrayList<>());
        }

        return ResponseEntity.ok(response);
    }
}