package kr.co.bnk.bnk_project.service;

import kr.co.bnk.bnk_project.dto.*;
import kr.co.bnk.bnk_project.mapper.FundMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FundService {

    private final FundMapper productMapper;

    public List<ProductDTO> getProductList(){
        return productMapper.find_ProductList();
    }

    public ProductDTO getProductDetail(String fundcode) {
        return productMapper.findProductDetail(fundcode);
    }

    public List<ProductDTO> getFundDocuments() {
        return productMapper.findFundDocuments();
    }

    // 회원별 펀드 정보
    public List<UserFundDTO> getMyFundList(Long custNo) {
        // DB에서 데이터 가져오기
        List<UserFundDTO> list = productMapper.selectMyFundList(custNo);

        // 가져온 리스트를 하나씩 꺼내서 '세금'과 '세후 금액' 계산하여 채워넣기
        for (UserFundDTO dto : list) {

            long principal = dto.getPurchaseAmount();       // 투자 원금
            long current = dto.getCurrentEvalAmount();      // 현재 평가금액 (세전)
            long profit = current - principal;              // 평가 손익 (얼마 벌었는지)

            // A. 손익금액(amount) 세팅
            dto.setProfitAmount(profit);

            // B. 세금(tax) 계산 (이익금의 15.4%, 손실이면 0원)
            long tax = 0;
            if (profit > 0) {
                tax = (long) (profit * 0.154);
            }
            dto.setTaxAmount(tax);

            // C. 세후 평가금액(net) 세팅 (현재금액 - 세금)
            dto.setAfterTaxAmount(current - tax);
        }

        return list;
    }

    // 총 평가금액 계산
    public long calculateTotalEval(List<UserFundDTO> list) {
        return list.stream().mapToLong(UserFundDTO::getCurrentEvalAmount).sum();
    }

    // 총 수익률 계산 (단순 합계 수익률)
    public double calculateTotalYield(List<UserFundDTO> list) {
        long totalPrincipal = list.stream().mapToLong(UserFundDTO::getPurchaseAmount).sum();
        long totalEval = list.stream().mapToLong(UserFundDTO::getCurrentEvalAmount).sum();

        if (totalPrincipal == 0) return 0.0;

        return (double) (totalEval - totalPrincipal) / totalPrincipal * 100;
    }

    // 펀드 목록 가져오기
    public List<FundMasterDTO> getAllFunds() {
        return productMapper.selectAllFundList();
    }

    // 기준가격 조회
    public List<FundPriceDTO> getPriceHistory(String fundCode, String startDate, String endDate) {
        return productMapper.selectFundPriceHistory(fundCode, startDate, endDate);
    }

    // 수익률 조회
    @Transactional(readOnly = true)
    public List<UserFundDTO> getYieldList(FundSearchDTO params) {
        return productMapper.selectFundYieldList(params);
    }
}
