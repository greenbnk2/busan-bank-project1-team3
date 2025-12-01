package kr.co.bnk.bnk_project.mapper.admin;

import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.admin.FundCategoryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface FundCategoryMapper {

    // 페이징 목록 조회
    List<FundCategoryDTO> selectAllCategories(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    // 전체 갯수 조회
    int selectCategoryTotal(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    int updateCategoryStatus(@Param("categoryCode") String categoryCode,
                             @Param("status") String status);

    void insertCategory(FundCategoryDTO dto);

    void updateCategory(FundCategoryDTO dto);

    void deleteCategory(@Param("categoryCode") String categoryCode);

    List<FundCategoryDTO> selectCategoryOptions();

}
