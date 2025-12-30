package kr.co.bnk.bnk_project.service.mobile;

import kr.co.bnk.bnk_project.dto.mobile.FundOrderDTO;
import kr.co.bnk.bnk_project.mapper.mobile.FundOrderMapper;
import kr.co.bnk.bnk_project.util.HolidayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 금액 확정 배치 서비스
 * REQUESTED 상태의 주문을 FIXED 상태로 전환
 * 공휴일을 고려하여 다음 영업일에 확정
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FundOrderFixService {

    private final FundOrderMapper fundOrderMapper;

    /**
     * 금액 확정 배치 실행
     * 어제 이전에 신청한 주문 중 REQUESTED 상태인 주문을 금액 확정 처리
     */
    @Transactional
    public void fixOrders() {
        log.info("=== 금액 확정 배치 시작 ===");
        
        // 금액 확정 대상 주문 조회 (어제 이전에 신청한 REQUESTED 상태 주문)
        List<FundOrderDTO> ordersToFix = fundOrderMapper.selectOrdersToFix();
        
        if (ordersToFix.isEmpty()) {
            log.info("금액 확정 대상 주문이 없습니다.");
            return;
        }
        
        log.info("금액 확정 대상 주문 수: {}", ordersToFix.size());
        
        LocalDate today = LocalDate.now();
        LocalDate fixDate = HolidayUtil.getNextBusinessDay(today.minusDays(1)); // 어제의 다음 영업일
        
        int successCount = 0;
        int failCount = 0;
        
        for (FundOrderDTO order : ordersToFix) {
            try {
                // 금액 확정 처리
                // 실제로는 수수료 등을 계산하여 FIX_AMOUNT를 설정해야 함
                // 여기서는 간단히 REQ_AMOUNT를 그대로 사용
                Long fixAmount = order.getReqAmount();
                
                // 수수료 계산 (예: 선취수수료 1% 가정)
                // 실제로는 펀드별 수수료율을 조회하여 계산해야 함
                // fixAmount = order.getReqAmount() - (order.getReqAmount() * 수수료율 / 100);
                
                order.setFixAmount(fixAmount);
                order.setAmountFixAt(LocalDateTime.of(fixDate, LocalDateTime.now().toLocalTime()));
                
                fundOrderMapper.updateOrderStatusToFixed(order);
                
                log.info("주문 ID {} 금액 확정 완료: {}원", order.getOrderId(), fixAmount);
                successCount++;
                
            } catch (Exception e) {
                log.error("주문 ID {} 금액 확정 실패: {}", order.getOrderId(), e.getMessage());
                failCount++;
            }
        }
        
        log.info("=== 금액 확정 배치 완료: 성공 {}, 실패 {} ===", successCount, failCount);
    }

    /**
     * 특정 주문 금액 확정 처리 (테스트용)
     * 날짜 조건 없이 바로 처리
     * 
     * @param orderId 주문 ID
     * @throws IllegalArgumentException 주문을 찾을 수 없을 때
     * @throws IllegalStateException REQUESTED 상태가 아닐 때
     */
    @Transactional
    public void fixOrderById(Long orderId) {
        log.info("=== 특정 주문 금액 확정 처리: orderId={} ===", orderId);
        
        FundOrderDTO order = fundOrderMapper.getOrderById(orderId);
        
        if (order == null) {
            throw new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId);
        }
        
        if (!"REQUESTED".equals(order.getStatus())) {
            throw new IllegalStateException("REQUESTED 상태의 주문만 확정할 수 있습니다. 현재 상태: " + order.getStatus());
        }
        
        // 금액 확정 처리
        Long fixAmount = order.getReqAmount();
        order.setFixAmount(fixAmount);
        order.setAmountFixAt(LocalDateTime.now());
        
        fundOrderMapper.updateOrderStatusToFixed(order);
        
        log.info("주문 ID {} 금액 확정 완료: {}원", orderId, fixAmount);
    }
}

