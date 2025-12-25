package kr.co.bnk.bnk_project.mapper.admin;

import kr.co.bnk.bnk_project.dto.admin.InfoAttachmentDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InfoAttachmentMapper {

    // 첨부파일 등록
    public void insertInfoAttachment(InfoAttachmentDTO infoAttachmentDTO);

    // 첨부파일 조회
    public List<InfoAttachmentDTO> selectByPostId(int postId);

    // 첨부파일 삭제 (수정/삭제)
    public void deleteByPostId(int postId);

}
