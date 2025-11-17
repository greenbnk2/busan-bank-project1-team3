package kr.co.bnk.bnk_project.service;

import kr.co.bnk.bnk_project.dto.UserTermsDTO;
import kr.co.bnk.bnk_project.mapper.UserTermsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserTermsService {

    private final UserTermsMapper userTermsMapper;

    /* 전체 약관 조회 */
    public List<UserTermsDTO> getAllTerms() {
        return userTermsMapper.selectAllTerms();
    }

    /* termId 로 단일 약관 조회*/
    public UserTermsDTO getTerm(String termId) {
        return userTermsMapper.selectTermById(termId);
    }

    /* 약관 수정*/
    public void updateTerm(UserTermsDTO dto) {
        userTermsMapper.updateTerm(dto);
    }
}
