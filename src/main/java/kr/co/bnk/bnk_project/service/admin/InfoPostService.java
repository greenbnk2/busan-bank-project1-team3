package kr.co.bnk.bnk_project.service.admin;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
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

    ///////////////////////////////////////
    /////////////공시자료///////////////////
    //////////////////////////////////////

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

    // 공시자료 상세 확인
    public InfoPostDTO findInfoPostById(int postId) {
        return infoPostMapper.selectInfoPostById(postId);
    }

    // 공시자료 목록 전체 (페이징 사용으로 주석처리)
    /*
    public List<InfoPostDTO> findAllInfoPost(){
        return infoPostMapper.selectAllInfoPost();
    }
    */

    // 공시자료 페이징 목록
    public PageResponseDTO<InfoPostDTO> findInfoPostPage(PageRequestDTO pageRequestDTO) {

        // 방어 코드
        if (pageRequestDTO.getPg() <= 0) pageRequestDTO.setPg(1);
        if (pageRequestDTO.getSize() <= 0) pageRequestDTO.setSize(10);

        // 목록
        List<InfoPostDTO> list = infoPostMapper.selectInfoPostList(pageRequestDTO);

        // 총 개수
        int total = infoPostMapper.selectInfoPostTotal(pageRequestDTO);


        return PageResponseDTO.of(pageRequestDTO, list, total);


    }

    // 공시자료 수정 (글 + 파일)
    public void updateDisclosure(InfoPostDTO dto, MultipartFile attachment) {

        // 글 수정
        infoPostMapper.updateInfoPost(dto);

        // 첨부파일 없으면 기존 파일 그대로 사용
        if (attachment == null || attachment.isEmpty()) {
            return;
        }

        // 기존 첨부파일 조회 (여러 개일 수도 있음)
        List<InfoAttachmentDTO> oldFiles = infoAttachmentMapper.selectByPostId(dto.getPostId());

        String projectRoot = System.getProperty("user.dir");
        File uploadDir = new File(projectRoot, uploadPath);

        if (!uploadDir.exists()) uploadDir.mkdirs();

        // 기존 파일 삭제
        if (oldFiles != null && !oldFiles.isEmpty()) {

            for (InfoAttachmentDTO old : oldFiles) {

                if (old == null) continue;
                if (old.getFilePath() == null || old.getFilePath().isBlank()) continue;

                File delFile = new File(uploadDir, old.getFilePath());
                if (delFile.exists()) delFile.delete();
            }

            // DB 삭제
            infoAttachmentMapper.deleteByPostId(dto.getPostId());
        }

        // 새 파일 저장
        String oriName = attachment.getOriginalFilename();
        String ext = "";

        if (oriName != null && oriName.lastIndexOf(".") != -1) {
            ext = oriName.substring(oriName.lastIndexOf("."));
        }

        String savedName = UUID.randomUUID().toString() + ext;

        File saveFile = new File(uploadDir, savedName);

        try {
            attachment.transferTo(saveFile);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }

        // 새 파일 DB 등록
        InfoAttachmentDTO newFile = new InfoAttachmentDTO();
        newFile.setPostId(dto.getPostId());
        newFile.setFileName(oriName);
        newFile.setFilePath(savedName);
        newFile.setSortOrder(1);

        infoAttachmentMapper.insertInfoAttachment(newFile);
    }


    // 공시자료 삭제
    public void deleteDisclosure(int postId) {

        String projectRoot = System.getProperty("user.dir");
        File uploadDir = new File(projectRoot, uploadPath);

        // 첨부파일 여러 개 조회
        List<InfoAttachmentDTO> oldFiles = infoAttachmentMapper.selectByPostId(postId);

        if (oldFiles != null && !oldFiles.isEmpty()) {
            for (InfoAttachmentDTO old : oldFiles) {

                // old 자체가 null 일 경우 skip
                if (old == null) continue;

                // filePath 가 null 이면 skip
                if (old.getFilePath() == null || old.getFilePath().isBlank()) continue;

                File delFile = new File(uploadDir, old.getFilePath());
                if (delFile.exists()) delFile.delete();
            }

            // DB 삭제
            infoAttachmentMapper.deleteByPostId(postId);
        }

        // 게시글 삭제
        infoPostMapper.deleteInfoPost(postId);
    }

    ////////////////////////////////////
    ///////////   수시공시  /////////////
    //////////////////////////////////
    // 수시공시 등록
    public void createAdHoc(InfoPostDTO infoPostDTO) {
        // 기본값
        if (infoPostDTO.getStatus() == null || infoPostDTO.getStatus().isBlank()) {
            infoPostDTO.setStatus("PUBLISHED");
        }
        if (infoPostDTO.getCreatedBy() == null || infoPostDTO.getCreatedBy().isBlank()) {
            infoPostDTO.setCreatedBy("admin");
        }

        infoPostMapper.insertAdHoc(infoPostDTO);
    }

    // 수시공시 목록 전체 (페이징 사용으로 주석처리)
    /*
    public List<InfoPostDTO> findAllAdHoc(){
        return infoPostMapper.selectAllAdHoc();
    }
    */

    // 수시공시 페이징 목록
    public PageResponseDTO<InfoPostDTO> findAdHocPage(PageRequestDTO pageRequestDTO) {

        // 방어 코드
        if (pageRequestDTO.getPg() <= 0) pageRequestDTO.setPg(1);
        if (pageRequestDTO.getSize() <= 0) pageRequestDTO.setSize(10);

        // 목록
        List<InfoPostDTO> list = infoPostMapper.selectAdHocList(pageRequestDTO);

        // 총 개수
        int total = infoPostMapper.selectAdHocTotal(pageRequestDTO);


        return PageResponseDTO.of(pageRequestDTO, list, total);


    }

    public InfoPostDTO selectAdHocById(int postId) {
        return infoPostMapper.selectAdHocById(postId);
    }

    public void updateAdHoc(InfoPostDTO infoPostDTO) {
        infoPostMapper.updateAdHoc(infoPostDTO);
    }

    public void deleteAdHoc(int postId) {
        infoPostMapper.deleteAdHoc(postId);
    }

    ////////////////////////////////////
    ///////////   펀도정보  /////////////
    //////////////////////////////////
    // 펀드정보 등록
    public void createFundInfo(InfoPostDTO infoPostDTO) {
        // 기본값
        if (infoPostDTO.getStatus() == null || infoPostDTO.getStatus().isBlank()) {
            infoPostDTO.setStatus("PUBLISHED");
        }
        if (infoPostDTO.getCreatedBy() == null || infoPostDTO.getCreatedBy().isBlank()) {
            infoPostDTO.setCreatedBy("admin");
        }

        infoPostMapper.insertFundInfo(infoPostDTO);
    }

    // 펀드정보 목록 전체 (페이징 사용으로 주석처리)
    /*
    public List<InfoPostDTO> findAllFundInfo(){
        return infoPostMapper.selectAllFundInfo();
    }
    */

    // 펀드정보 페이징 목록
    public PageResponseDTO<InfoPostDTO> findFundInfoPage(PageRequestDTO pageRequestDTO) {

        // 방어 코드
        if (pageRequestDTO.getPg() <= 0) pageRequestDTO.setPg(1);
        if (pageRequestDTO.getSize() <= 0) pageRequestDTO.setSize(10);

        // 목록
        List<InfoPostDTO> list = infoPostMapper.selectFundInfoList(pageRequestDTO);

        // 총 개수
        int total = infoPostMapper.selectFundInfoTotal(pageRequestDTO);


        return PageResponseDTO.of(pageRequestDTO, list, total);


    }

    public InfoPostDTO selectFundInfoById(int postId) {
        return infoPostMapper.selectFundInfoById(postId);
    }

    public void updateFundInfo(InfoPostDTO infoPostDTO) {
        infoPostMapper.updateFundInfo(infoPostDTO);
    }

    public void deleteFundInfo(int postId) {
        infoPostMapper.deleteFundInfo(postId);
    }




    ////////////////////////////////////
    /////////// 펀드 가이드 /////////////
    //////////////////////////////////

    // 펀드가이드 등록 + 파일첨부
    public void createGuide(InfoPostDTO dto, MultipartFile attachment) {

        // 기본값
        if (dto.getStatus() == null || dto.getStatus().isBlank()) {
            dto.setStatus("PUBLISHED");
        }
        if (dto.getCreatedBy() == null || dto.getCreatedBy().isBlank()) {
            dto.setCreatedBy("admin");
        }

        // 글 먼저 등록
        infoPostMapper.insertFundGuide(dto);
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

    // 펀드 가이드 목록 전체 (페이징 사용으로 주석처리)
    /*
    public List<InfoPostDTO> findAllFundGuide() {
        return infoPostMapper.selectAllFundGuide();
    }
    */

    // 펀드 가이드 페이징 목록
    public PageResponseDTO<InfoPostDTO> findFundGuidePage(PageRequestDTO pageRequestDTO) {

        // 방어 코드
        if (pageRequestDTO.getPg() <= 0) pageRequestDTO.setPg(1);
        if (pageRequestDTO.getSize() <= 0) pageRequestDTO.setSize(10);

        // 목록
        List<InfoPostDTO> list = infoPostMapper.selectFundGuideList(pageRequestDTO);

        // 총 개수
        int total = infoPostMapper.selectFundGuideTotal(pageRequestDTO);


        return PageResponseDTO.of(pageRequestDTO, list, total);


    }

    // 펀드 가이드 상세
    public InfoPostDTO findFundGuideById(int postId) {
        return infoPostMapper.selectFundGuideById(postId);
    }

    // 펀드 가이드 수정 (글 + 파일)
    public void updateGuide(InfoPostDTO dto, MultipartFile attachment) {

        // 글 수정
        infoPostMapper.updateFundGuide(dto);

        // 첨부파일 없으면 기존 파일 그대로 사용
        if (attachment == null || attachment.isEmpty()) {
            return;
        }

        // 기존 첨부파일 조회 (여러 개일 수도 있음)
        List<InfoAttachmentDTO> oldFiles = infoAttachmentMapper.selectByPostId(dto.getPostId());

        String projectRoot = System.getProperty("user.dir");
        File uploadDir = new File(projectRoot, uploadPath);

        if (!uploadDir.exists()) uploadDir.mkdirs();

        // 기존 파일 삭제
        if (oldFiles != null && !oldFiles.isEmpty()) {

            for (InfoAttachmentDTO old : oldFiles) {

                if (old == null) continue;
                if (old.getFilePath() == null || old.getFilePath().isBlank()) continue;

                File delFile = new File(uploadDir, old.getFilePath());
                if (delFile.exists()) delFile.delete();
            }

            // DB 삭제
            infoAttachmentMapper.deleteByPostId(dto.getPostId());
        }

        // 새 파일 저장
        String oriName = attachment.getOriginalFilename();
        String ext = "";

        if (oriName != null && oriName.lastIndexOf(".") != -1) {
            ext = oriName.substring(oriName.lastIndexOf("."));
        }

        String savedName = UUID.randomUUID().toString() + ext;

        File saveFile = new File(uploadDir, savedName);

        try {
            attachment.transferTo(saveFile);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }

        // 새 파일 DB 등록
        InfoAttachmentDTO newFile = new InfoAttachmentDTO();
        newFile.setPostId(dto.getPostId());
        newFile.setFileName(oriName);
        newFile.setFilePath(savedName);
        newFile.setSortOrder(1);

        infoAttachmentMapper.insertInfoAttachment(newFile);
    }

    // 펀드 가이드 삭제
    public void deleteGuide(int postId) {

        String projectRoot = System.getProperty("user.dir");
        File uploadDir = new File(projectRoot, uploadPath);

        // 첨부파일 여러 개 조회
        List<InfoAttachmentDTO> oldFiles = infoAttachmentMapper.selectByPostId(postId);

        if (oldFiles != null && !oldFiles.isEmpty()) {
            for (InfoAttachmentDTO old : oldFiles) {

                // old 자체가 null 일 경우 skip
                if (old == null) continue;

                // filePath 가 null 이면 skip
                if (old.getFilePath() == null || old.getFilePath().isBlank()) continue;

                File delFile = new File(uploadDir, old.getFilePath());
                if (delFile.exists()) delFile.delete();
            }

            // DB 삭제
            infoAttachmentMapper.deleteByPostId(postId);
        }

        // 게시글 삭제
        infoPostMapper.deleteFundGuide(postId);
    }

    ///////////////////////////////////////
    /////////////펀드 시황///////////////////
    //////////////////////////////////////

    // 펀드시황 등록 + 파일첨부
    public void createMarket(InfoPostDTO dto, MultipartFile attachment) {

        // 기본값
        if (dto.getStatus() == null || dto.getStatus().isBlank()) {
            dto.setStatus("PUBLISHED");
        }
        if (dto.getCreatedBy() == null || dto.getCreatedBy().isBlank()) {
            dto.setCreatedBy("admin");
        }

        // 글 먼저 등록
        infoPostMapper.insertFundMarket(dto);
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

    // 펀드 시황 목록 전체 (페이징 사용으로 주석처리)
    /*
    public List<InfoPostDTO> findAllFundMarket() {
        return infoPostMapper.selectAllFundMarket();
    }
    */

    // 펀드 시황 페이징 목록
    public PageResponseDTO<InfoPostDTO> findFundMarketPage(PageRequestDTO pageRequestDTO) {

        // 방어 코드
        if (pageRequestDTO.getPg() <= 0) pageRequestDTO.setPg(1);
        if (pageRequestDTO.getSize() <= 0) pageRequestDTO.setSize(10);

        // 목록
        List<InfoPostDTO> list = infoPostMapper.selectFundMarketList(pageRequestDTO);

        // 총 개수
        int total = infoPostMapper.selectFundMarketTotal(pageRequestDTO);


        return PageResponseDTO.of(pageRequestDTO, list, total);


    }

    // 펀드 시황 상세
    public InfoPostDTO findFundMarketById(int postId) {
        return infoPostMapper.selectFundMarketById(postId);
    }

    // 펀드 시황 수정 (글 + 파일)
    public void updateMarket(InfoPostDTO dto, MultipartFile attachment) {

        // 글 수정
        infoPostMapper.updateFundMarket(dto);

        // 첨부파일 없으면 기존 파일 그대로 사용
        if (attachment == null || attachment.isEmpty()) {
            return;
        }

        // 기존 첨부파일 조회 (여러 개일 수도 있음)
        List<InfoAttachmentDTO> oldFiles = infoAttachmentMapper.selectByPostId(dto.getPostId());

        String projectRoot = System.getProperty("user.dir");
        File uploadDir = new File(projectRoot, uploadPath);

        if (!uploadDir.exists()) uploadDir.mkdirs();

        // 기존 파일 삭제
        if (oldFiles != null && !oldFiles.isEmpty()) {

            for (InfoAttachmentDTO old : oldFiles) {

                if (old == null) continue;
                if (old.getFilePath() == null || old.getFilePath().isBlank()) continue;

                File delFile = new File(uploadDir, old.getFilePath());
                if (delFile.exists()) delFile.delete();
            }

            // DB 삭제
            infoAttachmentMapper.deleteByPostId(dto.getPostId());
        }

        // 새 파일 저장
        String oriName = attachment.getOriginalFilename();
        String ext = "";

        if (oriName != null && oriName.lastIndexOf(".") != -1) {
            ext = oriName.substring(oriName.lastIndexOf("."));
        }

        String savedName = UUID.randomUUID().toString() + ext;

        File saveFile = new File(uploadDir, savedName);

        try {
            attachment.transferTo(saveFile);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }

        // 새 파일 DB 등록
        InfoAttachmentDTO newFile = new InfoAttachmentDTO();
        newFile.setPostId(dto.getPostId());
        newFile.setFileName(oriName);
        newFile.setFilePath(savedName);
        newFile.setSortOrder(1);

        infoAttachmentMapper.insertInfoAttachment(newFile);
    }

    // 펀드 시황 삭제
    public void deleteMarket(int postId) {

        String projectRoot = System.getProperty("user.dir");
        File uploadDir = new File(projectRoot, uploadPath);

        // 첨부파일 여러 개 조회
        List<InfoAttachmentDTO> oldFiles = infoAttachmentMapper.selectByPostId(postId);

        if (oldFiles != null && !oldFiles.isEmpty()) {
            for (InfoAttachmentDTO old : oldFiles) {

                // old 자체가 null 일 경우 skip
                if (old == null) continue;

                // filePath 가 null 이면 skip
                if (old.getFilePath() == null || old.getFilePath().isBlank()) continue;

                File delFile = new File(uploadDir, old.getFilePath());
                if (delFile.exists()) delFile.delete();
            }

            // DB 삭제
            infoAttachmentMapper.deleteByPostId(postId);
        }

        // 게시글 삭제
        infoPostMapper.deleteFundMarket(postId);
    }




}
