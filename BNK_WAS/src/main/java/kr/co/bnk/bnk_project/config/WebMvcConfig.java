package kr.co.bnk.bnk_project.config;

import org.springframework.beans.factory.annotation.Value;
import kr.co.bnk.bnk_project.interceptor.FundAccessInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/*
    날짜 : 2025/11/26
    이름 : 이종봉
    내용 : 약관, 투자설명서, 간이투자설명서 웹띄워서 확인. 로컬 테스트완료. 배포는 추후.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.doc-path}")
    private String filePath;
    private final FundAccessInterceptor fundAccessInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 약관(terms)
        registry.addResourceHandler("/upload/terms/**")
                .addResourceLocations("file:" + filePath + "/terms/");

        // 투자설명서(invest)
        registry.addResourceHandler("/upload/invest/**")
                .addResourceLocations("file:" + filePath + "/invest/");

        // 간이설명서(summary)
        registry.addResourceHandler("/upload/summary/**")
                .addResourceLocations("file:" + filePath + "/summary/");

        // 로고 전용 경로(logo)
        registry.addResourceHandler("/upload/logo/**")
                .addResourceLocations("file:///home/rsa-key-20251117/upload/logo/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(fundAccessInterceptor)
                .addPathPatterns("/fund/**") // (1) 검사할 경로: 펀드 하위 모든 페이지
                .excludePathPatterns(        // (2) 검사에서 제외할 경로
                        "/fund/fundGuide",       // 이용 가이드는 누구나 봐야 함
                        "/fund/fundInformation", // 정보 센터도 공개
                        "/member/survey/**",     // 설문 페이지는 막으면 안 됨 (무한 루프 방지)
                        "/css/**", "/js/**", "/images/**", "/files/**" // 정적 리소스 및 업로드 파일 제외
                );
    }

    /* ====================================================================
     * [2024-12-24 추가] Flutter 앱을 위한 CORS 설정
     * ====================================================================
     * Flutter 앱에서 API 호출을 허용하기 위한 CORS 설정
     * /api/** 경로에 대한 모든 origin 허용 (개발용)
     * 운영환경에서는 Flutter 앱의 실제 도메인으로 변경 필요
     * ==================================================================== */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*") // 개발용: 모든 origin 허용 (운영환경에서는 Flutter 앱의 실제 도메인으로 변경)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
    /* ====================================================================
     * [2024-12-24 추가] 끝
     * ==================================================================== */
}

