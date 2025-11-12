package kr.co.bnk.bnk_project.security;

import kr.co.bnk.bnk_project.dto.BnkUserDTO;
import kr.co.bnk.bnk_project.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("userSecurityService")
@RequiredArgsConstructor
public class UserSecurityService implements UserDetailsService {

    private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String custId) throws UsernameNotFoundException {

        BnkUserDTO user = memberMapper.findByCustId(custId);

        if (user == null) {
            throw new UsernameNotFoundException("사용자 아이디를 찾을 수 없습니다: " + custId);
        }

        return new MyUserDetails(user);
    }
}