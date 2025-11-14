package kr.co.bnk.bnk_project.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import kr.co.bnk.bnk_project.dto.BnkUserDTO;
import kr.co.bnk.bnk_project.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 처리
    public void registerUser(BnkUserDTO dto) {

        // 1. DTO에 비즈니스 로직 값 설정 (암호화)
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        dto.setAccountPassword(passwordEncoder.encode(dto.getAccountPassword()));

        log.info("registerUser BnkUserDTO =>{} " , dto.toString());

        // 2. DB INSERT (순서대로 실행)
        memberMapper.insertUser(dto);           // (1) bnk_user 저장
        memberMapper.insertAccount(dto);        // (2) bnk_account 저장
        memberMapper.insertJobInfo(dto);        // (3) BNK_USER_JOBINFO 저장
        memberMapper.insertAdditionalInfo(dto); // (4) BNK_USER_INFO 저장

        // 3. 약관 동의 정보 저장 (user_agreement)
        Long custNo = dto.getCustNo();

        /*if ("receive".equals(dto.getPhoneSms())) {
            memberMapper.insertAgreement(custNo, "PHONE_SMS", "Y");
        } else {
            memberMapper.insertAgreement(custNo, "PHONE_SMS", "N");
        }

        if ("receive".equals(dto.getEmailSms())) {
            memberMapper.insertAgreement(custNo, "EMAIL_SMS", "Y");
        } else {
            memberMapper.insertAgreement(custNo, "EMAIL_SMS", "N");
        }*/
    }

}
