package kr.co.bnk.bnk_project.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response, 
                                       Authentication authentication) throws IOException, ServletException {
        
        AdminUserDetails adminDetails = (AdminUserDetails) authentication.getPrincipal();
        String role = adminDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("");
        // 역할에 따라 다른 페이지로 리다이렉트
        if ("CS".equals(role)) {
            // CS 계정은 FAQ 페이지로
            response.sendRedirect("/bnk/admin/cs/faq");
        } else {
            // SAD, ADM 계정은 메인 페이지로
            response.sendRedirect("/bnk/admin/main");
        }
    }
}
