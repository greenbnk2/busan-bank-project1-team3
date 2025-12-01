package kr.co.bnk.bnk_project.mapper;

import kr.co.bnk.bnk_project.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FundMapper {
    ProductDTO findProductDetail(String fundcode);
    List<ProductDTO> find_ProductList();
    List<ProductDTO> findALL();
    List<ProductDTO> findFundDocuments();

    //마이페이지 - 회원별 펀드 정보
    List<UserFundDTO> selectMyFundList(Long custNo);

    //마이페이지 -  기준가격 이력 조회
    List<FundPriceDTO> selectFundPriceHistory(@Param("fundCode") String fundCode,
                                              @Param("startDate") String startDate,
                                              @Param("endDate") String endDate);
    List<FundMasterDTO> selectAllFundList();

    //마이페이지 - 수익률 조회
    List<UserFundDTO> selectFundYieldList(FundSearchDTO params);
}
