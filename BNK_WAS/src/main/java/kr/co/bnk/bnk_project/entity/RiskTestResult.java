package kr.co.bnk.bnk_project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "RISK_TEST_RESULT")
@Getter
@NoArgsConstructor
public class RiskTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CUST_NO")
    private String custNo;

    @Column(name = "RISK_TYPE") // 투자 성향 (FUND_MASTER의 RISK_LEVEL과 매칭)
    private String riskType;

    @Column(name = "TEST_DATE")
    private LocalDateTime testDate;
}