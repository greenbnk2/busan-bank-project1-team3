package kr.co.bnk.bnk_project.mapper;

import kr.co.bnk.bnk_project.dto.ProductDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WishListMapper {

    int existsWishlist(@Param("custNo") Long custNo,
                       @Param("fundCode") String fundCode);

    int insertWishlist(@Param("custNo") Long custNo,
                       @Param("fundCode") String fundCode);

    int deleteWishlist(@Param("custNo") Long custNo,
                       @Param("fundCode") String fundCode);

    List<ProductDTO> getWishlist(@Param("custNo") Long custNo);

}