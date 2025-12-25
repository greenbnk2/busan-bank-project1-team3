package kr.co.bnk.bnk_project.dto;

import lombok.Data;
import java.util.Date;

@Data
public class FundPriceDTO {
    private String fundCode;        // 펀드코드
    private Date tradeDate;         // 기준일자
    private double navPerUnit;      // 기준가격
    private double taxBaseNav;      // 과표기준가격
    private long setupOriginAmount; // 설정원본

    private double changeAmount;    // 전일대비 등락폭
    private double changeRate;      // 등락률 (%)
}