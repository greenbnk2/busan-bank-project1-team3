package kr.co.bnk.bnk_project.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.bnk.bnk_project.dto.BnkUserDTO;
import kr.co.bnk.bnk_project.dto.LoginHistoryDTO;
import kr.co.bnk.bnk_project.mapper.LoginHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final LoginHistoryMapper loginHistoryMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 로그인한 사용자 정보
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        BnkUserDTO user = userDetails.getUserDTO();

        // IP 주소 추출 (프록시/로드밸런서 고려)
        String ip = getClientIp(request);

        // DTO 생성 및 데이터 세팅
        LoginHistoryDTO history = new LoginHistoryDTO();
        history.setCustNo(user.getCustNo());
        history.setIpAddr(ip);

        // DB 저장
        loginHistoryMapper.insertLoginHistory(history);

        // 로그인 성공 후 이동할 페이지 (기존 defaultSuccessUrl("/") 역할)
        response.sendRedirect("/bnk");
    }

    // 클라이언트 IP 추출 유틸 메서드
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}