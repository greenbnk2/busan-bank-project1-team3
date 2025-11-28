package kr.co.bnk.bnk_project.service.admin;

import kr.co.bnk.bnk_project.dto.CsDTO;
import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
import kr.co.bnk.bnk_project.dto.admin.*;
import kr.co.bnk.bnk_project.mapper.admin.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
/*
    날짜 : 2025/11/24
    이름 : 이종봉
    내용 : 펀드목록 돋보기
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    // 펀드 목록 페이지
    public PageResponseDTO<ProductListDTO> getProductPage(PageRequestDTO pageRequestDTO) {

        // 방어 코드
        if (pageRequestDTO.getPg() <= 0) pageRequestDTO.setPg(1);
        if (pageRequestDTO.getSize() <= 0) pageRequestDTO.setSize(10);

        // 목록
        List<ProductListDTO> list = productMapper.selectProductList(pageRequestDTO);

        // 총 개수
        int total = productMapper.selectProductTotal(pageRequestDTO);


        return PageResponseDTO.of(pageRequestDTO, list, total);

    }

    // 펀드 상세 조회(돋보기)
    public FundListDetailDTO getProductDetail(String fundCode) {

        // 기존 상세 조회
        FundListDetailDTO dto = productMapper.selectProductDetail(fundCode);

        // 상세 없다면 null 반환 (방어코드)
        if(dto == null) return null;

        // 문서조회(약관, 투자설명서, 간이투자설명서)
        List<FundListDetailDTO> docs = productMapper.selectFundDocuments(fundCode);

        String termsUrl = null;
        String investUrl = null;
        String summaryUrl = null;

        for(FundListDetailDTO doc : docs){
            switch (doc.getDocType()){
                case "TERMS":
                    termsUrl = doc.getDocUrl();
                    break;
                case "INVEST":
                    investUrl = doc.getDocUrl();
                    break;
                case "SUMMARY":
                    summaryUrl = doc.getDocUrl();
                    break;
            }
        }

        dto.setTermsUrl(termsUrl);
        dto.setInvestUrl(investUrl);
        dto.setSummaryUrl(summaryUrl);

        // 수익률 추이
        List<FundPriceHistoryDTO> priceHistoryList = productMapper.selectFundPriceHistory(fundCode);
        dto.setPriceHistoryList(priceHistoryList);

        // 가격변동 추이
        List<FundReturnHistoryDTO> returnHistoryList = productMapper.selectFundReturnHistory(fundCode);
        dto.setReturnHistoryList(returnHistoryList);

        // 자산 구성 내역
        //List<FundAssetAllocationDTO> assetAllocationList = productMapper.selectFundAssetAllocation(fundCode);


        // 결산 및 상환
        List<FundSettlementHistoryDTO> settlementList = productMapper.selectFundSettlementHistory(fundCode);
        dto.setSettlementList(settlementList);

        return dto;
    }
}