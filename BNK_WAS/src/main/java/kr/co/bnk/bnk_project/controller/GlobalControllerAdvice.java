package kr.co.bnk.bnk_project.controller;

import kr.co.bnk.bnk_project.dto.KeywordDTO;
import kr.co.bnk.bnk_project.service.FundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final FundService fundService;

    // 모든 페이지가 열릴 때마다 'keywordList'라는 이름으로 추천 키워드를 자동으로 모델에 담음
    @ModelAttribute("keywordList")
    public List<KeywordDTO> globalKeywords() {
        try {
            // DB에서 추천 키워드 5개 가져오기
            return fundService.getRecommendedKeywords();
        } catch (Exception e) {
            // DB 에러가 나더라도 페이지는 떠야 하므로 null 반환 (로그만 남김)
            System.out.println("추천 키워드 로딩 실패: " + e.getMessage());
            return null;
        }
    }
}