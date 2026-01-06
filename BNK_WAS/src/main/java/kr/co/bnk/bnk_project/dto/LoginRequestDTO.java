package kr.co.bnk.bnk_project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequestDTO {

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

}
