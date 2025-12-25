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

    private LocalDate tradeDate;              // 기준일
    private Double setupOriginAmount;         // 설정원본

    // 계산된 NAV 일일 수익률
    private Double navReturn;

    // 계산된 지수 수익률
    private Double kospiReturn;
    private Double kosdaqReturn;
    private Double govBondReturn;
    private Double corpBondReturn;


}
