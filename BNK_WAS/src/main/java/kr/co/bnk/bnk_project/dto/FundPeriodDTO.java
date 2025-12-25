package kr.co.bnk.bnk_project.dto;

import lombok.Data;

@Data
public class FundPeriodDTO {

    private String fundCode;

    private Double currentNav;  // 오늘 NAV
    private Double nav1M;
    private Double nav3M;
    private Double nav6M;
    private Double nav12M;

    private Double yield1M;
    private Double yield3M;
    private Double yield6M;
    private Double yield12M;
}
