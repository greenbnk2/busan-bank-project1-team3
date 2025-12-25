package kr.co.bnk.bnk_project.mapper.mobile;

import kr.co.bnk.bnk_project.dto.mobile.MockAccountDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface MockAccountMapper {
    // 고객번호로 모의투자 계좌 정보 조회
    MockAccountDTO findByCustNo(Long custNo);

    // 특정 고객의 가장 최근 투자 성향 결과 조회
    String findRiskTypeByCustNo(Long custNo);

    // 모의투자 계좌 생성
    int insertMockAccount(MockAccountDTO mockAccountDTO);

    Map<String, Object> getDashboardSummary(Long custNo);

    List<Map<String, Object>> getHoldingsByCustNo(Long custNo);
}
