package kr.co.bnk.bnk_project.controller.mobile;

import kr.co.bnk.bnk_project.service.mobile.MockAiDiagnosisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OASIS - AI 모의투자 진단 전용 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mock/ai") // AI 관련 기능을 위한 전용 경로
public class MockAiController {

    private final MockAiDiagnosisService mockAiDiagnosisService;

    // AI 모의투자 진단 리포트 생성
    @GetMapping("/report/{custNo}")
    public ResponseEntity<String> getMockInvestmentReport(@PathVariable Long custNo) {
        try {
            // 서비스 계층으로 전달 시 String으로 변환하여 전달
            String report = mockAiDiagnosisService.getMockAiPortfolioReport(String.valueOf(custNo));

            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error(e.getMessage());
            // 에러 발생 시 상세 메시지를 포함하여 반환
            return ResponseEntity.internalServerError()
                    .body("AI 리포트를 생성하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}