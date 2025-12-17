package kr.co.bnk.bnk_project.api;

import jakarta.servlet.http.HttpServletRequest;
import kr.co.bnk.bnk_project.dto.LoginRequest;
import kr.co.bnk.bnk_project.dto.LoginHistoryDTO;
import kr.co.bnk.bnk_project.dto.BnkUserDTO;
import kr.co.bnk.bnk_project.mapper.LoginHistoryMapper;
import kr.co.bnk.bnk_project.security.JwtUtil;
import kr.co.bnk.bnk_project.security.MyUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class MobileAuthController {

    @Qualifier("apiAuthManager")
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final LoginHistoryMapper loginHistoryMapper;


    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {  System.out.println("🔥 /api/auth/login 진입");
        try {
            // 1️⃣ Spring Security로 인증 (UserDetailsService + BCrypt 자동 사용)
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getUserId(),
                                    request.getPassword()
                            )
                    );

            // 2️⃣ 인증 성공 → 사용자 정보
            MyUserDetails userDetails =
                    (MyUserDetails) authentication.getPrincipal();
            BnkUserDTO user = userDetails.getUserDTO();

            // 3️⃣ 로그인 이력 저장 (기존 로직 재사용 👍)
            LoginHistoryDTO history = new LoginHistoryDTO();
            history.setCustNo(user.getCustNo());
            history.setIpAddr(getClientIp(httpRequest));
            loginHistoryMapper.insertLoginHistory(history);

            // 4️⃣ JWT 발급
            String token = jwtUtil.createToken(user.getUserId());

            // 5️⃣ JSON 응답
            return ResponseEntity.ok(
                    Map.of("accessToken", token)
            );

        } catch (AuthenticationException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("아이디 또는 비밀번호가 올바르지 않습니다");
        }
    }

    // IP 추출 (기존 방식 축소 버전)
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {

        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(
                Map.of(
                        "userId", userDetails.getUsername(),
                        "name", userDetails.getDisplayName()
                )
        );
    }
}
