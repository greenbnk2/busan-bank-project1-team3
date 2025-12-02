package kr.co.bnk.bnk_project.mapper;

import kr.co.bnk.bnk_project.dto.*;
import kr.co.bnk.bnk_project.dto.FundPeriodDTO;
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

    // 추천 키워드 (랜덤 5개)
    List<KeywordDTO> selectRandomKeywords();

    // 연관 단어 찾기 (예: "파킹" -> "단기채권,채권")
    List<String> selectRelatedKeywords(String searchWord);

    // 실제 상품 검색
    List<ProductDTO> selectFundsBySearch(@Param("searchWord") String searchWord,
                                         @Param("expandedList") List<String> expandedList);

    List<FundChartDTO> selectFundNavLast3Months(String fundCode);

    FundPeriodDTO selectFundPeriodYield(String fundCode);
}

