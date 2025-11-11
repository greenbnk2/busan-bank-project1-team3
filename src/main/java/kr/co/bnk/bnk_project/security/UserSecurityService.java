package kr.co.bnk.bnk_project.security;

import kr.co.bnk.bnk_project.dto.BnkUserDTO;
import kr.co.bnk.bnk_project.mapper.MemberMapper;
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

// "userSecurityService"라는 Bean 이름을 명시적으로 부여합니다.
@Service("userSecurityService")
@RequiredArgsConstructor
public class UserSecurityService implements UserDetailsService {

    private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String custId) throws UsernameNotFoundException {

        // 1. MemberMapper를 통해 DB에서 사용자 정보를 조회합니다. (이전 단계에서 MemberMapper에 findByCustId를 추가했습니다)
        BnkUserDTO user = memberMapper.findByCustId(custId);

        if (user == null) {
            // 사용자 정보가 없으면 예외를 발생시킵니다.
            throw new UsernameNotFoundException("사용자 아이디를 찾을 수 없습니다: " + custId);
        }

        // 계정 상태(status_code)를 확인하여 휴면/탈퇴 계정 로그인을 막을 수 있습니다.
        // if (!"A".equals(user.getStatusCode())) {
        //    throw new DisabledException("비활성화된 계정입니다.");
        // }

        List<GrantedAuthority> authorities = new ArrayList<>();

        // 2. bnk_user 테이블의 모든 사용자는 "ROLE_USER" 권한을 가집니다.
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // 3. Spring Security의 User 객체를 생성하여 반환합니다.
        return new User(user.getUserId(), user.getPassword(), authorities);
    }
}