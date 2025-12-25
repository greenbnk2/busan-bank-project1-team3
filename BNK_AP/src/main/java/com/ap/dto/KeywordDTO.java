package com.ap.dto;

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
