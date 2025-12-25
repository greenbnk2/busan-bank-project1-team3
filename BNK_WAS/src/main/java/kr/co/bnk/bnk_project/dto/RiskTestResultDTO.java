package kr.co.bnk.bnk_project.dto;

import lombok.Data;

@Data
public class RiskTestResultDTO {
    private String testRunId;
    private Long custNo;
    private int totalScore;
    private String riskType;
}