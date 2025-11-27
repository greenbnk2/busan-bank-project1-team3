package kr.co.bnk.bnk_project.service.admin;

import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
import kr.co.bnk.bnk_project.dto.admin.AdminFundMasterDTO;
import kr.co.bnk.bnk_project.dto.admin.ProductListDTO;
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



    /* 펀드 등록 검색 */
    public AdminFundMasterDTO getPendingFundEdit(String fundCode) {
        if (fundCode == null || fundCode.isBlank()) {
            return null;
        }
        return adminFundMapper.selectPendingFundEdit(fundCode);
    }

    public void updateFund(AdminFundMasterDTO dto) {

        adminFundMapper.updateFundForEdit(dto);
    }


    /*중지 재개*/
    public void stopFund(String fundCode) {

        adminFundMapper.stopFund(fundCode);
    }

    public void resumeFund(String fundCode) {

        adminFundMapper.resumeFund(fundCode);
    }


    public void updateOperStatus(String fundCode) {

        // 메서드 진입 확인 로그 (가장 먼저)
        System.out.println(">>> updateOperStatus 메서드 호출됨 - fundCode: [" + fundCode + "]");

        if(fundCode == null || fundCode.isBlank()) {
            System.out.println(">>> fundCode가 null이거나 비어있음 - return");
            return;
        }

        // 데이터베이스에서 현재 펀드 상태 조회
        AdminFundMasterDTO currentFund = adminFundMapper.selectPendingFundEdit(fundCode);

        if(currentFund == null) {
            System.out.println(">>> 펀드를 찾을 수 없습니다: " + fundCode);
            return;
        }

        String operStatus = currentFund.getOperStatus() != null ? currentFund.getOperStatus().trim() : "";
        String updateStat = currentFund.getUpdateStat() != null ? currentFund.getUpdateStat().trim() : null;

        if("등록".equals(operStatus) && (updateStat == null || updateStat.isEmpty())){
            adminFundMapper.updateOperStatus(fundCode);
        }else if("운용중".equals(operStatus) && "수정".equals(updateStat)){
            adminFundMapper.updateStatus(currentFund);
        } else {
            System.out.println("조건 불일치 - operStatus: [" + operStatus + "], updateStat: [" + updateStat + "]");
        }
    }

    public void updateStatusAfterApproval(String fundCode, String status) {
        adminFundMapper.updateStatusAfterApproval(fundCode, status);
    }






    /*--------------------------------------------------*/

    // 펀드 목록 페이지
    public PageResponseDTO<ProductListDTO> getProductPage(PageRequestDTO pageRequestDTO) {

        // 방어 코드
        if (pageRequestDTO.getPg() <= 0) pageRequestDTO.setPg(1);
        if (pageRequestDTO.getSize() <= 0) pageRequestDTO.setSize(10);

        // 목록
        List<ProductListDTO> list = adminFundMapper.selectProductList(pageRequestDTO);

        // 총 개수
        int total = adminFundMapper.selectProductTotal(pageRequestDTO);


        return PageResponseDTO.of(pageRequestDTO, list, total);


    }
}
