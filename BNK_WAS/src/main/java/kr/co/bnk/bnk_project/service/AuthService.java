package kr.co.bnk.bnk_project.service;

import org.springframework.transaction.annotation.Transactional;
import kr.co.bnk.bnk_project.domain.User;
import kr.co.bnk.bnk_project.dto.SignupRequestDTO;
import kr.co.bnk.bnk_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTToken jwtTokenProvider;


    @Transactional
    public void signup(SignupRequestDTO request) {

        // 1. 아이디 중복 체크
        if (userRepository.existsByCustId(request.getCustId())) {
            throw new RuntimeException("이미 존재하는 아이디입니다");
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. User 생성
        User user = User.builder()
                .custId(request.getCustId())
                .password(encodedPassword)
                .name(request.getCustName())
                .phone(request.getCustHp())
                .email(request.getCustEmail())
                .zipCode(request.getZipCode())
                .addr1(request.getAddr1())
                .addr2(request.getAddr2())
                .gender(request.getGender())
                .build();

        // 4. 저장
        userRepository.save(user);
    }


    public String login(String username, String rawPassword) {

        System.out.println("==== 로그인 시도 ====");
        System.out.println("입력 username = " + username);
        System.out.println("입력 password = " + rawPassword);

        User user = userRepository.findByCustId(username)
                .orElseThrow(() -> {
                    System.out.println("❌ 사용자 조회 실패");
                    return new RuntimeException("존재하지 않는 사용자");
                });

        System.out.println("✅ 사용자 조회 성공");
        System.out.println("DB password = " + user.getPassword());

        boolean match = passwordEncoder.matches(rawPassword, user.getPassword());
        System.out.println(" passwordEncoder.matches 결과 = " + match);

        if (!match) {
            throw new RuntimeException("비밀번호 불일치");
        }


        String token = jwtTokenProvider.createToken(
                user.getCustId(),
                user.getRole()
        );

        System.out.println("✅ JWT 발급 성공");

        return token;
    }
}
