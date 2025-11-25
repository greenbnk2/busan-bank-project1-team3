package kr.co.bnk.bnk_project.mapper.admin;

import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.admin.AdminFundMasterDTO;
import kr.co.bnk.bnk_project.dto.admin.ProductListDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminFundMapper {

    /*펀드를 검색 타입 + 키워드로 조회*/
    AdminFundMasterDTO selectPendingFund(PageRequestDTO pageRequestDTO);



    /*등록*/
    List<AdminFundMasterDTO> selectFundSuggestions(@Param("searchType") String searchType,
                                                   @Param("keyword") String keyword);

    void updateFundForRegister(AdminFundMasterDTO dto);



    /*수정*/
    AdminFundMasterDTO selectPendingFundEdit(@Param("fundCode") String fundCode);

    void updateFundForEdit(AdminFundMasterDTO dto);


    void stopFund(@Param("fundCode") String fundCode);
    void resumeFund(@Param("fundCode") String fundCode);

    /*------------------------------------------------------------------------------------*/
    // 페이징 목록 조회
    List<ProductListDTO> selectProductList(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    // 전체 갯수 조회
    int selectProductTotal(@Param("pageRequestDTO")PageRequestDTO pageRequestDTO);

}
