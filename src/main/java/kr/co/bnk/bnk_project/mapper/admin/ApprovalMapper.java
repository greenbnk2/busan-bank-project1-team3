package kr.co.bnk.bnk_project.mapper.admin;

import kr.co.bnk.bnk_project.dto.CsDTO;
import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.admin.ApprovalDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApprovalMapper {
    void insertApproval(ApprovalDTO approvalDTO);

    List<ApprovalDTO> selectPendingApprovals(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    int selectPendingApprovalsTotal(@Param("pageRequestDTO")PageRequestDTO pageRequestDTO);

    List<ApprovalDTO> selectRecentApprovalHistory();

    List<ApprovalDTO> selectApprovalHistory(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    int selectApprovalHistoryTotal(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    void approvalFund(ApprovalDTO approvalDTO);

    ApprovalDTO selectApprovalByNo(@Param("apprNo") Long apprNo);
}
