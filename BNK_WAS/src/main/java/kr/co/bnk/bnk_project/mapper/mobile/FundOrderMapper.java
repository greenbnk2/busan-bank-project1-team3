package kr.co.bnk.bnk_project.mapper.mobile;

import kr.co.bnk.bnk_project.dto.mobile.FundOrderDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FundOrderMapper {

    Long getNextOrderId();

    void insertFundOrder(FundOrderDTO fundOrderDTO);

    /**
     * 이미 가입된 펀드의 주문 정보 조회 (USER_FUND_PLAN에 없을 경우)
     * @param custNo 고객 번호
     * @param fundCode 펀드 코드
     * @return 가장 최근 주문 정보 (없으면 null)
     */
    FundOrderDTO getExistingOrderInfo(@Param("custNo") Long custNo, @Param("fundCode") String fundCode);

    /**
     * 금액 확정: REQUESTED -> FIXED
     */
    void updateOrderStatusToFixed(FundOrderDTO orderDTO);

    /**
     * 투자 시작: FIXED -> STARTED
     */
    void updateOrderStatusToStarted(FundOrderDTO orderDTO);

    /**
     * 주문 취소
     */
    void cancelOrder(FundOrderDTO orderDTO);

    /**
     * 금액 확정 대상 주문 조회 (REQUESTED 상태)
     */
    List<FundOrderDTO> selectOrdersToFix();

    /**
     * 투자 시작 대상 주문 조회 (FIXED 상태)
     */
    List<FundOrderDTO> selectOrdersToStart();

    /**
     * 주문 상세 조회
     */
    FundOrderDTO getOrderById(@Param("orderId") Long orderId);

}
