package kr.co.bnk.bnk_project.dto;

import lombok.Data;

@Data
public class UserFundDTO {
    private String fundCode;
    private String fundName;        // 펀드명
    private String acctNo;          // 계좌번호
    private long purchaseAmount;    // 투자원금
    private long currentEvalAmount; // 평가금액
    private long taxAmount;         // 예상 세금 (수익의 15.4%)
    private long afterTaxAmount;    // 세후 평가금액 (불안액기준)
    private long profitAmount;      // 평가 손익 (금액방식)
    private double yieldPct;        // 수익률
    private String status;          // 상태
}
