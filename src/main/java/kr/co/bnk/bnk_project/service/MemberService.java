package kr.co.bnk.bnk_project.service;

import kr.co.bnk.bnk_project.dto.MemberUpdateDTO;
import kr.co.bnk.bnk_project.dto.UserFundDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import kr.co.bnk.bnk_project.dto.BnkUserDTO;
import kr.co.bnk.bnk_project.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    // 계좌번호 랜덤 생성
    public String generateAccountNum() {

        SecureRandom secureRandom = new SecureRandom();
        String generatedAccountNum = "";

        do {
            int part1 = secureRandom.nextInt(900) + 100;
            int part2 = secureRandom.nextInt(900) + 100;
            int part3 = secureRandom.nextInt(900000) + 100000;

            generatedAccountNum = String.format("%03d-%03d-%06d", part1, part2, part3);

        } while (memberMapper.checkAccountExist(generatedAccountNum) > 0);

        return generatedAccountNum;
    }

    // 회원가입 처리
    @Transactional
    public void registerUser(BnkUserDTO dto) {

        // 비즈니스 검증
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if (memberMapper.existsByCustId(dto.getUserId()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

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

    @Transactional(readOnly = true)
    public boolean isUserIdAvailable(String userId) {
        // 1. DB에서 userId 개수 확인
        int count = memberMapper.existsByCustId(userId);

        // 2. count가 0이면 사용 가능 (true), 0보다 크면 중복 (false)
        return count == 0;
    }

    // 수정 화면 진입 시 정보 조회
    public MemberUpdateDTO getMemberInfo(Long custNo) {
        return memberMapper.findDetailByCustNo(custNo);
    }

    // 정보 수정 실행
    public void updateMemberInfo(MemberUpdateDTO dto) {
        memberMapper.updateMemberInfo(dto);
    }

    // 회원별 펀드 정보
    public List<UserFundDTO> getMyFundList(Long custNo) {
        // DB에서 데이터 가져오기
        List<UserFundDTO> list = memberMapper.selectMyFundList(custNo);

        // 가져온 리스트를 하나씩 꺼내서 '세금'과 '세후 금액' 계산하여 채워넣기
        for (UserFundDTO dto : list) {

            long principal = dto.getPurchaseAmount();       // 투자 원금
            long current = dto.getCurrentEvalAmount();      // 현재 평가금액 (세전)
            long profit = current - principal;              // 평가 손익 (얼마 벌었는지)

            // A. 손익금액(amount) 세팅
            dto.setProfitAmount(profit);

            // B. 세금(tax) 계산 (이익금의 15.4%, 손실이면 0원)
            long tax = 0;
            if (profit > 0) {
                tax = (long) (profit * 0.154);
            }
            dto.setTaxAmount(tax);

            // C. 세후 평가금액(net) 세팅 (현재금액 - 세금)
            dto.setAfterTaxAmount(current - tax);
        }

        return list;
    }

    // 총 평가금액 계산
    public long calculateTotalEval(List<UserFundDTO> list) {
        return list.stream().mapToLong(UserFundDTO::getCurrentEvalAmount).sum();
    }

    // 총 수익률 계산 (단순 합계 수익률)
    public double calculateTotalYield(List<UserFundDTO> list) {
        long totalPrincipal = list.stream().mapToLong(UserFundDTO::getPurchaseAmount).sum();
        long totalEval = list.stream().mapToLong(UserFundDTO::getCurrentEvalAmount).sum();

        if (totalPrincipal == 0) return 0.0;

        return (double) (totalEval - totalPrincipal) / totalPrincipal * 100;
    }
}
