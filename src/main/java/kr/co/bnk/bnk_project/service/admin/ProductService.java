package kr.co.bnk.bnk_project.service.admin;

import kr.co.bnk.bnk_project.dto.CsDTO;
import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
import kr.co.bnk.bnk_project.dto.admin.ProductListDTO;
import kr.co.bnk.bnk_project.mapper.admin.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    // 펀드 목록 페이지
    public PageResponseDTO<ProductListDTO> getProductPage(PageRequestDTO pageRequestDTO) {

        if (pageRequestDTO.getPg() <= 0) pageRequestDTO.setPg(1);
        if (pageRequestDTO.getSize() <= 0) pageRequestDTO.setSize(10);

        pageRequestDTO.setCate("product");

        List<ProductListDTO> list = productMapper.selectProductList(pageRequestDTO);
        int total = productMapper.selectProductTotal(pageRequestDTO);

        return PageResponseDTO.of(pageRequestDTO, list, total);


    }
}