package kr.co.bnk.bnk_project.scheduler;

import kr.co.bnk.bnk_project.service.mobile.FundOrderFixService;
import kr.co.bnk.bnk_project.service.mobile.FundOrderStartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 펀드 주문 배치 스케줄러
 * 매일 오전 9시에 금액 확정 및 투자 시작 배치 실행
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FundOrderScheduler {

    private final FundOrderFixService fundOrderFixService;
    private final FundOrderStartService fundOrderStartService;

    /**
     * 금액 확정 배치
     * 매일 오전 9시에 실행
     * REQUESTED 상태의 주문을 FIXED 상태로 전환
     */
    @Scheduled(cron = "0 0 9 * * ?") // 매일 오전 9시
    public void fixOrders() {
        log.info("=== 금액 확정 배치 스케줄러 시작 ===");
        try {
            fundOrderFixService.fixOrders();
        } catch (Exception e) {
            log.error("금액 확정 배치 실행 중 오류 발생", e);
        }
        log.info("=== 금액 확정 배치 스케줄러 종료 ===");
    }

    /**
     * 투자 시작 배치
     * 매일 오전 9시 30분에 실행
     * FIXED 상태의 주문을 STARTED 상태로 전환하고 체결 내역 생성
     */
    @Scheduled(cron = "0 30 9 * * ?") // 매일 오전 9시 30분
    public void startInvestments() {
        log.info("=== 투자 시작 배치 스케줄러 시작 ===");
        try {
            fundOrderStartService.startInvestments();
        } catch (Exception e) {
            log.error("투자 시작 배치 실행 중 오류 발생", e);
        }
        log.info("=== 투자 시작 배치 스케줄러 종료 ===");
    }
}

