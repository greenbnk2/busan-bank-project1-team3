package kr.co.bnk.bnk_project.mapper;

import kr.co.bnk.bnk_project.dto.UserTermsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserTermsMapper {

    // 전체 약관 목록 조회
    List<UserTermsDTO> selectAllTerms();

    // termId 로 단일 약관 조회
    UserTermsDTO selectTermById(@Param("termId") String termId);

    // 약관 수정
    int updateTerm(UserTermsDTO dto);

    // 필요시 INSERT
    // int insertTerm(UserTermsDTO dto);
}
