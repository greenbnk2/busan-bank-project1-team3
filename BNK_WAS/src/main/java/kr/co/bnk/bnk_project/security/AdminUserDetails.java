package kr.co.bnk.bnk_project.security;

import kr.co.bnk.bnk_project.dto.BnkAdminDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class AdminUserDetails implements UserDetails {

    private final BnkAdminDTO adminDTO;

    public AdminUserDetails(BnkAdminDTO adminDTO) {
        this.adminDTO = adminDTO;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + adminDTO.getRole()));
    }

    @Override
    public String getPassword() {
        return adminDTO.getPassword();
    }

    @Override
    public String getUsername() {
        return adminDTO.getAdminId();
    }

    public String getDisplayName() {
        return adminDTO.getAdminName(); // ◀◀◀ 핵심 수정 사항
    }

    // --- 계정 상태 ---

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 여부
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부
    }
}