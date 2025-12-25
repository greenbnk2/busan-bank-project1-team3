package kr.co.bnk.bnk_project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Date;

@Data
public class ProductDTO {

    /*test 테이블에 있는 것들*/
    private String fundName;   // 펀드이름
    private Double perf1M;     // 1개월 수익률
    private Double perf3M;     // 3개월 수익률
    private Double perf6M;     // 6개월 수익률
    private Double perf12M;    // 1년 수익률// 위험구분 (매우높음위험 등)
    // [2024-12-24 수정] int -> Integer로 변경 (null 값 처리 가능하도록)
    private Integer upfrontFEE; // 선취수수료 (nullable)

    /*fund master에 있는 테이블들 */
    private String fundcode;
    private String fundshortcode;
    private String fundNm;
    private String overview;
    private String fundfeature;
    private String investgrade;
    private String asset;
    private String classname;
    // [2024-12-24 추가] Flutter 앱을 위한 JSON 날짜 형식 지정
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date regdate;
    private String admincompany;

    private String doctype;
    private String docurl;
    private String docfile;


    private String trustManagement; // 태그/운용전략 (#배당주, #인덱스 등)
    private String investRegion;    // 투자지역 (국내/해외)
    private String fundType;        // 펀드유형 (주식형/채권형)
    private String assetManagerId;  // 운용사명 (신한자산운용 등)

    private Double currentNav;
    private Double nav;
    private Double nav1M;
    private Double nav3M;
    private Double nav6M;
    private Double nav12M;
    // [2024-12-24 추가] Flutter 앱을 위한 JSON 날짜 형식 지정
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date tradeDate;

    private String label;
    private Double value;


}
