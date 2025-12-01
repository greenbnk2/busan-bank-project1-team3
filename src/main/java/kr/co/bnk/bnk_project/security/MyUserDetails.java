package kr.co.bnk.bnk_project.security;

import kr.co.bnk.bnk_project.dto.BnkUserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class MyUserDetails implements UserDetails {

    private final BnkUserDTO userDTO;

    public MyUserDetails(BnkUserDTO userDTO) {
        this.userDTO = userDTO;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 모든 일반 사용자는 "ROLE_USER" 권한을 가집니다.
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return userDTO.getPassword(); // DTO에서 비밀번호 반환
    }

    @Override
    public String getUsername() {
        return userDTO.getUserId(); // DTO에서 아이디 반환
    }

    public String getDisplayName() {
        return userDTO.getName(); // DTO에서 실제 이름 반환
    }

    public BnkUserDTO getUserDTO() {
        return userDTO;
    }

    // --- 계정 상태 관련 ---

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부 (일단 true)
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부 (일단 true)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 여부 (일단 true)
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부 (일단 true)
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyUserDetails that = (MyUserDetails) o;
        // userDTO의 userId를 기준으로 같은지 비교
        return Objects.equals(userDTO.getUserId(), that.userDTO.getUserId());
    }

    @Override
    public int hashCode() {
        // userId를 기준으로 해시코드 생성
        return Objects.hash(userDTO.getUserId());
    }
}
