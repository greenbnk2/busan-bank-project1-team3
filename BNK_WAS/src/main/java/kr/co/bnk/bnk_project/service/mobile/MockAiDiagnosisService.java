package kr.co.bnk.bnk_project.service.mobile;
import kr.co.bnk.bnk_project.dto.mobile.MockUserInvestmentDto;
import kr.co.bnk.bnk_project.mapper.mobile.MockInvestmentMapper;
import kr.co.bnk.bnk_project.service.GeminiService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MockAiDiagnosisService {

    private final MockInvestmentMapper mockInvestmentMapper;
    private final GeminiService geminiService;

    public MockAiDiagnosisService(MockInvestmentMapper mockInvestmentMapper, GeminiService geminiService) {
        this.mockInvestmentMapper = mockInvestmentMapper;
        this.geminiService = geminiService;
    }

    public String getMockAiPortfolioReport(String userId) {
        // DB에서 사용자의 전체 모의투자 포트폴리오 조회
        List<MockUserInvestmentDto> portfolioList = mockInvestmentMapper.getMockUserPortfolio(userId);

        if (portfolioList == null || portfolioList.isEmpty()) {
            return "현재 보유 중인 모의투자 내역이 없어 분석이 불가능합니다.";
        }

        // 여러 개의 펀드 내역을 하나의 텍스트로 요약 (프롬프트 데이터화)
        StringBuilder fundListText = new StringBuilder();
        for (MockUserInvestmentDto fund : portfolioList) {
            fundListText.append(String.format("- 펀드명: %s (현재수익률: %.2f%%)\n",
                    fund.getFundName(), fund.getCurrentReturn()));
        }

        // 기본 정보 추출 (리스트의 첫 번째 항목에서 가져옴)
        String userName = portfolioList.get(0).getUserName();
        String propensity = portfolioList.get(0).getPropensity();

        // 종합 진단을 위한 프롬프트 구성
        String prompt = String.format(
                "당신은 OASIS펀드 의 AI 모의투자 자문위원 'OASIS'입니다.\n" +
                        "다음 고객의 전체 포트폴리오를 분석하여 전문적인 종합 리포트를 작성해 주세요.\n\n" +
                        "[고객 정보]\n" +
                        "- 고객명: %s\n" +
                        "- 투자 성향: %s\n\n" +
                        "[현재 보유 펀드 리스트]\n%s\n" +
                        "[요청 사항]\n" +
                        "1. 보유한 각 펀드의 수익률을 기반으로 전체적인 포트폴리오의 건강 상태를 진단할 것.\n" +
                        "2. 고객의 투자 성향(%s)과 현재 펀드 구성이 얼마나 조화로운지 분석할 것.\n" +
                        "3. 분산 투자 관점에서 보완할 점이나 향후 유지/매도 전략을 추천할 것.\n" +
                        "4. 신뢰감 있는 전문가 말투(~합니다)를 사용하고 본론만 명확히 작성할 것.",
                userName, propensity, fundListText.toString(), propensity
        );

        // 5. Gemini API 호출
        return geminiService.getAnalysis(prompt);
    }
}