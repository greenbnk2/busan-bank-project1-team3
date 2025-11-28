package kr.co.bnk.bnk_project.mapper.admin;

import kr.co.bnk.bnk_project.dto.admin.FundMasterRevisionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FundMasterRevisionMapper {
    
    // 수정 이력 저장
    void insertRevision(FundMasterRevisionDTO revision);
    
    // 승인 대기 중인 수정 이력 조회
    FundMasterRevisionDTO selectPendingRevision(@Param("fundCode") String fundCode);
    
    // 승인 완료된 수정 이력 조회 (예약 시간이 지난 것)
    List<FundMasterRevisionDTO> selectRevisionsToApply();
    
    // 승인 처리
    void approveRevision(@Param("revId") Long revId, 
                        @Param("approvedBy") String approvedBy);
    
    // 수정사항을 FUND_MASTER에 반영
    void applyRevisionToMaster(@Param("revId") Long revId);
    
    // 예약 시간 설정
    int setApplyTime(@Param("revId") Long revId, 
                     @Param("applyAt") LocalDateTime applyAt);
    
    // FUND_MASTER 전체 데이터 조회 (revision 생성용)
    FundMasterRevisionDTO selectFundMasterForRevision(@Param("fundCode") String fundCode);
    
    // 승인 시 REV_STATUS를 '수정완료'로 변경 (FUND_CODE 기준)
    void updateRevisionStatusToCompleted(@Param("fundCode") String fundCode,
                                         @Param("approvedBy") String approvedBy);
    
    // 반려 시 REV_STATUS를 '수정반려'로 변경 (FUND_CODE 기준)
    void updateRevisionStatusToRejected(@Param("fundCode") String fundCode,
                                       @Param("approvedBy") String approvedBy);
    
    // REV_STATUS를 '적용완료'로 변경
    void updateRevisionStatusToApplied(@Param("revId") Long revId);
    
    // 디버깅용: 모든 revision 상태 조회
    List<FundMasterRevisionDTO> selectAllRevisionsForDebug();
    
    // 수정완료된 revision 조회 (예약용)
    FundMasterRevisionDTO selectCompletedRevision(@Param("fundCode") String fundCode);
}
