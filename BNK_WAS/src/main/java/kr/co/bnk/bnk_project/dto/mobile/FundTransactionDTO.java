package kr.co.bnk.bnk_project.dto.mobile;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FundTransactionDTO {

    private Long trxId;              // 체결 내역 고유 ID
    private Long orderId;            // 원본 주문 ID
    private String type;             // 체결 유형 (BUY/SELL)
    private LocalDateTime tradeAt;   // 실제 체결(반영) 일자
    private Long amount;             // 체결된 금액
    private BigDecimal unit;         // 체결 좌수(지분)
    private BigDecimal nav;          // 적용 기준가(NAV)

}

