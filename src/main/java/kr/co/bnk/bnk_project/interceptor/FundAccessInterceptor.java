package kr.co.bnk.bnk_project.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.bnk.bnk_project.security.MyUserDetails;
import kr.co.bnk.bnk_project.service.InvestmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
@RequiredArgsConstructor
public class FundAccessInterceptor implements HandlerInterceptor {

    private final InvestmentService investmentService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 현재 로그인된 사용자 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 로그인 여부 및 사용자 타입 확인 (일반 회원만 검사)
        if (auth != null && auth.getPrincipal() instanceof MyUserDetails) {
            MyUserDetails userDetails = (MyUserDetails) auth.getPrincipal();
            Long custNo = userDetails.getUserDTO().getCustNo();

            // Service를 통해 투자성향분석 유효성 체크
            boolean isValid = investmentService.isRiskTestValid(custNo);

            // 유효하지 않다면(분석 안함 or 만료됨) -> 리다이렉트
            if (!isValid) {
                // 알림창을 띄우기 위해 파라미터 전달 (?error=need_survey)
                response.sendRedirect("/bnk/member/survey?alert=true");
                return false; // 컨트롤러 진입 차단
            }
        }

        // 유효하면 통과
        return true;
    }
}