package kr.co.bnk.bnk_project.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeywordDTO {

    private String keywordNo;
    private String keywordName;
    private String RegDate;
    private String relatedWords;

}
