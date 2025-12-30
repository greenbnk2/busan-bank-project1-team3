package kr.co.bnk.bnk_project.dto.mobile;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FundPositionDTO {

    private Long custNo;              // 고객 번호
    private String fundCode;          // 펀드 코드
    private BigDecimal holdUnit;      // 현재 보유 좌수
    private Long investedAmt;         // 총 매입 원금
    private BigDecimal avgCostNav;    // 평균 매입 기준가
    private LocalDateTime updatedAt;  // 마지막 갱신 일시

}

