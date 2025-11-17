package kr.co.bnk.bnk_project.mapper;

import kr.co.bnk.bnk_project.dto.BnkUserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface MemberMapper {

    //Spring Security 인증을 위한 사용자 정보 조회
    BnkUserDTO findByCustId(@Param("custId") String custId);

    // 아이디 중복 체크
    int existsByCustId(String userId);

    // 이메일 중복 체크
    int existsByEmail(String email);

    // 회원 정보 저장
    void insertUser(BnkUserDTO dto);

    // 계좌 정보 저장
    void insertAccount(BnkUserDTO dto);

    // 직장 정보 저장
    void insertJobInfo(BnkUserDTO dto);

    // 부가 정보 저장
    void insertAdditionalInfo(BnkUserDTO dto);

    // 약관 동의 저장
    void insertAgreement(@Param("custNo") Long custNo,
                         @Param("termId") String termId,
                         @Param("isAgreed") String isAgreed);
}
