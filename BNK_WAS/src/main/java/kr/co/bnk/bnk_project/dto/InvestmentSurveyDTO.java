package kr.co.bnk.bnk_project.dto;

import lombok.Data;

import java.util.List;

@Data
public class InvestmentSurveyDTO {
    private Integer q1, q2, q3, q4; // 단일 선택
    private List<Integer> q5;       // 다중 선택
    private Integer q6, q7, q8, q9, q10;
}
