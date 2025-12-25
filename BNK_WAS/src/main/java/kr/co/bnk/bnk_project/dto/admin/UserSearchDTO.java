package kr.co.bnk.bnk_project.dto.admin;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserSearchDTO {

    private Long custNo;
    private String custId;
    private String custName;
    private String custHp;
    private String custEmail;
    private String statusCode;
    private LocalDate joinDate;

}
