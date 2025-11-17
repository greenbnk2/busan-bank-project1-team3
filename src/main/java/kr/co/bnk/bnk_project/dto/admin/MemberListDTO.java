package kr.co.bnk.bnk_project.dto.admin;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberListDTO {

    private Long custNo;        // 고객 번호
    private String custId;      // 아이디
    private String custName;    // 이름
    private String custHp;      // 전화번호
    private String custEmail;   // 이메일
    private LocalDateTime joinDate; // 가입일
    private String statusCode;  // 상태 코드 (A/S/J/D)

    private String addr1;
    private String addr2;
    private String zipCode;

    // 화면용 추가 필드
    private String address;
    private String statusText;  // 상태 (정상 / 휴면 / 정지 / 탈퇴)
}
