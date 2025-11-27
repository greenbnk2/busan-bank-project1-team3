package kr.co.bnk.bnk_project.dto.admin;

import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class FundPriceHistoryDTO {

    private LocalDate tradeDate;        // 기준일자 .FUND_DAILY_HISTORY
    private Double navPerUnit;          // 기준가격 .FUND_DAILY_HISTORY
    private Double dailyChangeRate;     // 전일대비등락 .FUND_DAILY_HISTORY
    private Double taxBaseNav;          // 일자별 과표기준가격 .FUND_DAILY_HISTORY
    private Double setupOriginAmount;   // 일자별 설정원본 .FUND_DAILY_HISTORY

    private Double bmKospi;             // KOSPI .FUND_DAILY_HISTORY
    private Double bmKospi200;          // KOSPI200 .FUND_DAILY_HISTORY
    private Double bmKosdaq;            // KOSDAQ .FUND_DAILY_HISTORY
    private Double bmGovBond;           // 국공채(3년만기) .FUND_DAILY_HISTORY
    private Double bmCorpBond;          // 회사채(3년만기) .FUND_DAILY_HISTORY



}
