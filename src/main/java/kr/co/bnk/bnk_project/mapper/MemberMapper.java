package kr.co.bnk.bnk_project.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface MemberMapper {

    // ID 중복 체크
    int checkDuplicateId(@Param("custId") String custId);

    // 회원 정보 저장
    void insertUser(Map<String, Object> params);

    // 계좌 정보 저장
    void insertAccount(Map<String, Object> params);

    // 약관 동의 저장
    void insertAgreement(@Param("custNo") Long custNo,
                         @Param("termId") String termId,
                         @Param("isAgreed") String isAgreed);
}
