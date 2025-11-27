package kr.co.bnk.bnk_project.dto;

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
    private int upfrontFEE; //

    /*fund master에 있는 테이블들 */
    private String fundcode;
    private String fundshortcode;
    private String fundNm;
    private String overview;
    private String fundfeature;
    private String investgrade;
    private String asset;
    private String classname;
    private Date regdate;
    private String admincompany;

    private String doctype;
    private String docurl;
    private String docfile;


}
