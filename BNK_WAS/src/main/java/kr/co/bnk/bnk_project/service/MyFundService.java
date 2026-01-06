package kr.co.bnk.bnk_project.service;

import kr.co.bnk.bnk_project.dto.MyFundResponse;
import kr.co.bnk.bnk_project.mapper.FundMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MyFundService {
    
    private final FundMapper fundMapper;
    
    public List<MyFundResponse> getMyFunds(Integer custNo) {
        try {
            return fundMapper.findMyFundsByCustNo(custNo);
        } catch (Exception e) {
            System.err.println("보유펀드 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 보유펀드 상세 정보 조회
    public Map<String, Object> getMyFundDetail(Integer custNo, String fundCode) {
        try {
            return fundMapper.findMyFundDetail(custNo, fundCode);
        } catch (Exception e) {
            System.err.println("보유펀드 상세 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("보유펀드 상세 조회 중 오류 발생: " + e.getMessage(), e);
        }
    }

    // 보유펀드 수익률 히스토리 조회
    public List<Map<String, Object>> getMyFundProfitHistory(Integer custNo, String fundCode, String period) {
        // period에 따라 시작일 계산
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        
        switch (period) {
            case "1M":
                startDate = endDate.minusMonths(1);
                break;
            case "3M":
                startDate = endDate.minusMonths(3);
                break;
            case "6M":
                startDate = endDate.minusMonths(6);
                break;
            case "1Y":
                startDate = endDate.minusYears(1);
                break;
            case "ALL":
            default:
                startDate = endDate.minusYears(1);
                break;
        }
        
        try {
            return fundMapper.findMyFundProfitHistory(
                custNo, 
                fundCode, 
                startDate.toString(), 
                endDate.toString()
            );
        } catch (Exception e) {
            System.err.println("수익률 히스토리 조회 중 오류: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 보유펀드 거래 내역 조회
    public List<Map<String, Object>> getMyFundTransactions(Integer custNo, String fundCode) {
        try {
            List<Map<String, Object>> transactions = fundMapper.findMyFundTransactions(custNo, fundCode);
            
            // orderNo를 역순으로 설정하고 첫 번째 거래에 isStart=true 설정
            int totalCount = transactions.size();
            for (int i = 0; i < transactions.size(); i++) {
                Map<String, Object> transaction = transactions.get(i);
                // orderNo는 역순으로 설정 (가장 최근 거래가 1번)
                transaction.put("orderNo", totalCount - i);
                // 마지막(가장 오래된) 거래가 첫 거래
                transaction.put("isStart", (i == transactions.size() - 1));
            }
            
            return transactions;
        } catch (Exception e) {
            System.err.println("거래 내역 조회 중 오류: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}

