package kr.co.bnk.bnk_project.service;

import kr.co.bnk.bnk_project.dto.CsDTO;
import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
import kr.co.bnk.bnk_project.mapper.CsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CsService {

    private final CsMapper csMapper;

    /** faq 목록 **/
    public PageResponseDTO<CsDTO> getFaqPage(PageRequestDTO pageRequestDTO) {

        if (pageRequestDTO.getPg() <= 0) pageRequestDTO.setPg(1);
        if (pageRequestDTO.getSize() <= 0) pageRequestDTO.setSize(10);

        pageRequestDTO.setCate("faq");

        List<CsDTO> list = csMapper.selectFaqList(pageRequestDTO);
        int total = csMapper.selectFaqTotal(pageRequestDTO);

        return PageResponseDTO.of(pageRequestDTO, list, total);
    }

    /* faq 등록 */

    public void insertFaq(CsDTO csDTO) {
        csMapper.insertFaq(csDTO);
    }

    /* FAQ 수정 */
    // FAQ 단건 조회
    public CsDTO getFaq(Long csId) {
        return csMapper.selectFaqById(csId);
    }

    // FAQ 수정
    public void updateFaq(CsDTO csDTO) {
        // 혹시라도 카테고리 바뀌면 안 되니까 고정
        csDTO.setCategoryId(8L);      // Long 타입이면 8L
        csDTO.setStatus("답변완료");   // 수정 후에도 완료 상태 유지
        csMapper.updateFaq(csDTO);
    }
    /* FAQ 삭제 */
    @Transactional
    public void deleteFaq(Long csId) {
        csMapper.deleteFaq(csId);
    }


    /**---------------- QNA---------------------- **/
    /** QNA 목록 **/
    public PageResponseDTO<CsDTO> getQnaPage(PageRequestDTO pageRequestDTO) {
        if (pageRequestDTO.getPg() <= 0) pageRequestDTO.setPg(1);
        if (pageRequestDTO.getSize() <= 0) pageRequestDTO.setSize(10);

        pageRequestDTO.setCate("qna");

        List<CsDTO> list = csMapper.selectQnaList(pageRequestDTO);
        int total = csMapper.selectQnaTotal(pageRequestDTO);

        return PageResponseDTO.of(pageRequestDTO, list, total);
    }


    /* QNA 상세 */
    public CsDTO getQnaDetail(Long csId) {
        return csMapper.selectQnaDetail(csId);
    }


    /* QNA 수정 */
    // QNA 단건 조회
    public CsDTO getQna(Long csId) {
        return csMapper.selectQnaById(csId);
    }

    // QNA 답변 업데이트
    public void updateQnaAnswer(CsDTO csDTO) {

        csDTO.setStatus("답변완료");  // 혹시라도 파라미터 조작 방지
        csMapper.updateQnaAnswer(csDTO);
    }

    /* qna 삭제 */
    @Transactional
    public void deleteQna(Long csId) {
        csMapper.deleteQna(csId);
    }


    /* QNA 등록 (사용자 1:1 문의 작성) */
    public void registerInquiry(CsDTO csDTO) {csMapper.insertQna(csDTO);}

}
