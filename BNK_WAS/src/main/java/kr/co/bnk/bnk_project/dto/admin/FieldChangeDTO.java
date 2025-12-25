package kr.co.bnk.bnk_project.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldChangeDTO {
    private String fieldName;      // 필드명 (한글)
    private String oldValue;       // 기존 값 (FUND_MASTER)
    private String newValue;       // 변경 값 (FUND_MASTER_REVISION)
}


