package kr.co.bnk.bnk_project.mapper.admin;

import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.admin.AdminFundMasterDTO;
import kr.co.bnk.bnk_project.dto.admin.FundDocumentDTO;
import kr.co.bnk.bnk_project.dto.admin.ProductListDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminFundMapper {

    /*펀드를 검색 타입 + 키워드로 조회*/
    AdminFundMasterDTO selectPendingFund(PageRequestDTO pageRequestDTO);



    /*등록*/
    List<AdminFundMasterDTO> selectFundSuggestions(@Param("searchType") String searchType,
                                                   @Param("keyword") String keyword);

    void updateFundForRegister(AdminFundMasterDTO dto);



    /*수정*/
    AdminFundMasterDTO selectPendingFundEdit(@Param("fundCode") String fundCode);

    void updateFundForEdit(AdminFundMasterDTO dto);
    void updateStatus(AdminFundMasterDTO dto);


    void stopFund(@Param("fundCode") String fundCode);
    void resumeFund(@Param("fundCode") String fundCode);

    /*------------------------------------------------------------------------------------*/
    // 페이징 목록 조회
    List<ProductListDTO> selectProductList(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    // 전체 갯수 조회
    int selectProductTotal(@Param("pageRequestDTO")PageRequestDTO pageRequestDTO);

    void updateOperStatus(@Param("fundCode") String fundCode);  // 추가

    void updateStatusAfterApproval(@Param("fundCode") String fundCode,
                                   @Param("status") String status);



    // 예약

    List<AdminFundMasterDTO> selectFundsForOperateReserve();

    void updateOperStatusToRunning(@Param("fundCode") String fundCode,
                                   @Param("lastUpdId") String lastUpdId);

    void setFundReserveTime(@Param("fundCode") String fundCode,
                            @Param("operStartAt") LocalDateTime operStartAt);

    // 상태를 운용대기로 변경 (등록완료일 때만)
    void updateStatusToPending(@Param("fundCode") String fundCode);
    
    // 예약 시간이 지난 모든 펀드 조회 (revision 적용 배치용)
    List<AdminFundMasterDTO> selectFundsWithExpiredReserveTime();
    
    // 예약 시간 초기화 (revision 적용 후)
    void clearReserveTime(@Param("fundCode") String fundCode);


    // 문서 (약관, 투자설명서, 간이투자설명서)

    // 문서 (등록/수정 공통)
    void insertFundDocument(FundDocumentDTO dto);

    // 문서 조회
    List<FundDocumentDTO> selectFundDocuments(@Param("fundCode") String fundCode);

    // 문서 삭제 (펀드 + 타입 기준, 새 파일로 교체할 때)
    void deleteFundDocumentsByType(@Param("fundCode") String fundCode,
                                   @Param("docType") String docType);


}

