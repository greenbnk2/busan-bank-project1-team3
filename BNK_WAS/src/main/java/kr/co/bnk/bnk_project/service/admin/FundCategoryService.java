package kr.co.bnk.bnk_project.service.admin;

import kr.co.bnk.bnk_project.dto.CsDTO;
import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
import kr.co.bnk.bnk_project.dto.admin.FundCategoryDTO;
import kr.co.bnk.bnk_project.dto.admin.MemberListDTO;
import kr.co.bnk.bnk_project.mapper.admin.FundCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class FundCategoryService {

    private final FundCategoryMapper fundCategoryMapper;

    public PageResponseDTO<FundCategoryDTO> getCategoryPage(PageRequestDTO pageRequestDTO) {

        if (pageRequestDTO.getPg() <= 0) pageRequestDTO.setPg(1);
        if (pageRequestDTO.getSize() <= 0) pageRequestDTO.setSize(10);


        int total = fundCategoryMapper.selectCategoryTotal(pageRequestDTO);
        List<FundCategoryDTO> list = fundCategoryMapper.selectAllCategories(pageRequestDTO);

        return PageResponseDTO.of(pageRequestDTO, list, total);

    }

    public boolean updateCategoryStatus(String categoryCode, String status) {
        if (categoryCode == null || categoryCode.isBlank()) {
            return false;
        }
        String normalizedStatus = "on";
        if ("off".equalsIgnoreCase(status)) {
            normalizedStatus = "off";
        }
        return fundCategoryMapper.updateCategoryStatus(categoryCode, normalizedStatus) > 0;
    }


    public void createCategory(FundCategoryDTO dto) {

        fundCategoryMapper.insertCategory(dto);
    }

    public void updateCategory(FundCategoryDTO dto) {

        fundCategoryMapper.updateCategory(dto);
    }

    public void deleteCategory(String categoryCode) {

        fundCategoryMapper.deleteCategory(categoryCode);
    }

    public List<FundCategoryDTO> getAllCategories() {
        return fundCategoryMapper.selectCategoryOptions();
    }

}
