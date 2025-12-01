package kr.co.bnk.bnk_project.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;
    private final AdminLoginSuccessHandler adminLoginSuccessHandler;  // 추가

    // @Qualifier를 사용하여 Bean 이름을 기준으로 정확히 주입. - UserDetailsService
    @Qualifier("adminSecurityService")
    private final UserDetailsService adminSecurityService;

    @Qualifier("userSecurityService")
    private final UserDetailsService userSecurityService;

    // PasswordEncoder는 공통으로 사용
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * @Order(1) : 관리자용 SecurityFilterChain (먼저 검사)
     * /admin/** 경로에 대한 모든 보안 설정을 담당.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. /admin/ 경로로 시작하는 모든 요청을 이 필터체인이 처리하도록 매칭
                .securityMatcher("/admin/**")

                // 2. /admin/** 경로에 대한 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login").permitAll() // 관리자 로그인 페이지는 모두 허용
                        .requestMatchers("/admin/monitor/**").hasRole("SAD")
                        .requestMatchers("/admin/cs/**").hasAnyRole("CS", "ADM", "SAD") // CS 페이지는 3가지 역할 모두
                        .requestMatchers("/admin/product").hasAnyRole("CS", "ADM", "SAD") // CS 페이지는 3가지 역할 모두
                        .requestMatchers("/admin/member/permission").hasAnyRole(  "SAD") // CS 페이지는 3가지 역할 모두
                        .requestMatchers("/admin/member/**").hasAnyRole("CS", "ADM", "SAD") // CS 페이지는 3가지 역할 모두
                        .requestMatchers("/admin/**").hasAnyRole("ADM", "SAD") // 그 외 관리자 페이지는 ADM, SAD만
                        .anyRequest().authenticated() // 혹시 모를 나머지 admin 경로는 인증만 되면 허용

                )

                // 3. ⭐️ AdminSecurityService를 사용하여 인증하도록 설정
                .userDetailsService(adminSecurityService)

                // 4. 관리자 전용 로그인 폼 설정
                .formLogin(form -> form
                        .loginPage("/admin/login") // 관리자 로그인 페이지 URL
                        .loginProcessingUrl("/admin/login") // 로그인 폼이 전송될 URL
                        .usernameParameter("adminId") // 로그인 폼의 아이디 필드 name (adminId)
                        .passwordParameter("password") // 로그인 폼의 비밀번호 필드 name (password)
                        .successHandler(adminLoginSuccessHandler)  // defaultSuccessUrl 대신 핸들러 사용
                        .failureUrl("/admin/login?error=true") // 로그인 실패 시 URL
                )

                // 5. 관리자 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout=true")
                );

        return http.build();
    }

    /**
     * @Order(2) : 일반 사용자용 SecurityFilterChain (나중에 검사)
     * /admin/ 경로를 *제외한* 모든 요청을 처리
     */
    @Bean
    @Order(2)
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. /admin/ 경로를 제외한 모든 경로를 이 필터체인이 처리
                .securityMatcher("/**")

                // 챗봇 용도 (필터 무시)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/chatbot/ask")
                )

                // 2. 경로별 권한 설정 (⭐️ 순서가 중요 ⭐️)
                .authorizeHttpRequests(auth -> auth

                        // 1순위: /admin/** 경로는 이 필터체인에서 무조건 거부. (adminFilterChain이 담당)
                        .requestMatchers("/admin/**").denyAll()

                        // 2순위: 인증이 필요한 사용자 전용 페이지 (예: 마이페이지, 펀드 가입 신청 등)
                        // ⭐️ 여기에 "로그인이 필요한" URL 패턴을 추가.
                        .requestMatchers("/my/**", "/fund/**", "/user/profile/**","/api/session/extend", "/member/survey/**").authenticated() // USER ROLE이 따로 없어서 로그인 하면 허용

                        // 3순위: 위 2개 외의 "모든" 요청은 허용 -> 공개 페이지가 자동으로 허용.
                        .anyRequest().permitAll()
                )

                // 3. ⭐️ UserSecurityService를 사용하여 인증하도록 설정
                .userDetailsService(userSecurityService)

                // 4. 일반 사용자 로그인 폼 설정
                .formLogin(form -> form
                        .loginPage("/member/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("userid")
                        .passwordParameter("userpw")
                        .successHandler(loginSuccessHandler)
                        .failureUrl("/member/login?error=true")
                )

                // 5. 일반 사용자 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true) //서버 세션 파기
                        .deleteCookies("JSESSIONID") //브라우저 JSESSIONID 쿠키 삭제
                );

        return http.build();
    }
}