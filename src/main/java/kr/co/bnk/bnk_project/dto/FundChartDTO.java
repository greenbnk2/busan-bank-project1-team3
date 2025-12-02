package kr.co.bnk.bnk_project.dto;

import lombok.Data;

@Data
public class FundChartDTO {
    private String baseDate;      // YYYY-MM-DD 문자열
    private Double navPerUnit;    // 기준가
}
