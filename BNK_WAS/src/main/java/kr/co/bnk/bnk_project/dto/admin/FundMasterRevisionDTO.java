package kr.co.bnk.bnk_project.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundMasterRevisionDTO {
    private Long revId;                    // REV_ID
    private String fundCode;               // FUND_CODE
    private String fundShortCode;          // FUND_SHORT_CODE
    private String fundName;               // FUND_NAME
    private String assetManagerId;         // ASSET_MANAGER_ID
    private LocalDate setupDate;           // SETUP_DATE
    private BigDecimal initialNav;         // INITIAL_NAV
    private String fundType;               // FUND_TYPE
    private String investRegion;           // INVEST_REGION
    private String classifyCode;           // CLASSIFY_CODE
    private String publicPrivateType;      // PUBLIC_PRIVATE_TYPE
    private String trusteeCompany;         // TRUSTEE_COMPANY
    private String salesCompany;           // SALES_COMPANY
    private String adminCompany;           // ADMIN_COMPANY
    private String operPeriodType;         // OPER_PERIOD_TYPE
    private String isUnitType;             // IS_UNIT_TYPE
    private String investGrade;            // INVEST_GRADE
    private String fundFeature;            // FUND_FEATURE
    private String className;              // CLASS_NAME
    private String overview;               // OVERVIEW
    private String redemptionMethod;       // REDEMPTION_METHOD
    private String tradeMethod;            // TRADE_METHOD
    private String subscriptionMethod;     // SUBCRIPTION_METHOD
    private String trustManagement;        // TRUST_MANAGEMENT
    private String paymentId;              // PAYMENT_ID
    private String notice1;                // NOTICE_1
    private String notice2;                // NOTICE_2
    private String rgnType;                // RGN_TYPE
    private String prfmType;               // PRFM_TYPE
    private String updateStat;             // UPDATE_STAT
    private String revStatus;              // REV_STATUS
    private LocalDateTime applyAt;         // APPLY_AT (예약 시간)
    private LocalDateTime createdAt;       // CREATED_AT
    private String createdBy;              // CREATED_BY
    private LocalDateTime approvedAt;      // APPROVED_AT
    private String approvedBy;             // APPROVED_BY
    private LocalDateTime operStartAt;     // OPER_START_AT (FUND_MASTER에서 조인)
    private String reserveYn;              // RESERVE_YN (FUND_MASTER에서 조인)
}
