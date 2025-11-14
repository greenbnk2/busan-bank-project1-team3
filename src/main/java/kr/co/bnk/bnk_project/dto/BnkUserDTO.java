package kr.co.bnk.bnk_project.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BnkUserDTO {

    // ===== DB 테이블에서 직접 사용되는 필드 =====
    private Long custNo;        // bnk_user (PK - <selectKey>로 채워짐)

    // ===== register.html 폼 필드 =====

    // 개인정보
    private String name;              // 성명
    private String userId;            // 회원아이디
    private String password;          // 비밀번호
    private String passwordConfirm;   // 비밀번호 확인
    private String phone;             // 핸드폰번호
    private String phoneSms;          // 전화수신거부

    // 주소
    private String zipcode;           // 우편번호
    private String address1;          // 주소1
    private String address2;          // 주소2

    // 이메일
    private String emailId;           // 이메일 아이디
    private String emailDomain;       // 이메일 도메인
    private String emailSms;          // 이메일수신거부

    // 계좌정보
    private String accountNumber;     // 계좌번호
    private String accountPassword;   // 계좌비밀번호

    // 직장정보
    private String hasJob;            // 직장정보입력구분
    private String companyName;       // 직장명
    private String jobType;           // 직업구분
    private String department;        // 부서명
    private String position;          // 직위
    private String jobZipcode;        // 직장 우편번호
    private String jobAddress1;       // 직장 주소1
    private String jobAddress2;       // 직장 주소2

    // 부가정보
    private String birthdate;         // 생년월일
    private String calendarType;      // 양력/음력
    private String childCount;        // 자녀수
    private String houseOwnership;    // 주택소유
    private String houseType;         // 주택종류
    private String hasCar;            // 자동차소유
    private String carNumber;         // 차량번호

    // 이메일 조합 메서드 - 조회시 사용
    public String getFullEmail() {
        return emailId + "@" + emailDomain;
    }

    // 전체 주소 조합 메서드 - 조회시 사용
    public String getFullAddress() {
        return String.format("(%s) %s %s", zipcode, address1, address2);
    }
}
