package kr.co.bnk.bnk_project.service.mobile;

import kr.co.bnk.bnk_project.dto.mobile.FundOrderDTO;
import kr.co.bnk.bnk_project.dto.mobile.FundPositionDTO;
import kr.co.bnk.bnk_project.dto.mobile.FundTransactionDTO;
import kr.co.bnk.bnk_project.mapper.FundMapper;
import kr.co.bnk.bnk_project.mapper.mobile.FundOrderMapper;
import kr.co.bnk.bnk_project.mapper.mobile.FundPositionMapper;
import kr.co.bnk.bnk_project.mapper.mobile.FundTransactionMapper;
import kr.co.bnk.bnk_project.util.HolidayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 투자 시작 배치 서비스
 * FIXED 상태의 주문을 STARTED 상태로 전환하고 체결 내역 생성
 * 공휴일을 고려하여 다음 영업일에 투자 시작
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FundOrderStartService {

    private final FundOrderMapper fundOrderMapper;
    private final FundTransactionMapper fundTransactionMapper;
    private final FundPositionMapper fundPositionMapper;
    private final FundMapper fundMapper;

    /**
     * 투자 시작 배치 실행
     * 어제 이전에 확정된 주문 중 FIXED 상태인 주문을 투자 시작 처리
     */
    @Transactional
    public void startInvestments() {
        log.info("=== 투자 시작 배치 시작 ===");
        
        // 투자 시작 대상 주문 조회 (어제 이전에 확정된 FIXED 상태 주문)
        List<FundOrderDTO> ordersToStart = fundOrderMapper.selectOrdersToStart();
        
        if (ordersToStart.isEmpty()) {
            log.info("투자 시작 대상 주문이 없습니다.");
            return;
        }
        
        log.info("투자 시작 대상 주문 수: {}", ordersToStart.size());
        
        LocalDate today = LocalDate.now();
        LocalDate startDate = HolidayUtil.getNextBusinessDay(today.minusDays(1)); // 어제의 다음 영업일
        
        int successCount = 0;
        int failCount = 0;
        
        for (FundOrderDTO order : ordersToStart) {
            try {
                // 1. 현재 NAV 조회
                BigDecimal currentNav = getCurrentNav(order.getFundCode());
                
                if (currentNav == null || currentNav.compareTo(BigDecimal.ZERO) <= 0) {
                    log.error("주문 ID {} NAV 조회 실패 또는 0 이하", order.getOrderId());
                    failCount++;
                    continue;
                }
                
                // 2. 좌수 계산 (확정 금액 / NAV)
                BigDecimal unit = BigDecimal.valueOf(order.getFixAmount())
                        .divide(currentNav, 8, RoundingMode.DOWN);
                
                // 3. 체결 내역 생성
                FundTransactionDTO transaction = new FundTransactionDTO();
                transaction.setTrxId(fundTransactionMapper.getNextTrxId());
                transaction.setOrderId(order.getOrderId());
                transaction.setType("BUY");
                transaction.setTradeAt(LocalDateTime.of(startDate, LocalDateTime.now().toLocalTime()));
                transaction.setAmount(order.getFixAmount());
                transaction.setUnit(unit);
                transaction.setNav(currentNav);
                
                fundTransactionMapper.insertFundTransaction(transaction);
                
                // 4. 포지션 업데이트
                updatePosition(order.getCustNo(), order.getFundCode(), unit, order.getFixAmount(), currentNav);
                
                // 5. 주문 상태를 STARTED로 변경
                order.setStartAt(LocalDateTime.of(startDate, LocalDateTime.now().toLocalTime()));
                fundOrderMapper.updateOrderStatusToStarted(order);
                
                log.info("주문 ID {} 투자 시작 완료: {}원, {}좌, NAV {}", 
                        order.getOrderId(), order.getFixAmount(), unit, currentNav);
                successCount++;
                
            } catch (Exception e) {
                log.error("주문 ID {} 투자 시작 실패: {}", order.getOrderId(), e.getMessage(), e);
                failCount++;
            }
        }
        
        log.info("=== 투자 시작 배치 완료: 성공 {}, 실패 {} ===", successCount, failCount);
    }

    /**
     * 현재 NAV 조회
     */
    private BigDecimal getCurrentNav(String fundCode) {
        try {
            Double nav = fundMapper.getCurrentNav(fundCode);
            if (nav == null) {
                log.error("NAV 조회 결과 null: fundCode={}", fundCode);
                return null;
            }
            return BigDecimal.valueOf(nav);
        } catch (Exception e) {
            log.error("NAV 조회 실패: fundCode={}, error={}", fundCode, e.getMessage());
            return null;
        }
    }

    /**
     * 포지션 업데이트 (INSERT 또는 UPDATE)
     */
    private void updatePosition(Long custNo, String fundCode, BigDecimal newUnit, Long newAmount, BigDecimal nav) {
        FundPositionDTO existingPosition = fundPositionMapper.getPosition(custNo, fundCode);
        
        if (existingPosition == null) {
            // 신규 포지션 생성
            FundPositionDTO newPosition = new FundPositionDTO();
            newPosition.setCustNo(custNo);
            newPosition.setFundCode(fundCode);
            newPosition.setHoldUnit(newUnit);
            newPosition.setInvestedAmt(newAmount);
            newPosition.setAvgCostNav(nav);
            newPosition.setUpdatedAt(LocalDateTime.now());
            
            fundPositionMapper.insertFundPosition(newPosition);
        } else {
            // 기존 포지션 업데이트 (평균 단가 재계산)
            BigDecimal totalUnit = existingPosition.getHoldUnit().add(newUnit);
            Long totalAmount = existingPosition.getInvestedAmt() + newAmount;
            
            // 평균 단가 = 총 투자금액 / 총 좌수
            BigDecimal avgNav = BigDecimal.valueOf(totalAmount)
                    .divide(totalUnit, 8, RoundingMode.HALF_UP);
            
            existingPosition.setHoldUnit(totalUnit);
            existingPosition.setInvestedAmt(totalAmount);
            existingPosition.setAvgCostNav(avgNav);
            existingPosition.setUpdatedAt(LocalDateTime.now());
            
            fundPositionMapper.updateFundPosition(existingPosition);
        }
    }

    /**
     * 특정 주문 투자 시작 처리 (테스트용)
     * 날짜 조건 없이 바로 처리
     * 
     * @param orderId 주문 ID
     * @throws IllegalArgumentException 주문을 찾을 수 없을 때
     * @throws IllegalStateException FIXED 상태가 아니거나 확정 금액이 없을 때
     */
    @Transactional
    public void startInvestmentById(Long orderId) {
        log.info("=== 특정 주문 투자 시작 처리: orderId={} ===", orderId);
        
        FundOrderDTO order = fundOrderMapper.getOrderById(orderId);
        
        if (order == null) {
            throw new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId);
        }
        
        if (!"FIXED".equals(order.getStatus())) {
            throw new IllegalStateException("FIXED 상태의 주문만 시작할 수 있습니다. 현재 상태: " + order.getStatus());
        }
        
        if (order.getFixAmount() == null) {
            throw new IllegalStateException("확정 금액이 없습니다.");
        }
        
        // 1. 현재 NAV 조회
        BigDecimal currentNav = getCurrentNav(order.getFundCode());
        
        if (currentNav == null || currentNav.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("NAV 조회 실패 또는 0 이하: " + order.getFundCode());
        }
        
        // 2. 좌수 계산
        BigDecimal unit = BigDecimal.valueOf(order.getFixAmount())
                .divide(currentNav, 8, RoundingMode.DOWN);
        
        // 3. 체결 내역 생성
        FundTransactionDTO transaction = new FundTransactionDTO();
        transaction.setTrxId(fundTransactionMapper.getNextTrxId());
        transaction.setOrderId(order.getOrderId());
        transaction.setType("BUY");
        transaction.setTradeAt(LocalDateTime.now());
        transaction.setAmount(order.getFixAmount());
        transaction.setUnit(unit);
        transaction.setNav(currentNav);
        
        fundTransactionMapper.insertFundTransaction(transaction);
        
        // 4. 포지션 업데이트
        updatePosition(order.getCustNo(), order.getFundCode(), unit, order.getFixAmount(), currentNav);
        
        // 5. 주문 상태를 STARTED로 변경
        order.setStartAt(LocalDateTime.now());
        fundOrderMapper.updateOrderStatusToStarted(order);
        
        log.info("주문 ID {} 투자 시작 완료: {}원, {}좌, NAV {}", 
                orderId, order.getFixAmount(), unit, currentNav);
    }
}

