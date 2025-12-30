package kr.co.bnk.bnk_project.mapper.mobile;

import kr.co.bnk.bnk_project.dto.mobile.FundPositionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FundPositionMapper {

    /**
     * 포지션 조회 (없으면 null)
     */
    FundPositionDTO getPosition(@Param("custNo") Long custNo, @Param("fundCode") String fundCode);

    /**
     * 포지션 INSERT (신규 보유)
     */
    void insertFundPosition(FundPositionDTO positionDTO);

    /**
     * 포지션 UPDATE (기존 보유 증가)
     */
    void updateFundPosition(FundPositionDTO positionDTO);

    /**
     * 고객의 모든 포지션 조회
     */
    List<FundPositionDTO> getPositionsByCustNo(@Param("custNo") Long custNo);

}

