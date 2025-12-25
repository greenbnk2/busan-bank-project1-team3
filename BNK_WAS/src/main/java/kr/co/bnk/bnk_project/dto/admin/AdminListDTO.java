package kr.co.bnk.bnk_project.dto.admin;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminListDTO {

    private Long adminNo;      // BNK_ADMIN.ADMIN_NO
    private String adminId;    // BNK_ADMIN.ADMIN_ID
    private String adminName;  // BNK_ADMIN.ADMIN_NAME
    private String role;       // SAD / ADM / CS

    // BNK_USER 조인 정보
    private String custHp;     // CUST_HP
    private String custEmail;  // CUST_EMAIL
    private LocalDate joinDate; // JOIN_DATE
}

