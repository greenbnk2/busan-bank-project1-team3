package kr.co.bnk.bnk_project.mapper;

import kr.co.bnk.bnk_project.dto.ProductDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductMapper {
    List<ProductDTO> findProductList();
}
