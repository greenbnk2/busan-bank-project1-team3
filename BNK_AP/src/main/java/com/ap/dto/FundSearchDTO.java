package com.ap.dto;

import lombok.Data;

@Data
public class FundSearchDTO {
    private Long custNo;        // 고객번호
    private String startDate;   // 조회 시작일
    private String endDate;     // 조회 종료일
    private String fundCode;    // 펀드코드
    private String searchType;  // 검색타입 (hold/all)
}
