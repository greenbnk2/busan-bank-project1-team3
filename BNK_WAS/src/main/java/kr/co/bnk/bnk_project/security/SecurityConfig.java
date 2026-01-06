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
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;
    private final AdminLoginSuccessHandler adminLoginSuccessHandler;

    @Qualifier("adminSecurityService")
    private final UserDetailsService adminSecurityService;

    @Qualifier("userSecurityService")
    private final UserDetailsService userSecurityService;

    /* ===============================
       공통 PasswordEncoder
       =============================== */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /* ===============================
       1️⃣ 관리자 전용 Security
       =============================== */
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/admin/**")

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login").permitAll()
                        .requestMatchers("/admin/monitor/**").hasRole("SAD")
                        .requestMatchers("/admin/cs/**").hasAnyRole("CS", "ADM", "SAD")
                        .requestMatchers("/admin/product").hasAnyRole("CS", "ADM", "SAD")
                        .requestMatchers("/admin/member/permission").hasRole("SAD")
                        .requestMatchers("/admin/member/**").hasAnyRole("CS", "ADM", "SAD")
                        .requestMatchers("/admin/**").hasAnyRole("ADM", "SAD")
                        .anyRequest().authenticated()
                )

                .userDetailsService(adminSecurityService)

                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .usernameParameter("adminId")
                        .passwordParameter("password")
                        .successHandler(adminLoginSuccessHandler)
                        .failureUrl("/admin/login?error=true")
                )

                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout=true")
                );

        return http.build();
    }

    /* ===============================
       2️⃣ 웹 사용자 + Flutter API
       =============================== */
    @Bean
    @Order(2)
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/**")

                /* 🔥 Flutter API는 CSRF 제외 */
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/auth/**",
                                "/api/mock/**",
                                "/api/fund/**",
                                "/api/funds/**",
                                "/api/chatbot/**",
                                "/faq",
                                "/inquiry/**"
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        /* 🔥 Flutter 로그인/회원가입 */
                        .requestMatchers("/api/auth/**").permitAll()

                        /* Flutter 공개 API */
                        .requestMatchers("/api/mock/**").permitAll()
                        .requestMatchers("/api/funds/**").permitAll()

                        /* 파일 다운로드 */
                        .requestMatchers("/upload/**").permitAll()

                        /* admin은 여기서 차단 (1번 체인이 처리) */
                        .requestMatchers("/admin/**").denyAll()

                        /* 로그인 필요한 웹 페이지 */
                        .requestMatchers(
                                "/my/**",
                                "/fund/**",
                                "/user/profile/**",
                                "/api/session/extend",
                                "/member/survey/**"
                        ).authenticated()

                        /* 그 외 전부 공개 */
                        .anyRequest().permitAll()
                )

                .userDetailsService(userSecurityService)

                /* 웹 전용 formLogin */
                .formLogin(form -> form
                        .loginPage("/member/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("userid")
                        .passwordParameter("userpw")
                        .successHandler(loginSuccessHandler)
                        .failureUrl("/member/login?error=true")
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )

                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/member/login?expired=true")
                );

        return http.build();
    }

    /* ===============================
       세션 동시접속 제어용
       =============================== */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
