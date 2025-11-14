package kr.co.bnk.bnk_project.mapper.admin;

import kr.co.bnk.bnk_project.dto.admin.InfoAttachmentDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InfoAttachmentMapper {

    public void insertInfoAttachment(InfoAttachmentDTO infoAttachmentDTO);
}
