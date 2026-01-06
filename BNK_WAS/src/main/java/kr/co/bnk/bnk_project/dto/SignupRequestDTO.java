package kr.co.bnk.bnk_project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDTO {

    @JsonProperty("custId")
    private String custId;

    @JsonProperty("password")
    private String password;

    @JsonProperty("custName")
    private String custName;

    @JsonProperty("custHp")
    private String custHp;

    @JsonProperty("custEmail")
    private String custEmail;

    @JsonProperty("zipCode")
    private String zipCode;

    @JsonProperty("addr1")
    private String addr1;

    @JsonProperty("addr2")
    private String addr2;

    @JsonProperty("gender")
    private String gender;
}