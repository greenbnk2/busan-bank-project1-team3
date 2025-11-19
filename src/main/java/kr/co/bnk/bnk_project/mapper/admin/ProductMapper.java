package kr.co.bnk.bnk_project.mapper.admin;

import kr.co.bnk.bnk_project.dto.CsDTO;
import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.admin.ProductListDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    // 페이징 목록 조회
    List<ProductListDTO> selectProductList(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    // 전체 갯수 조회
    int selectProductTotal(@Param("pageRequestDTO")PageRequestDTO pageRequestDTO);


}
