package kr.co.bnk.bnk_project.mapper.admin;

import kr.co.bnk.bnk_project.dto.CsDTO;
import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.admin.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    // 페이징 목록 조회
    List<ProductListDTO> selectProductList(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    // 전체 갯수 조회
    int selectProductTotal(@Param("pageRequestDTO")PageRequestDTO pageRequestDTO);

    // 돋보기 상세
    FundListDetailDTO selectProductDetail(@Param("fundCode") String fundCode);

    // 펀드 문서 조회(약관, 투자설명서, 간이설명서)
    List<FundListDetailDTO> selectFundDocuments(@Param("fundCode") String fundCode);

    // 수익률 추이 차트
    List<FundReturnHistoryDTO> selectFundReturnHistory(@Param("fundCode") String fundCode);

    // 가격변동 추이
    List<FundPriceHistoryDTO> selectFundPriceHistory(@Param("fundCode") String fundCode);

    // 자산구성내역


    // 결산 및 상환
    List<FundSettlementHistoryDTO> selectFundSettlementHistory(@Param("fundCode") String fundCode);

    // 공시관련
}
