package com.ap.service;

import com.ap.dto.ProductDTO;
import com.ap.entity.FundMaster;
import com.ap.mapper.FundMapper;
import com.ap.repository.FundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FundService {

    // 나머지는 기존 서비스 형식 그대로 코드 작성하시면 됩니다.

    private final FundMapper productMapper;
    private final FundRepository fundRepository;

    // 단일 펀드 조회
    public FundMaster getFundMasterByGrade(String investGrade) {
        return fundRepository.findAllByInvestGrade(investGrade).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("상품 없음"));
    }

    // 전체 리스트 조회
    public List<FundMaster> getFundList() {
        return fundRepository.findAll();
    }

    public ProductDTO getProductDetail(String fundcode) {
        return productMapper.findProductDetail(fundcode);
    }
}

