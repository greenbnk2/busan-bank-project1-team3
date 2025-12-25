package kr.co.bnk.bnk_project.dto.admin;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class FundSettlementHistoryDTO {


    private LocalDate settleDate;       // 결산/상환 기준일 . 신탁회계기초
    private LocalDate settleStartDate;  // 결산 기간 시작일 . 신탁회계기말
    private Double elapsedDays;          // 경과일수
    private Double settleNav;           // 결산 시점 기준 가격. 기준
    private Double settleTaxNav;        // 과표기준가격 . 과표
    private Double setupOriginAmount;   // 결산 시점 설정 원본 . 설정원본
    private String settleType;          // 구분명
}
