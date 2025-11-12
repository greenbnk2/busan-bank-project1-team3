package kr.co.bnk.bnk_project.security;

import kr.co.bnk.bnk_project.dto.BnkUserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

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

    // --- 계정 상태 관련 (BnkUserDTO에 statusCode 필드가 있다면 활용 가능) ---

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
}
