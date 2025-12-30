package kr.co.bnk.bnk_project.mapper;

import kr.co.bnk.bnk_project.dto.CsDTO;
import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CsMapper {

    /* FAQ */
    List<CsDTO> selectFaqList(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    int selectFaqTotal(@Param("pageRequestDTO")PageRequestDTO pageRequestDTO);
    /* FAQ  등록*/
    void insertFaq(CsDTO csDTO);
    /* FAQ 수정 */
    CsDTO selectFaqById(@Param("csId") Long csId);
    void updateFaq(CsDTO csDTO);
    /* FAQ 삭제 */
    int deleteFaq(@Param("csId") Long csId);


    /* QNA */
    List<CsDTO> selectQnaList(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    int selectQnaTotal(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    /* QNA 상세 */
    CsDTO selectQnaDetail(@Param("csId") Long csId);
    /* QNA 답변 */
    CsDTO selectQnaById(@Param("csId") Long csId);
    void updateQnaAnswer(CsDTO csDTO);
    /* QnA 1:1문의 등록 */
    void insertQna(CsDTO csDTO);
    /* QnA 삭제 */
    int deleteQna(@Param("csId") Long csId);

    /* Flutter API용 */
    /* 내 문의 내역 조회 (CATEGORY JOIN 포함) */
    List<CsDTO> selectMyInquiries(@Param("userId") String userId);
    
    /* 문의 등록 (동적 CATEGORY_ID) */
    void insertInquiry(CsDTO csDTO);
}
