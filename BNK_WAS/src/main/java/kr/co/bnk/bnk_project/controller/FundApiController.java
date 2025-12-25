package kr.co.bnk.bnk_project.controller;

import kr.co.bnk.bnk_project.dto.ProductDTO;
import kr.co.bnk.bnk_project.security.MyUserDetails;
import kr.co.bnk.bnk_project.service.FundService;
import kr.co.bnk.bnk_project.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fund")
public class FundApiController {

    private final FundService fundService;
    private final WishListService wishListService;

    @GetMapping("/list")
    public List<ProductDTO> getFundList() {
        return fundService.getProductList();
    }

    @GetMapping("/best")
    public List<ProductDTO> getFundBest() {
        return fundService.getFundYieldBest();
    }

    @PostMapping("/wishlist/add")
    public ResponseEntity<?> addWish(@RequestParam String fundCode, Authentication auth) {

        Long custNo = ((MyUserDetails) auth.getPrincipal()).getUserDTO().getCustNo();

        try {
            wishListService.addWish(custNo, fundCode);
            return ResponseEntity.ok(Map.of("result", "added"));

        } catch (IllegalStateException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)  // 409
                    .body(Map.of("result", "exists", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/wishlist/delete")
    public Map<String, Object> deleteWish(@RequestParam String fundCode, Authentication auth) {
        Long custNo = ((MyUserDetails) auth.getPrincipal()).getUserDTO().getCustNo();
        wishListService.deleteWish(custNo, fundCode);
        return Map.of("result", "deleted");
    }

    @GetMapping("/wishlist")
    public List<ProductDTO> getWishList(Authentication auth) {
        Long custNo = ((MyUserDetails) auth.getPrincipal()).getUserDTO().getCustNo();
        return wishListService.getWishList(custNo);
    }

    @GetMapping("/nav/year/{fundCode}")
    @ResponseBody
    public Map<String, Object> getYearNav(@PathVariable String fundCode) {

        List<ProductDTO> list = fundService.getLastYearNav(fundCode);

        Map<String, Object> result = new HashMap<>();
        result.put("labels", list.stream().map(ProductDTO::getLabel).toList());
        result.put("data", list.stream().map(ProductDTO::getValue).toList());

        return result;
    }
}
