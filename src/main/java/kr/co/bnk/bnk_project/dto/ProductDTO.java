package kr.co.bnk.bnk_project.dto;

import lombok.Data;


@Data
public class ProductDTO {

    private String fundCode;
    private String fundName;   // 펀드이름
    private Double perf1M;     // 1개월 수익률
    private Double perf3M;     // 3개월 수익률
    private Double perf6M;     // 6개월 수익률
    private Double perf12M;    // 1년 수익률
    private String investGrade;   // 위험구분 (매우높음위험 등)
    private int upfrontFEE; //

    private String fundfeature;

}
