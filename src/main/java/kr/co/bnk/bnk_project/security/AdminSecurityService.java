package kr.co.bnk.bnk_project.security;

import kr.co.bnk.bnk_project.dto.BnkAdminDTO;
import kr.co.bnk.bnk_project.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;



@Service("adminSecurityService")
@RequiredArgsConstructor
public class AdminSecurityService implements UserDetailsService {

    private final AdminMapper adminMapper;

    @Override
    public UserDetails loadUserByUsername(String adminId) throws UsernameNotFoundException {

        // AdminMapper를 통해 DB에서 관리자 정보를 조회
        BnkAdminDTO admin = adminMapper.findByAdminId(adminId);

        if (admin == null) {
            // 관리자 정보가 없으면 예외 발생.
            throw new UsernameNotFoundException("관리자 아이디를 찾을 수 없습니다: " + adminId);
        }

        return new AdminUserDetails(admin);
    }
}