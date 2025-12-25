package kr.co.bnk.bnk_project.dto.admin;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FundDocumentDTO {

    private String fundCode;
    private String docType; // 약관, 투자설명서, 간이투자설명서
    private String docUrl;  // upload/terms/xxx_terms.pdf 이런식
    private String docFileName;
    private LocalDate regDate;
}
