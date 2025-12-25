package kr.co.bnk.bnk_project.service;

import kr.co.bnk.bnk_project.dto.InvestmentResultDTO;
import kr.co.bnk.bnk_project.dto.InvestmentSurveyDTO;
import kr.co.bnk.bnk_project.dto.RiskTestResultDTO;
import kr.co.bnk.bnk_project.mapper.MemberMapper;
import kr.co.bnk.bnk_project.mapper.RiskTestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final RiskTestMapper riskTestMapper;
    private final MemberMapper memberMapper;

    @Transactional
    public InvestmentResultDTO processAnalysis(InvestmentSurveyDTO dto, String userId) {

        // 점수 계산
        int score = calculateTotalScore(dto);

        // 성향 판정 및 설명 결정
        InvestmentResultDTO resultInfo = determineRiskType(score);

        // 사용자 식별번호(PK) 조회
        Long custNo = memberMapper.findCustNoByUserId(userId);

        // DB 저장을 위한 DTO 변환
        RiskTestResultDTO dbDto = new RiskTestResultDTO();
        dbDto.setCustNo(custNo);
        dbDto.setTotalScore(score);
        dbDto.setRiskType(resultInfo.getType()); // "공격투자형", "안정형" 등 저장

        // DB 저장
        riskTestMapper.insertRiskTestResult(dbDto);

        return resultInfo;
    }

    /**
     * 문항별 배점 계산 로직 (총 100점)
     */
    private int calculateTotalScore(InvestmentSurveyDTO dto) {
        int score = 0;

        // Q1. 연령 (최대 10) - 1(19이하):10, 2:10, 3:8, 4:6, 5:4, 6:2
        score += getScore(new int[]{0, 10, 10, 8, 6, 4, 2}, dto.getQ1());

        // Q2. 연소득 (최대 10) - 2, 4, 6, 8, 10
        score += getScore(new int[]{0, 2, 4, 6, 8, 10}, dto.getQ2());

        // Q3. 수입원 (최대 5) - 1:5, 2:4, 3:2, 4:1
        score += getScore(new int[]{0, 5, 4, 2, 1}, dto.getQ3());

        // Q4. 투자비중 (최대 10) - 2, 4, 6, 8, 10
        score += getScore(new int[]{0, 2, 4, 6, 8, 10}, dto.getQ4());

        // Q5. 투자경험 (다중선택, 최대 10)
        // 1,2번: 1점 / 3번: 2점 / 4,5번: 3점
        if (dto.getQ5() != null) {
            int q5Score = 0;
            for (Integer val : dto.getQ5()) {
                if (val == 1 || val == 2) q5Score += 1;
                else if (val == 3) q5Score += 2;
                else if (val == 4 || val == 5) q5Score += 3;
            }
            score += Math.min(q5Score, 10); // 합산 최대 10점으로 제한
        }

        // Q6. 경험기간 (최대 5) - 1:1, 2:3, 3:5
        score += getScore(new int[]{0, 1, 3, 5}, dto.getQ6());

        // Q7. 이해도 (최대 10) - 1:10, 2:7, 3:4, 4:1
        score += getScore(new int[]{0, 10, 7, 4, 1}, dto.getQ7());

        // Q8. 가입목적 (최대 5) - 1:5, 2:3, 3:3, 4:1
        score += getScore(new int[]{0, 5, 3, 3, 1}, dto.getQ8());

        // Q9. 투자기간 (최대 10) - 1:2, 2:4, 3:7, 4:10
        score += getScore(new int[]{0, 2, 4, 7, 10}, dto.getQ9());

        // Q10. 손실감내 (최대 25) - 1:5, 2:10, 3:15, 4:20, 5:25
        score += getScore(new int[]{0, 5, 10, 15, 20, 25}, dto.getQ10());

        return score;
    }

    /**
     * 점수에 따른 등급 및 설명 결정
     */
    private InvestmentResultDTO determineRiskType(int score) {
        String type;
        String description;

        if (score > 80) {
            type = "공격투자형";
            description = "높은 수익을 위해 시장 평균 이상의 위험을 적극적으로 수용합니다.";
        } else if (score > 60) {
            type = "적극투자형";
            description = "투자 원금의 보전보다는 위험을 감내하더라도 높은 수익을 추구합니다.";
        } else if (score > 40) {
            type = "위험중립형";
            description = "일정 수준의 손실 위험을 감내하면서 예적금보다 높은 수익을 기대합니다.";
        } else if (score > 20) {
            type = "안정추구형";
            description = "원금 손실을 최소화하고 안정적인 이자 소득을 추구합니다.";
        } else {
            type = "안정형";
            description = "예금 또는 적금 수준의 안정적인 수익을 기대하며 원금 손실을 기피합니다.";
        }

        return new InvestmentResultDTO(type, score, description);
    }

    // 배열 인덱스 안전 접근 헬퍼 메서드
    private int getScore(int[] scoreArr, Integer value) {
        if (value == null || value < 1 || value >= scoreArr.length) return 0;
        return scoreArr[value];
    }

    @Transactional(readOnly = true)
    public boolean isRiskTestValid(Long custNo) {
        RiskTestResultDTO result = riskTestMapper.findValidTestByCustNo(custNo);
        return result != null;
    }

    @Transactional(readOnly = true)
    public String getUserRiskType(Long custNo) {
        RiskTestResultDTO result = riskTestMapper.findValidTestByCustNo(custNo);
        if (result != null) {
            return result.getRiskType(); // 예: "공격투자형", "위험중립형" 등
        }
        return null;
    }
}