package kr.co.bnk.bnk_project.mapper.admin;

import kr.co.bnk.bnk_project.dto.admin.InfoPostDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InfoPostMapper {

    // 공시자료
    public void insertInfoPost(InfoPostDTO infoPostDTO);
    public InfoPostDTO selectInfoPostById(int postId);
    public List<InfoPostDTO> selectAllInfoPost();
    public void updateInfoPost(InfoPostDTO infoPostDTO);
    public void deleteInfoPost(int postId);

    // 수시공시
    public void insertAdHoc(InfoPostDTO infoPostDTO);
    public InfoPostDTO selectAdHocById(int postId);
    public List<InfoPostDTO> selectAllAdHoc();
    public void updateAdHoc(InfoPostDTO infoPostDTO);
    public void deleteAdHoc(int postId);

    // 펀드정보
    public void insertFundInfo(InfoPostDTO infoPostDTO);
    public InfoPostDTO selectFundInfoById(int postId);
    public List<InfoPostDTO> selectAllFundInfo();
    public void updateFundInfo(InfoPostDTO infoPostDTO);
    public void deleteFundInfo(int postId);

    // 펀드 가이드
    public void insertFundGuide(InfoPostDTO infoPostDTO);
    public InfoPostDTO selectFundGuideById(int postId);
    public List<InfoPostDTO> selectAllFundGuide();
    public void updateFundGuide(InfoPostDTO infoPostDTO);
    public void deleteFundGuide(int postId);

    // 펀드시황관리
    public void insertFundMarket(InfoPostDTO infoPostDTO);
    public InfoPostDTO selectFundMarketById(int postId);
    public List<InfoPostDTO> selectAllFundMarket();
    public void updateFundMarket(InfoPostDTO infoPostDTO);
    public void deleteFundMarket(int postId);

}
