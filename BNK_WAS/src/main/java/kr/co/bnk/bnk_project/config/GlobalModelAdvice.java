package kr.co.bnk.bnk_project.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    /**
     * 모든 요청에 대해, 모델(Model)에 "sessionTimeout" 속성을 자동으로 추가합니다.
     * @param session (자동으로 주입되는 현재 세션)
     * @return 세션 만료 시간(초)
     */

    @ModelAttribute("sessionTimeout")
    public Integer getSessionTimeout(HttpSession session) {
        // application.yml에서 설정한 세션 타임아웃 시간(초)을 반환.
        return session.getMaxInactiveInterval();
    }
}