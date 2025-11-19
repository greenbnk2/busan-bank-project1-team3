package kr.co.bnk.bnk_project.service;

import kr.co.bnk.bnk_project.dto.ProductDTO;
import kr.co.bnk.bnk_project.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FundService {

    private final ProductMapper productMapper;

    public List<ProductDTO> getProductList(){
        return productMapper.findProductList();
    }
}
