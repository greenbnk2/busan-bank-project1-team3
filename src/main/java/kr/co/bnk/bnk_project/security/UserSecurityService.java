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

    private final MemberMapper userMapper; // or Repository

    @Override
    public UserDetails loadUserByUsername(String userId)
            throws UsernameNotFoundException {

        System.out.println("loadUserByUsername : " + userId);


        BnkUserDTO user = userMapper.findByCustId(userId);

        if (user == null) {
            throw new UsernameNotFoundException("사용자 없음");
        }

        return new MyUserDetails(user);
    }
}