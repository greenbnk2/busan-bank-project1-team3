package kr.co.bnk.bnk_project.service.admin;

import kr.co.bnk.bnk_project.dto.PageRequestDTO;
import kr.co.bnk.bnk_project.dto.PageResponseDTO;
import kr.co.bnk.bnk_project.dto.admin.MemberListDTO;
import kr.co.bnk.bnk_project.mapper.admin.AdminMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMemberService {

    private final AdminMemberMapper adminMemberMapper;

    public PageResponseDTO<MemberListDTO> getMemberPage(PageRequestDTO pageRequestDTO) {

        if (pageRequestDTO.getPg() <= 0) pageRequestDTO.setPg(1);
        if (pageRequestDTO.getSize() <= 0) pageRequestDTO.setSize(10);


        int total = adminMemberMapper.selectMemberTotal(pageRequestDTO);
        List<MemberListDTO> list = adminMemberMapper.selectMemberList(pageRequestDTO);

        return PageResponseDTO.of(pageRequestDTO, list, total);

    }

   // 회원 단건 조회 (수정 페이지용)
    public MemberListDTO getMember(int custNo) {
        return adminMemberMapper.selectMemberByCustNo(custNo);
    }

    // 회원 수정
    public void updateMember(MemberListDTO dto) {
        adminMemberMapper.updateMember(dto);
    }


    public void updateMemberStatus(int custNo, String statusCode) {
        adminMemberMapper.updateMemberStatus(custNo, statusCode);
    }
}
