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

    /* 이미 등록된 펀드 확인 */
    public AdminFundMasterDTO getRegisteredFund(String searchType, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        if (searchType == null || searchType.isBlank()) {
            searchType = "code";
        }
        return adminFundMapper.selectRegisteredFund(searchType, keyword);
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

    public boolean updateFundAndChangeStatus(AdminFundMasterDTO dto) {
        // 업데이트 시도 (oper_status = '대기'인 경우에만 업데이트됨)
        int updatedRows = adminFundMapper.updateFundForRegisterWithResult(dto);
        // 업데이트된 행이 1개 이상이면 성공
        return updatedRows > 0;
    }

    /*---------------------수정-----------------------------*/


    /* 펀드 등록 검색 */
    public AdminFundMasterDTO getPendingFundEdit(String fundCode) {
        if (fundCode == null || fundCode.isBlank()) {
            return null;
        }

        // 1. FUND_MASTER 데이터 조회
        AdminFundMasterDTO fund = adminFundMapper.selectPendingFundEdit(fundCode);
        if (fund == null) {
            return null;
        }

        // 2. revision이 있으면 revision의 내용으로 덮어쓰기 (수정 가능한 필드만)
        FundMasterRevisionDTO revision = fundMasterRevisionMapper.selectPendingRevision(fundCode);
        if (revision != null && (revision.getRevStatus() != null &&
                (revision.getRevStatus().equals("대기") || revision.getRevStatus().equals("수정완료")))) {
            // revision의 수정 가능한 필드들을 fund에 반영
            if (revision.getInvestGrade() != null) {
                fund.setInvestGrade(revision.getInvestGrade());
            }
            if (revision.getFundFeature() != null) {
                fund.setFundFeature(revision.getFundFeature());
            }
            if (revision.getSubscriptionMethod() != null) {
                fund.setSubscriptionMethod(revision.getSubscriptionMethod());
            }
            if (revision.getNotice1() != null) {
                fund.setNotice1(revision.getNotice1());
            }
            if (revision.getNotice2() != null) {
                fund.setNotice2(revision.getNotice2());
            }
        }

        return fund;
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
        if (dto.getSubscriptionMethod() != null && !dto.getSubscriptionMethod().isBlank()) {
            revision.setSubscriptionMethod(dto.getSubscriptionMethod());
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

        // 데이터베이스의 현재 시간 사용 (fundOperateReserveJob과 동일하게)
        LocalDateTime now = adminFundMapper.selectCurrentDbTime();
        System.out.println("setFundReserveTime: FUND_CODE=" + fundCode + ", 예약시간=" + date + ", 현재시간(DB)=" + now);

        // 미래 시간으로 예약하는 경우: 배치가 처리하도록 예약 시간만 설정하고 종료
        // 현재 시간보다 1초 이상 미래인 경우만 미래로 판단 (초 단위 오차 방지)
        if (date.isAfter(now.plusSeconds(1))) {
            System.out.println("setFundReserveTime: 미래 시간으로 예약 - 배치가 처리하도록 예약 시간만 설정");
            adminFundMapper.setFundReserveTime(fundCode, date);
            return; // 배치가 처리하도록 revision은 그대로 유지
        }

        // 과거 또는 현재 시간으로 예약하는 경우: 즉시 적용
        System.out.println("setFundReserveTime: 과거/현재 시간으로 예약 - 즉시 적용 (DB SYSDATE 직접 사용)");
        // 즉시 반영인 경우 DB SYSDATE를 직접 사용하여 예약 시간 설정
        adminFundMapper.setFundReserveTimeWithSysdate(fundCode);
        
        FundMasterRevisionDTO completedRevision = fundMasterRevisionMapper.selectCompletedRevision(fundCode);
        System.out.println("setFundReserveTime: '수정완료' 상태인 revision 조회 결과: " + (completedRevision != null ? "있음 (REV_ID: " + completedRevision.getRevId() + ")" : "없음"));

        if (completedRevision != null) {
            Long revId = completedRevision.getRevId();
            System.out.println("setFundReserveTime: revision 즉시 적용 시작 - REV_ID: " + revId);
            fundMasterRevisionMapper.applyRevisionToMaster(revId);
            fundMasterRevisionMapper.updateRevisionStatusToApplied(revId);
            adminFundMapper.clearReserveTime(fundCode);
            // 반영예약 완료 후 revision 삭제 (즉시 적용된 경우에만)
            fundMasterRevisionMapper.deleteRevision(revId);
            System.out.println("setFundReserveTime: revision 즉시 적용 완료 및 삭제 - REV_ID: " + revId);
        } else {
            System.out.println("setFundReserveTime: '수정완료' 상태인 revision이 없어 상태만 변경");
            adminFundMapper.updateStatusToPending(fundCode);
        }
    }
}