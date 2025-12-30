package kr.co.bnk.bnk_project.mapper;

import kr.co.bnk.bnk_project.dto.RiskTestResultDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RiskTestMapper {

    //투자성향분석 결과 저장
    void insertRiskTestResult(RiskTestResultDTO dto);

    //투자성향분석 결과 조회
    RiskTestResultDTO findValidTestByCustNo(@Param("custNo") Long custNo);

    //오늘 투자성향 조사를 했는지 확인 (하루에 한 번만)
    int hasTodayRiskTest(@Param("custNo") Long custNo);
}