package kr.co.bnk.bnk_project.dto;

import lombok.Data;

@Data
public class LoginHistoryDTO {
    private Long recordNo;  // DB 시퀀스
    private Long custNo;    // 고객 번호
    private String ipAddr;  // 접속 IP
    private String logTime; // 로그 시간
}