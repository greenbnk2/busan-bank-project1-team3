package kr.co.bnk.bnk_project.dto;

import jakarta.validation.constraints.*;
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
    @NotEmpty(message = "성명(실명)은 필수 항목입니다.")
    @Size(min = 2, max = 10, message = "성명은 2자에서 10자 사이여야 합니다.")
    private String name;              // 성명

    @NotEmpty(message = "회원아이디는 필수 항목입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "아이디는 6~20자의 영문 또는 숫자만 가능합니다.")
    private String userId;            // 회원아이디

    @NotEmpty(message = "비밀번호는 필수 항목입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 8자 이상이며, 문자, 숫자, 특수문자를 포함해야 합니다.")
    private String password;          // 비밀번호

    @NotEmpty(message = "비밀번호 확인은 필수 항목입니다.")
    private String passwordConfirm;   // 비밀번호 확인

    @NotEmpty(message = "성별은 필수 항목입니다.")
    private String gender; //성별

    @NotEmpty(message = "핸드폰 번호는 필수 항목입니다.")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "핸드폰 번호 형식이 올바르지 않습니다. (010-XXXX-XXXX)")
    private String phone;             // 핸드폰번호

    private String phoneSms;          // 전화수신거부

    // 주소
    @NotEmpty(message = "우편번호는 필수 항목입니다.")
    private String zipcode;           // 우편번호

    @NotEmpty(message = "주소는 필수 항목입니다.")
    private String address1;          // 주소1

    @NotEmpty(message = "상세주소는 필수 항목입니다.")
    private String address2;          // 주소2

    // 이메일
    @NotEmpty(message = "E-mail 아이디는 필수 항목입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9._+-]+$", message = "이메일 아이디 형식이 올바르지 않습니다.")
    private String emailId;           // 이메일 아이디

    @NotEmpty(message = "E-mail 도메인은 필수 항목입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "이메일 도메인 형식이 올바르지 않습니다.")
    private String emailDomain;       // 이메일 도메인

    private String emailSms;          // 이메일수신거부

    // 계좌정보
    @NotEmpty(message = "계좌번호는 필수 항목입니다.")
    private String accountNumber;     // 계좌번호

    @NotEmpty(message = "계좌비밀번호는 필수 항목입니다.")
    @Pattern(regexp = "^\\d{4}$", message = "계좌비밀번호는 4자리 숫자입니다.")
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
    @NotEmpty(message = "생년월일은 필수 항목입니다.")
    private String birthdate;         // 생년월일

    private String calendarType;      // 양력/음력
    private String childCount;        // 자녀수

    @NotEmpty(message = "주택소유 여부는 필수 항목입니다.")
    private String houseOwnership;    // 주택소유

    @NotEmpty(message = "주택종류는 필수 항목입니다.")
    private String houseType;         // 주택종류

    @NotEmpty(message = "자동차소유 여부는 필수 항목입니다.")
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
