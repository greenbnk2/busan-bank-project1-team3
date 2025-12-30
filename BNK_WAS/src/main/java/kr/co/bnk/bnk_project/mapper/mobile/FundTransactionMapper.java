package kr.co.bnk.bnk_project.mapper.mobile;

import kr.co.bnk.bnk_project.dto.mobile.FundTransactionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FundTransactionMapper {

    /**
     * TRX_ID 생성 (MAX+1)
     */
    Long getNextTrxId();

    /**
     * 체결 내역 INSERT
     */
    void insertFundTransaction(FundTransactionDTO transactionDTO);

    /**
     * 주문 ID로 체결 내역 조회
     */
    List<FundTransactionDTO> getTransactionsByOrderId(@Param("orderId") Long orderId);

    /**
     * 고객의 체결 내역 조회
     */
    List<FundTransactionDTO> getTransactionsByCustNo(@Param("custNo") Long custNo);

}

