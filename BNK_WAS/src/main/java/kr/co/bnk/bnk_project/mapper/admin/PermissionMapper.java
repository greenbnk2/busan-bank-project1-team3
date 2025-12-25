package kr.co.bnk.bnk_project.mapper.admin;

import kr.co.bnk.bnk_project.dto.BnkUserDTO;
import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.admin.AdminListDTO;
import kr.co.bnk.bnk_project.dto.admin.UserSearchDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PermissionMapper {

    // 회원 검색 목록
    List<UserSearchDTO> selectUserSearchList(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    // 검색 총 개수
    int selectUserSearchTotal(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);


    /** 관리자 목록 조회 */
    List<AdminListDTO> selectAdminList(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    /** 관리자 총 인원 수 */
    int selectAdminTotal(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    /*관리자 추가*/
    int insertAdminFromUser(@Param("custNo") Long custNo,
                            @Param("role") String role);


    /*관리자 권한 수정*/
    void updateAdminRole(@Param("adminNo") Long adminNo,
                         @Param("role") String role);


    /*관리자 권한 삭제*/
    void deleteAdminRole(@Param("adminNo") Long adminNo);
}