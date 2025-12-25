package com.ap.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "FUND_MASTER")
@Getter
@NoArgsConstructor
public class FundMaster {

    @Id
    @Column(name = "FUND_CODE") // 펀드 코드/ID
    private String fundId;

    @Column(name = "FUND_NAME") // 펀드명
    private String fundName;

    @Column(name = "INVEST_GRADE") // 위험도 (투자 성향과 매칭될 값, 예: "고위험", "중위험")
    private String investGrade;

    @Column(name = "FUND_FEATURE") // 상품 특징/설명
    private String fundFeature;

}
