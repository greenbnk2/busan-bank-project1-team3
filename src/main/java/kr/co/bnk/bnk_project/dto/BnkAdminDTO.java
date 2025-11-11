package kr.co.bnk.bnk_project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BnkAdminDTO {
    private Long adminNo;
    private String adminId;
    private String password;
    private String adminName;
    private String role; // SAD, ADM, CS
}