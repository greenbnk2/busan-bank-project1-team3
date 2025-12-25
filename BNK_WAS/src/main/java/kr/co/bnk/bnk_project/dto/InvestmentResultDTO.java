package kr.co.bnk.bnk_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvestmentResultDTO {
    private String type;      // 성향
    private int score;        // 점수
    private String description; // 설명
}
