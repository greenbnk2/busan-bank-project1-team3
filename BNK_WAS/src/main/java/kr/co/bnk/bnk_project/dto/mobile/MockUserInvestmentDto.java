package kr.co.bnk.bnk_project.dto.mobile;

import lombok.Data;

@Data
public class MockUserInvestmentDto {
    private String userName;        // 사용자 이름
    private String propensity;      // 투자 성향 (예: 공격투자형, 안정추구형)
    private String fundName;        // 보유 펀드명
    private long purchaseAmount;    // 매수 금액 (모의투자)
    private double currentReturn;   // 현재 수익률 (%)
    private long evaluationAmount;  // 평가 금액 (모의투자)
}