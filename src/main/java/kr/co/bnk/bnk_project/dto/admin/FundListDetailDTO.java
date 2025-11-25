package kr.co.bnk.bnk_project.dto.admin;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundListDetailDTO {

    /*
        날짜 : 2025/11/21
        이름 : 이종봉
        내용 : 펀드목록 돋보기
     */

    /* ---------------------------
     * 1) 요약 정보 (상단 Summary)
     * --------------------------- */
    private String fundCode;         // FUND_MASTER.FUND_CODE
    private String fundName;         // FUND_MASTER.FUND_NAME
    private Double initialNav;       // FUND_MASTER.INITIAL_NAV (기준가)
    private Double changeAmount;     // FUND_DAILY_HISTORY.DAILY_CHANGE_RATE (전일대비 금액)
    private Double dailyChangeRate;  // 전일대비 %
    private Double totalNav;         // FUND_DAILY_HISTORY.TOTAL_NAV (순자산총액)
    private String tradeDate;         // 기준일자

    /* ---------------------------
     * 2) 펀드 기본 정보 (FUND_MASTER)
     * --------------------------- */
    private String fundType;         // FUND_MASTER.FUND_TYPE 펀드유형, 구분
    private String investRegion;     // FUND_MASTER.INVEST_REGION 투자지역. 투자지역구분
    private String classifyCode;     // FUND_MASTER.CLASSIFY_CODE 분류코드
    private String setupDate;        // FUND_MASTER.SETUP_DATE 최초설정일

    private String publicPrivateType;    // FUND_MASTER.PUBLIC_PRIVATE_TYPE 공모/사모 구분
    private String operStatus;       // FUND_MASTER.OPER_STATUS 운용상태
    private String shortCode;        // FUND_MASTER.FUND_SHORT_CODE 단축코드
    private String isUnitType;       // FUND_MASTER.IS_UNIT.TYPE 추가/단위구분
    private String operPeriodType;   // FUND_MASTER.OPER_PERIOD_TYPE 신탁회계기간.신탁기간
    private String className;        // FUND_MASTER.CLASS_NAME 특성분류

    private String rgnType;   // 판매지역분류
    private String prfmType;  // 운용실적공시분류


    /* ---------------------------
     * 3) 관련보수 (FUND_FEE_STRUCTURE)
     * --------------------------- */
    private Double salesFeeRate;     // 판매보수
    private Double mgtFeeRate;       // 운용보수
    private Double trusteeFeeRate;   // 수탁보수
    private Double adminFeeRate;     // 일반사무관리보수
    private Double totalExpenseRatio;// 총비용비율(TER) , 총보수
    private Double totalFeeRate;     // 총 보수 합계


    /* ---------------------------
     * 4) 관련수수료 (FUND_FEE_STRUCTURE)
     * --------------------------- */
    private Double upfrontFee;       // 선취수수료
    private Double postFeeRate;      // 후취수수료

    /* ---------------------------
     * 5) 관련회사 정보
     * --------------------------- */
    private String operatorName;         // 자산운용사명 (ASSET_MANAGEMENT_COMPANY.OPERATOR_NAME)
    private String adminCompany;         // 일반사무관리회사 (FUND_MASTER.ADMIN_COMPANY)
    private String salesCompany;     // 판매회사 (FUND_MASTER.SALES_COMPANY)
    private String trusteeCompany;       // 수탁회사 (FUND_MASTER.TRUSTEE_COMPANY)
/*-----------------------------------------------------------------------------------------------------------------*/
    /* ---------------------------
     * 6) 회사 로고/개황/주요재무현황 정보 (ASSET_MANAGEMENT_COMPANY)
     * --------------------------- */
    private String operatorHp;           // 대표전화 . 운용사 대표번호
    private String ceoName;              // 대표자 . 대표명
    private String establishmentDate;    // 설립일. 회사설립일
    private Double totalAsset;           // 총자산
    private Double totalLiabilities;     // 총부채
    private Double capital;              // 자본금
    private Double equityCapital;        // 자기자본
    private String homepageUrl;          // 홈페이지. 홈페이지주소
    private String logo;                 // 회사 로고
    private String netIncome;            // 당기순이익
    private LocalDate lastModifiedDate;  // 기준일. 마지막수정일자
    private Double operatingCapital;    // 영업용순자본 = 총자산 - 총부채 (컬럼추가 x)


    /* ---------------------------
     * 7) 자산구성내역 (FUND_ASSET_ALLOCATION)
     * --------------------------- */
    //private List<AssetAllocationDTO> assetAllocations;

    /* ---------------------------
     * 8) 문서 정보 (FUND_DOCUMENTS)
     * --------------------------- */
    //private List<FundDocumentDTO> documents;

    /* ---------------------------
     * 9) 수익률 정보 (FUND_PERFORMANCE)
     * --------------------------- */
//    private Double perf1m;
//    private Double perf3m;
//    private Double perf6m;
//    private Double perfYtd;
//    private Double perfSinceSetup;
//    private Double perf12m;
//    private Double sharpeRatio;
//    private Double betaCoefficient;

}
