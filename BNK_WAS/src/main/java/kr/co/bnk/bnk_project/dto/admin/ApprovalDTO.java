package kr.co.bnk.bnk_project.dto.admin;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalDTO {
    private Long apprNo;           // 시퀀스
    private String apprType;       // 승인 종류 (등록/수정 등)
    private String fundCode;       // 상품코드
    private String fundName;       // 상품명
    private String requester;      // 요청자
    private LocalDateTime requestTime;  // 요청 시간
    private String status;         // '대기'
    private String approver;       // 승인자
    private LocalDateTime approvalTime;
    private String requestReason;  // 요청 사유
    private String remarks;        // 비고
    private String updateStat;     // 추가: UPDATE_STAT

}
