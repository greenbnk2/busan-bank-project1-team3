package kr.co.bnk.bnk_project.security;

import kr.co.bnk.bnk_project.dto.BnkAdminDTO;
import kr.co.bnk.bnk_project.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("adminSecurityService")
@RequiredArgsConstructor
public class AdminSecurityService implements UserDetailsService {

    private final AdminMapper adminMapper;

    @Override
    public UserDetails loadUserByUsername(String adminId) throws UsernameNotFoundException {

        // 1. AdminMapper를 통해 DB에서 관리자 정보를 조회합니다.
        BnkAdminDTO admin = adminMapper.findByAdminId(adminId);

        if (admin == null) {
            // 관리자 정보가 없으면 예외를 발생시킵니다.
            throw new UsernameNotFoundException("관리자 아이디를 찾을 수 없습니다: " + adminId);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        // 2. DB에 저장된 role(SAD, ADM, CS) 앞에 "ROLE_" 접두사를 붙여 권한 목록을 생성합니다.
        authorities.add(new SimpleGrantedAuthority("ROLE_" + admin.getRole()));

        // 3. Spring Security의 User 객체를 생성하여 반환합니다.
        // (아이디, 암호화된 비밀번호, 권한 목록)
        return new User(admin.getAdminId(), admin.getPassword(), authorities);
    }
}