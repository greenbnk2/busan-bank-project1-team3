package kr.co.bnk.bnk_project.dto.admin;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundReturnHistoryDTO {

    private LocalDate calcDate;         // 기준일 .FUND_PERFORMANCE
    private Double setupAmount;         // 설정원본 .FUND_PERFORMANCE
    private Double fundReturn;          // 운용 수익률 .FUND_PERFORMANCE

    private Double bmKospi;             // 밴치마크 지수(KOSPI) .FUND_DAILY_HISTORY
    private Double bmKosdaq;            // 밴치마크 지수(KOSDAQ) .FUND_DAILY_HISTORY
    private Double bmGovBond;           // 밴치마크 지수(국공채) .FUND_DAILY_HISTORY
    private Double bmCorpBond;          // 밴치마크 지수(회사채) .FUND_DAILY_HISTORY


}
