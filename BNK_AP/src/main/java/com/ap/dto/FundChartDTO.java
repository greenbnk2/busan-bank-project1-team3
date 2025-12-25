package com.ap.dto;

import lombok.Data;

@Data
public class FundChartDTO {
    private String baseDate;      // YYYY-MM-DD 문자열
    private Double navPerUnit;    // 기준가
}
