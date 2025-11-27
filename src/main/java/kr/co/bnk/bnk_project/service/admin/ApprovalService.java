package kr.co.bnk.bnk_project.service.admin;

import kr.co.bnk.bnk_project.dto.CsDTO;
import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
import kr.co.bnk.bnk_project.dto.admin.ApprovalDTO;
import kr.co.bnk.bnk_project.mapper.admin.ApprovalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalService {
    private final ApprovalMapper approvalMapper;
    private final AdminFundService adminFundService;


    public void insertApproval(ApprovalDTO approvalDTO) {
        approvalMapper.insertApproval(approvalDTO);

            adminFundService.updateOperStatus(approvalDTO.getFundCode());

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
        adminFundService.updateStatusAfterApproval(approvalDTO.getFundCode(), approvalDTO.getStatus());
    }

}
