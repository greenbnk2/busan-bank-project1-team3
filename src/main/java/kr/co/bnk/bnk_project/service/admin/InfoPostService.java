package kr.co.bnk.bnk_project.service.admin;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import kr.co.bnk.bnk_project.dto.admin.InfoAttachmentDTO;
import kr.co.bnk.bnk_project.dto.admin.InfoPostDTO;
import kr.co.bnk.bnk_project.mapper.admin.InfoAttachmentMapper;
import kr.co.bnk.bnk_project.mapper.admin.InfoPostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InfoPostService {

    @Value("${file.upload.path}")
    private String uploadPath;

    private final InfoPostMapper infoPostMapper;
    private final InfoAttachmentMapper infoAttachmentMapper;

    // 공시자료 등록 + 파일첨부
    public void createDisclosure(InfoPostDTO dto, MultipartFile attachment) {

        // 기본값
        if (dto.getStatus() == null || dto.getStatus().isBlank()) {
            dto.setStatus("PUBLISHED");
        }
        if (dto.getCreatedBy() == null || dto.getCreatedBy().isBlank()) {
            dto.setCreatedBy("admin");
        }

        // 글 먼저 등록
        infoPostMapper.insertInfoPost(dto);
        int postId = dto.getPostId();

        // 첨부파일 없으면 종료
        if (attachment == null || attachment.isEmpty()) {
            return;
        }

        // 실행된 프로젝트 기준 절대경로 가져오기
        String projectRoot = System.getProperty("user.dir");

        // 프로젝트/upload 폴더로 변환
        File uploadDir = new File(projectRoot, uploadPath);

        // 폴더 없으면 생성
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }


        System.out.println("최종 저장경로 = " + uploadDir.getAbsolutePath());


        // 원본파일명 + 확장자
        String oriName = attachment.getOriginalFilename();
        String ext = "";
        if (oriName != null && oriName.lastIndexOf(".") != -1) {
            ext = oriName.substring(oriName.lastIndexOf("."));
        }

        // 저장 파일명 (UUID)
        String savedName = UUID.randomUUID().toString() + ext;

        // 실제 저장할 파일 객체
        File saveFile = new File(uploadDir, savedName);

        try {
            attachment.transferTo(saveFile); // 파일 저장
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("첨부파일 저장 실패", e);
        }

        // DB에 저장
        InfoAttachmentDTO fileDTO = new InfoAttachmentDTO();
        fileDTO.setPostId(postId);
        fileDTO.setFileName(oriName);
        fileDTO.setFilePath(savedName);  // 저장파일명
        fileDTO.setSortOrder(1);

        infoAttachmentMapper.insertInfoAttachment(fileDTO);
    }

    // 상세 확인
    public InfoPostDTO findInfoPostById(int postId) {
        return infoPostMapper.selectInfoPostById(postId);
    }

    // 목록 전체
    public List<InfoPostDTO> findAllInfoPost(){
        return infoPostMapper.selectAllInfoPost();
    }
}
