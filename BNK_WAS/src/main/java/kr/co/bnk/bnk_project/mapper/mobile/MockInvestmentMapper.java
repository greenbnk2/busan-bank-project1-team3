package kr.co.bnk.bnk_project.mapper.mobile;

import kr.co.bnk.bnk_project.dto.mobile.MockUserInvestmentDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MockInvestmentMapper {
    // 특정 사용자의 현재 모의투자 포트폴리오 요약 정보 조회
    List<MockUserInvestmentDto> getMockUserPortfolio(String userId);
}