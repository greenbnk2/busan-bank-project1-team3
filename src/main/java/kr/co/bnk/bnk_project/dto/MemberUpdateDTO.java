package kr.co.bnk.bnk_project.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MemberUpdateDTO {

    private Long custNo; // DB 식별용 PK

    // === 수정 불가능한 정보 ===
    private String name;
    private String userId;
    private String email;
    private String birthdate;

    // === 수정 가능한 정보 ===

    @NotEmpty(message = "핸드폰 번호는 필수 항목입니다.")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "핸드폰 번호 형식이 올바르지 않습니다.")
    private String phone;

    @NotEmpty(message = "우편번호는 필수 항목입니다.")
    private String zipcode;

    @NotEmpty(message = "주소는 필수 항목입니다.")
    private String address1;

    @NotEmpty(message = "상세주소는 필수 항목입니다.")
    private String address2;
}