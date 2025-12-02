package kr.co.bnk.bnk_project.service;

import kr.co.bnk.bnk_project.dto.*;
import kr.co.bnk.bnk_project.dto.FundPeriodDTO;
import kr.co.bnk.bnk_project.mapper.FundMapper;
import kr.co.bnk.bnk_project.mapper.admin.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FundService {

    private final FundMapper productMapper;

    public List<ProductDTO> getProductList() {
        List<ProductDTO> list = productMapper.find_ProductList();

        for (ProductDTO dto : list) {
            Double cur = dto.getCurrentNav();
            Double nav1 = dto.getNav1M();
            Double nav3 = dto.getNav3M();
            Double nav6 = dto.getNav6M();
            Double nav12 = dto.getNav12M();

            dto.setPerf1M(calcYield(cur, nav1));
            dto.setPerf3M(calcYield(cur, nav3));
            dto.setPerf6M(calcYield(cur, nav6));
            dto.setPerf12M(calcYield(cur, nav12));
        }

        return list;
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

    // 검색 기능
    public List<ProductDTO> searchFunds(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // KEYWORD 테이블에서 연관 단어 찾기
        List<String> relatedWordsDb = productMapper.selectRelatedKeywords(keyword);

        List<String> expandedList = new ArrayList<>();
        if (relatedWordsDb != null) {
            for (String words : relatedWordsDb) {
                if (words == null) continue;
                // 콤마(,)로 쪼개서 리스트에 담기
                String[] split = words.split(",");
                for (String s : split) {
                    if (!s.trim().isEmpty()) {
                        expandedList.add(s.trim());
                    }
                }
            }
        }

        // 검색어 + 연관단어 리스트를 가지고 DB 조회
        return productMapper.selectFundsBySearch(keyword, expandedList);
    }

    // 추천 키워드 가져오기
    public List<KeywordDTO> getRecommendedKeywords() {
        return productMapper.selectRandomKeywords();
    }

    public List<FundChartDTO> getFundNavLast3Months(String fundCode) {
        return productMapper.selectFundNavLast3Months(fundCode);
    }

    public FundPeriodDTO getFundPeriodYield(String fundCode) {
        FundPeriodDTO dto = productMapper.selectFundPeriodYield(fundCode);

        if (dto == null) return null;

        // 수익률 계산
        dto.setYield1M(calcYield(dto.getCurrentNav(), dto.getNav1M()));
        dto.setYield3M(calcYield(dto.getCurrentNav(), dto.getNav3M()));
        dto.setYield6M(calcYield(dto.getCurrentNav(), dto.getNav6M()));
        dto.setYield12M(calcYield(dto.getCurrentNav(), dto.getNav12M()));

        return dto;
    }

    private double calcYield(Double current, Double past) {
        if (current == null || past == null || past == 0) return 0.0;
        return (current - past) / past * 100.0;
    }

    public Double calculate1MonthReturn(Long fundId) {

        ProductDTO today = productMapper.getLatestNav(fundId);
        if (today == null || today.getNav() == null) {
            return null;
        }

        ProductDTO monthAgo = productMapper.getOneMonthAgoNav(fundId, today.getTradeDate());
        if (monthAgo == null || monthAgo.getNav() == null) {
            return null;
        }

        double result = (today.getNav() / monthAgo.getNav() - 1) * 100;

        // 소수점 2자리까지 반올림
        return Math.round(result * 100) / 100.0;
    }

    public List<ProductDTO> getFundYieldBest() {

        List<ProductDTO> list = productMapper.selectFundYieldBest();

        // 🔥 perf1M이 NULL이면 아예 제거
        list.removeIf(dto -> dto.getPerf1M() == null);

        // 🔥 안전한 정렬
        list.sort((a, b) -> Double.compare(b.getPerf1M(), a.getPerf1M()));

        // 🔥 TOP10만 반환
        return list.stream().limit(10).toList();

    }
}

