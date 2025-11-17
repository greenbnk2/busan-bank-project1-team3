package kr.co.bnk.bnk_project.mapper.admin;

import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.admin.MemberListDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminMemberMapper {

    List<MemberListDTO> selectMemberList(PageRequestDTO pageRequestDTO);

    int selectMemberTotal(PageRequestDTO pageRequestDTO);


    // 회원 상세
    MemberListDTO selectMemberByCustNo(int custNo);

    // 회원 수정
    int updateMember(MemberListDTO dto);

    // 회원 상태 변경 (정상/정지/휴면 등)
    int updateMemberStatus(int custNo, String statusCode);
}
