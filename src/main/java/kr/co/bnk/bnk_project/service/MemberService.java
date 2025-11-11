package kr.co.bnk.bnk_project.service;

import jakarta.transaction.Transactional;
import kr.co.bnk.bnk_project.dto.BnkUserDTO;
import kr.co.bnk.bnk_project.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 처리
    public void registerUser(BnkUserDTO dto) {
        // 1. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        // 2. 계좌비밀번호 암호화
        String encodedAcctPass = passwordEncoder.encode(dto.getAccountPassword());

        // 3. 이메일 조합
        String fullEmail = dto.getEmailId() + "@" + dto.getEmailDomain();

        // 4. bnk_user 테이블 INSERT
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("custId", dto.getUserId());
        userMap.put("password", encodedPassword);
        userMap.put("custName", dto.getName());
        userMap.put("custHp", dto.getPhone());
        userMap.put("custEmail", fullEmail);
        userMap.put("statusCode", "A"); // 정상
        userMap.put("acctPass", encodedAcctPass);

        memberMapper.insertUser(userMap);

        // 5. 생성된 고객번호 가져오기
        Long custNo = (Long) userMap.get("custNo");

        // 6. bnk_account 테이블 INSERT
        Map<String, Object> accountMap = new HashMap<>();
        accountMap.put("custNo", custNo);
        accountMap.put("acctNo", dto.getAccountNumber());
        accountMap.put("acctType", "보통예금"); // 기본값
        accountMap.put("balance", 0);

        memberMapper.insertAccount(accountMap);

        // 7. 약관 동의 정보 저장 (user_agreement)
        // 전화수신, 이메일수신 등
        if ("receive".equals(dto.getPhoneSms())) {
            memberMapper.insertAgreement(custNo, "PHONE_SMS", "Y");
        }
        if ("receive".equals(dto.getEmailSms())) {
            memberMapper.insertAgreement(custNo, "EMAIL_SMS", "Y");
        }
    }

}
