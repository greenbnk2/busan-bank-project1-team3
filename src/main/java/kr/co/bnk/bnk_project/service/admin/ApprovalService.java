package kr.co.bnk.bnk_project.service.admin;

import kr.co.bnk.bnk_project.dto.CsDTO;
import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
import kr.co.bnk.bnk_project.dto.admin.ApprovalDTO;
import kr.co.bnk.bnk_project.dto.admin.AdminFundMasterDTO;
import kr.co.bnk.bnk_project.dto.admin.FundMasterRevisionDTO;
import kr.co.bnk.bnk_project.dto.admin.FieldChangeDTO;
import kr.co.bnk.bnk_project.mapper.admin.ApprovalMapper;
import kr.co.bnk.bnk_project.mapper.admin.FundMasterRevisionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ApprovalService {
    private final ApprovalMapper approvalMapper;
    private final AdminFundService adminFundService;
    private final FundMasterRevisionMapper fundMasterRevisionMapper;


    public void insertApproval(ApprovalDTO approvalDTO) {
        approvalMapper.insertApproval(approvalDTO);

        String fundCode = approvalDTO.getFundCode();
        if (fundCode == null || fundCode.isBlank()) {
            return;
        }

        AdminFundMasterDTO currentFund = adminFundService.getPendingFundEdit(fundCode);
        if (currentFund == null) {
            return;
        }

        String operStatus = currentFund.getOperStatus() != null ? currentFund.getOperStatus().trim() : "";
        String updateStat = currentFund.getUpdateStat() != null ? currentFund.getUpdateStat().trim() : null;

        if (("등록".equals(operStatus) && (updateStat == null || updateStat.isEmpty())) 
            || ("운용중".equals(operStatus) && "수정".equals(updateStat))) {
            adminFundService.updateOperStatus(fundCode);
        }
    }


    /** 결재 대기 목록 **/
    public PageResponseDTO<ApprovalDTO> selectpendingApproval(PageRequestDTO pageRequestDTO) {

        if (pageRequestDTO.getPg() <= 0) pageRequestDTO.setPg(1);
        if (pageRequestDTO.getSize() <= 0) pageRequestDTO.setSize(10);


        List<ApprovalDTO> list = approvalMapper.selectPendingApprovals(pageRequestDTO);
        int total = approvalMapper.selectPendingApprovalsTotal(pageRequestDTO);

        return PageResponseDTO.of(pageRequestDTO, list, total);
    }

    public List<ApprovalDTO> selectRecentApprovalHistory() {
        return approvalMapper.selectRecentApprovalHistory();
    }

    /** 결재 이력 목록 **/
    public PageResponseDTO<ApprovalDTO> selectApprovalHistory(PageRequestDTO pageRequestDTO) {

        if (pageRequestDTO.getPg() <= 0) pageRequestDTO.setPg(1);
        if (pageRequestDTO.getSize() <= 0) pageRequestDTO.setSize(10);

        List<ApprovalDTO> list = approvalMapper.selectApprovalHistory(pageRequestDTO);
        int total = approvalMapper.selectApprovalHistoryTotal(pageRequestDTO);

        return PageResponseDTO.of(pageRequestDTO, list, total);
    }

    @Transactional
    public void approvalFund(ApprovalDTO approvalDTO) {

        approvalMapper.approvalFund(approvalDTO);
        
        String approvedBy = approvalDTO.getApprover() != null ? approvalDTO.getApprover() : "system";
        
        // 승인 시 FUND_MASTER_REVISION의 REV_STATUS를 '수정완료'로 변경
        if ("승인".equals(approvalDTO.getStatus())) {
            fundMasterRevisionMapper.updateRevisionStatusToCompleted(
                approvalDTO.getFundCode(), 
                approvedBy
            );
        }
        // 반려 시 FUND_MASTER_REVISION의 REV_STATUS를 '수정반려'로 변경
        else if ("반려".equals(approvalDTO.getStatus())) {
            fundMasterRevisionMapper.updateRevisionStatusToRejected(
                approvalDTO.getFundCode(), 
                approvedBy
            );
        }
        
        adminFundService.updateStatusAfterApproval(approvalDTO.getFundCode(), approvalDTO.getStatus());
    }

    /**
     * 결재 상세 조회 (변경사항 포함)
     */
    public ApprovalDTO getApprovalDetail(Long apprNo) {
        return approvalMapper.selectApprovalByNo(apprNo);
    }

    /**
     * 수정일 때 변경사항 비교 (FUND_MASTER_REVISION vs FUND_MASTER)
     */
    public List<FieldChangeDTO> compareRevisionWithMaster(String fundCode) {
        List<FieldChangeDTO> changes = new ArrayList<>();

        // FUND_MASTER 현재 데이터 조회
        FundMasterRevisionDTO currentMaster = fundMasterRevisionMapper.selectFundMasterForRevision(fundCode);
        if (currentMaster == null) {
            return changes;
        }

        // FUND_MASTER_REVISION 대기 중인 수정 데이터 조회
        FundMasterRevisionDTO revision = fundMasterRevisionMapper.selectPendingRevision(fundCode);
        if (revision == null) {
            return changes;
        }

        // 각 필드 비교
        compareField(changes, "펀드단축코드", currentMaster.getFundShortCode(), revision.getFundShortCode());
        compareField(changes, "펀드명", currentMaster.getFundName(), revision.getFundName());
        compareField(changes, "자산운용사ID", currentMaster.getAssetManagerId(), revision.getAssetManagerId());
        compareField(changes, "설립일자", formatDate(currentMaster.getSetupDate()), formatDate(revision.getSetupDate()));
        compareField(changes, "초기NAV", formatNumber(currentMaster.getInitialNav()), formatNumber(revision.getInitialNav()));
        compareField(changes, "펀드유형", currentMaster.getFundType(), revision.getFundType());
        compareField(changes, "투자지역", currentMaster.getInvestRegion(), revision.getInvestRegion());
        compareField(changes, "분류코드", currentMaster.getClassifyCode(), revision.getClassifyCode());
        compareField(changes, "공사구분", currentMaster.getPublicPrivateType(), revision.getPublicPrivateType());
        compareField(changes, "수탁회사", currentMaster.getTrusteeCompany(), revision.getTrusteeCompany());
        compareField(changes, "판매회사", currentMaster.getSalesCompany(), revision.getSalesCompany());
        compareField(changes, "관리회사", currentMaster.getAdminCompany(), revision.getAdminCompany());
        compareField(changes, "운용기간유형", currentMaster.getOperPeriodType(), revision.getOperPeriodType());
        compareField(changes, "단위형태", currentMaster.getIsUnitType(), revision.getIsUnitType());
        compareField(changes, "투자등급", currentMaster.getInvestGrade(), revision.getInvestGrade());
        compareField(changes, "펀드특징", currentMaster.getFundFeature(), revision.getFundFeature());
        compareField(changes, "클래스명", currentMaster.getClassName(), revision.getClassName());
        compareField(changes, "개요", currentMaster.getOverview(), revision.getOverview());
        compareField(changes, "환매방법", currentMaster.getRedemptionMethod(), revision.getRedemptionMethod());
        compareField(changes, "거래방법", currentMaster.getTradeMethod(), revision.getTradeMethod());
        compareField(changes, "가입경로", currentMaster.getSubscriptionMethod(), revision.getSubscriptionMethod());
        compareField(changes, "신탁관리", currentMaster.getTrustManagement(), revision.getTrustManagement());
        compareField(changes, "지급ID", currentMaster.getPaymentId(), revision.getPaymentId());
        compareField(changes, "공지사항1", currentMaster.getNotice1(), revision.getNotice1());
        compareField(changes, "공지사항2", currentMaster.getNotice2(), revision.getNotice2());
        compareField(changes, "지역유형", currentMaster.getRgnType(), revision.getRgnType());
        compareField(changes, "성과유형", currentMaster.getPrfmType(), revision.getPrfmType());

        return changes;
    }

    private void compareField(List<FieldChangeDTO> changes, String fieldName, Object oldValue, Object newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            changes.add(FieldChangeDTO.builder()
                    .fieldName(fieldName)
                    .oldValue(formatValue(oldValue))
                    .newValue(formatValue(newValue))
                    .build());
        }
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    private String formatDate(java.time.LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.toString();
    }

    private String formatNumber(java.math.BigDecimal number) {
        if (number == null) {
            return "";
        }
        return number.toString();
    }

}
