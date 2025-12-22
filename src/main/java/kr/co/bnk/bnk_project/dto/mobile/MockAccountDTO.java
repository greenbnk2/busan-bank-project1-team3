package kr.co.bnk.bnk_project.dto.mobile;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MockAccountDTO {
    private String mockAcctNo;   // 모의투자 계좌번호
    private Long custNo;         // 고객 번호
    private String acctPass;     // 계좌 비밀번호
    private BigDecimal balance;   // 잔액
    private BigDecimal totalAsset; // 총 자산
    private LocalDateTime regDate;
    private String statusCode;
}