package kr.co.bnk.bnk_project.mapper;

import kr.co.bnk.bnk_project.dto.KeywordDTO;
import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KeywordMapper {

    // 페이징 목록 조회
    List<KeywordDTO> selectAllKeyword(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    
    // 전체 갯수 조회
    int selectKeywordTotal(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    void insertKeyword(KeywordDTO dto);

    void updateKeyword(KeywordDTO dto);

    void deleteKeyword(@Param("keywordNo") String keywordNo);

}
