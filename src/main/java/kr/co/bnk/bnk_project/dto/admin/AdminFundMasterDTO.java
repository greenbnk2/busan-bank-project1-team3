package kr.co.bnk.bnk_project.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FUND_MASTER + 운용사명 조인 결과를 담는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminFundMasterDTO {

    // FUND_MASTER 기본 정보
    private String fundCode;        // FUND_CODE
    private String fundShortCode;   // FUND_SHORT_CODE
    private String fundName;        // FUND_NAME

    private String fundType;        // FUND_TYPE (카테고리 코드)
    private String investRegion;    // INVEST_REGION
    private String investGrade;     // INVEST_GRADE
    private String fundFeature;     // FUND_FEATURE (CLOB -> String)

    private String operStatus;      // OPER_STATUS

    // 운용사 정보 (조인)
    private String assetManagerId;     // ASSET_MANAGER_ID
    private String assetManagerName;   // OPERATOR_NAME

    private String fundTypeName;       // 펀드유형명

}
