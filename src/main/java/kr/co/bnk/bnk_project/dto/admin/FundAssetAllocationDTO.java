package kr.co.bnk.bnk_project.dto.admin;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class FundAssetAllocationDTO {

    private String assetCategory;   // Category, Asset_type, item, bond_type
    private String assetName;       // stock, bond, kse, cd, gov_bond 등
    private Double weightPercent;   // 비중(%)
}
