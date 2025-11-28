package kr.co.bnk.bnk_project.mapper;

import kr.co.bnk.bnk_project.dto.BnkUserDTO;
import kr.co.bnk.bnk_project.dto.MemberUpdateDTO;
import kr.co.bnk.bnk_project.dto.UserFundDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.security.SecureRandom;
import java.util.List;

@Mapper
public interface MemberMapper {

    //Spring Security 인증을 위한 사용자 정보 조회
    BnkUserDTO findByCustId(@Param("custId") String custId);
    
    //UserId로 CustNo조회
    Long findCustNoByUserId(String userId);
    
    // 아이디 중복 체크
    int existsByCustId(String userId);

    // 이메일 중복 체크
    int existsByEmail(String email);

    // 계좌번호 중복 체크
    int checkAccountExist(String generatedAccountNum);

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

    //회원 정보 조회
    MemberUpdateDTO findDetailByCustNo(Long custNo);

    //회원 정보 수정
    void updateMemberInfo(MemberUpdateDTO dto);

}
