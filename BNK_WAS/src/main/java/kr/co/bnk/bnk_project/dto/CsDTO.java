package kr.co.bnk.bnk_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsDTO {

    private Long csId;
    private Long categoryId;

    private String title;
    private String question;
    private String answer;
    private String status;
    private String userId;

    private LocalDateTime createdAt;
    private LocalDateTime answeredAt;

    // 목록 화면에서 필요하면 카테고리 이름도 같이 가져오기용
    private String categoryName;
}
