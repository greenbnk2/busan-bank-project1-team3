package kr.co.bnk.bnk_project.dto;

import lombok.Data;

@Data
public class KeywordDTO {
    private int keywordNo;
    private String keywordName;   // 예: 배당주
    private String relatedWords;  // 예: 월배당, 인컴, 연금
}