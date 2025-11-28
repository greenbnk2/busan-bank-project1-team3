package kr.co.bnk.bnk_project.service.admin;

import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
import kr.co.bnk.bnk_project.dto.admin.AdminFundMasterDTO;
import kr.co.bnk.bnk_project.dto.admin.ApprovalDTO;
import kr.co.bnk.bnk_project.dto.admin.FundMasterRevisionDTO;
import kr.co.bnk.bnk_project.dto.admin.ProductListDTO;
import kr.co.bnk.bnk_project.mapper.admin.AdminFundMapper;
import kr.co.bnk.bnk_project.mapper.admin.FundMasterRevisionMapper;
import kr.co.bnk.bnk_project.mapper.admin.ApprovalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminFundService {

    private final AdminFundMapper adminFundMapper;
    private final FundMasterRevisionMapper fundMasterRevisionMapper;
    private final ApprovalMapper approvalMapper;

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

    @Transactional
    public void updateFund(AdminFundMasterDTO dto, String createdBy) {
        // 1. FUND_MASTER의 현재 전체 데이터 조회
        FundMasterRevisionDTO revision = fundMasterRevisionMapper.selectFundMasterForRevision(dto.getFundCode());

        if (revision == null) {
            throw new IllegalArgumentException("펀드를 찾을 수 없습니다: " + dto.getFundCode());
        }

        // 2. 수정된 필드만 반영
        if (dto.getInvestGrade() != null) {
            revision.setInvestGrade(dto.getInvestGrade());
        }
        if (dto.getFundFeature() != null) {
            revision.setFundFeature(dto.getFundFeature());
        }
        if (dto.getNotice1() != null) {
            revision.setNotice1(dto.getNotice1());
        }
        if (dto.getNotice2() != null) {
            revision.setNotice2(dto.getNotice2());
        }

        // 3. revision 정보 설정
        revision.setCreatedBy(createdBy);

        // 4. FUND_MASTER_REVISION에 INSERT
        fundMasterRevisionMapper.insertRevision(revision);

        // 5. APPROVAL_HISTORY에 INSERT (승인 요청)
        ApprovalDTO approvalDTO = ApprovalDTO.builder()
                .apprType("수정")
                .fundCode(dto.getFundCode())
                .requester(createdBy)
                .requestReason("펀드 정보 수정 요청")
                .build();
        approvalMapper.insertApproval(approvalDTO);
    }


    /*중지 재개*/
    public void stopFund(String fundCode) {

        adminFundMapper.stopFund(fundCode);
    }

    public void resumeFund(String fundCode) {

        adminFundMapper.resumeFund(fundCode);
    }


    public void updateOperStatus(String fundCode) {
        if (fundCode == null || fundCode.isBlank()) {
            return;
        }

        AdminFundMasterDTO currentFund = adminFundMapper.selectPendingFundEdit(fundCode);

        if (currentFund == null) {
            return;
        }

        String operStatus = currentFund.getOperStatus() != null ? currentFund.getOperStatus().trim() : "";
        String updateStat = currentFund.getUpdateStat() != null ? currentFund.getUpdateStat().trim() : null;

        if ("등록".equals(operStatus) && (updateStat == null || updateStat.isEmpty())) {
            adminFundMapper.updateOperStatus(fundCode);
        } else if ("운용중".equals(operStatus) && "수정".equals(updateStat)) {
            adminFundMapper.updateStatus(currentFund);
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

    //예약시간 넣기 및 상태 변경
    public void setFundReserveTime(String fundCode, LocalDateTime date) {
        AdminFundMasterDTO currentFund = adminFundMapper.selectPendingFundEdit(fundCode);
        if (currentFund == null) {
            throw new IllegalArgumentException("펀드를 찾을 수 없습니다: " + fundCode);
        }
        
        adminFundMapper.setFundReserveTime(fundCode, date);
        
        FundMasterRevisionDTO completedRevision = fundMasterRevisionMapper.selectCompletedRevision(fundCode);
        
        if (date.isBefore(LocalDateTime.now()) || date.isEqual(LocalDateTime.now())) {
            if (completedRevision != null) {
                fundMasterRevisionMapper.applyRevisionToMaster(completedRevision.getRevId());
                fundMasterRevisionMapper.updateRevisionStatusToApplied(completedRevision.getRevId());
            } else {
                adminFundMapper.updateStatusToPending(fundCode);
            }
        }
    }
}