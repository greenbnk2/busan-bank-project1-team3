package kr.co.bnk.bnk_project.service.admin;

import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.admin.AdminFundMasterDTO;
import kr.co.bnk.bnk_project.mapper.admin.AdminFundMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminFundService {

    private final AdminFundMapper adminFundMapper;

    /* 펀드 등록 검색 */
    public AdminFundMasterDTO getPendingFund(PageRequestDTO pageRequestDTO) {

        // 검색어 없으면 바로 null 리턴해서 화면은 빈 폼 유지
        if (pageRequestDTO.getKeyword() == null || pageRequestDTO.getKeyword().isBlank()) {
            return null;
        }

        // searchType 기본값 세팅 (없을 때 code로)
        if (pageRequestDTO.getSearchType() == null || pageRequestDTO.getSearchType().isBlank()) {
            pageRequestDTO.setSearchType("code");
        }

        return adminFundMapper.selectPendingFund(pageRequestDTO);
    }


    public List<AdminFundMasterDTO> getFundSuggestions(String searchType, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        if (searchType == null || searchType.isBlank()) {
            searchType = "code";
        }
        return adminFundMapper.selectFundSuggestions(searchType, keyword);

    }
    public void updateFundAndChangeStatus(AdminFundMasterDTO dto) {
        adminFundMapper.updateFundForRegister(dto);
    }

    /*---------------------수정-----------------------------*/



    public List<AdminFundMasterDTO> getFundSuggestionsEdit(String searchType, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        if (searchType == null || searchType.isBlank()) {
            searchType = "code";
        }
        return adminFundMapper.selectFundSuggestionsEdit(searchType, keyword);

    }

    public void updateFundAndChangeStatusEdit(AdminFundMasterDTO dto) {
        adminFundMapper.updateFundForEdit(dto);
    }
}
