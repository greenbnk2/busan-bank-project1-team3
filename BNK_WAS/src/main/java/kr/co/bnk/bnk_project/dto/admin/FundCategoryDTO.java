package kr.co.bnk.bnk_project.dto.admin;

import lombok.Builder;
import lombok.Data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundCategoryDTO {

    /** CATEGORY_CODE (PK) */
    private String categoryCode;

    /** CATEGORY_NAME (이름) */
    private String categoryName;

    /** CATEGORY_TYPE (유형) */
    private String categoryType;

    /** SLUG */
    private String slug;

    /** STATUS (노출 상태, 기본값 on) */
    private String status;

    /** CATE_DESC (분류 설명) */
    private String cateDesc;


}