package kr.co.bnk.bnk_project.mapper;

import kr.co.bnk.bnk_project.dto.BnkAdminDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminMapper {

    /**
     * Spring Security 인증을 위한 관리자 정보 조회
     * @param adminId (로그인 시 입력한 관리자 아이디)
     * @return BnkAdminDTO (관리자 정보)
     */
    BnkAdminDTO findByAdminId(@Param("adminId") String adminId);
}