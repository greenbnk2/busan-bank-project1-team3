package kr.co.bnk.bnk_project.service;

import jakarta.transaction.Transactional;
import kr.co.bnk.bnk_project.dto.InvestmentResultDTO;
import kr.co.bnk.bnk_project.dto.InvestmentSurveyDTO;
import kr.co.bnk.bnk_project.dto.RiskTestResultDTO;
import kr.co.bnk.bnk_project.mapper.MemberMapper;
import kr.co.bnk.bnk_project.mapper.RiskTestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final RiskTestMapper riskTestMapper;
    private final MemberMapper memberMapper;

    @Transactional
    public InvestmentResultDTO processAnalysis(InvestmentSurveyDTO dto, String userId) {

        // 1. 점수 계산 (100점 만점 로직)
        int score = calculateScore(dto);

        // 2. 성향 판정
        String type;
        String desc;
        if (score < 20) { type="매우 낮은 위험"; desc="원금 보전을 최우선으로 합니다."; }
        else if (score < 40) { type="낮은 위험"; desc="원금 보존과 이자 수익을 추구합니다."; }
        else if (score < 60) { type="중간 위험"; desc="일정 손실을 감내하며 수익을 기대합니다."; }
        else if (score < 80) { type="높은 위험"; desc="높은 수익을 위해 위험을 적극 수용합니다."; }
        else { type="매우 높은 위험"; desc="고수익을 위해 원금 손실 위험도 감수합니다."; }

        // 3. 사용자 식별번호(PK) 조회
        Long custNo = memberMapper.findCustNoByUserId(userId);

        // 4. DB 저장
        RiskTestResultDTO dbDto = new RiskTestResultDTO();
        dbDto.setCustNo(custNo);
        dbDto.setTotalScore(score);
        dbDto.setRiskType(type);
        riskTestMapper.insertRiskTestResult(dbDto);

        return new InvestmentResultDTO(type, score, desc);
    }

    private int calculateScore(InvestmentSurveyDTO dto) {
        int score = 0;
        // 예시 로직: 각 문항별 가중치 적용
        score += (dto.getQ1() != null) ? (6 - dto.getQ1()) * 3 : 0; // 연령
        score += (dto.getQ2() != null) ? dto.getQ2() * 2 : 0;       // 소득
        // ... (Q3~Q9 생략) ...
        score += (dto.getQ10() != null) ? dto.getQ10() * 5 : 0;     // 손실감내(가중치 높음)
        return Math.min(score, 100); // 최대 100점 제한
    }
}
