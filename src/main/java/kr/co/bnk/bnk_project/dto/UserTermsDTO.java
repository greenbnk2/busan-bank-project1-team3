package kr.co.bnk.bnk_project.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserTermsDTO {

    private String termId;
    private String title;
    private String content;
    private LocalDateTime regDate;
}
