package kr.co.bnk.bnk_project.service;

import kr.co.bnk.bnk_project.dto.ProductDTO;
import kr.co.bnk.bnk_project.mapper.WishListMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishListService {

    private final WishListMapper wishListMapper;

    public void addWish(Long custNo, String fundCode) {

        int exists = wishListMapper.existsWishlist(custNo, fundCode);

        if (exists > 0) {
            throw new IllegalStateException("이미 관심상품에 등록된 펀드입니다.");
        }

        wishListMapper.insertWishlist(custNo, fundCode);
    }

    public void deleteWish(Long custNo, String fundCode) {
        wishListMapper.deleteWishlist(custNo, fundCode);
    }

    public List<ProductDTO> getWishList(Long custNo) {
        return wishListMapper.getWishlist(custNo);
    }
}
