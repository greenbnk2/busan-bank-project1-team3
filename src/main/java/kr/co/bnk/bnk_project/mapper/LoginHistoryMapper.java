package kr.co.bnk.bnk_project.mapper;

import kr.co.bnk.bnk_project.dto.LoginHistoryDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginHistoryMapper {

    // 로그인 이력 저장
    void insertLoginHistory(LoginHistoryDTO loginHistoryDTO);

}
