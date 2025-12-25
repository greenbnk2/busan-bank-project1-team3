package kr.co.bnk.bnk_project.service;

import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import kr.co.bnk.bnk_project.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final MemberMapper memberMapper;
    private final HttpSession session;

    @Value("${spring.mail.username}")
    private String sender;

    /**
     * 이메일 중복 확인 및 인증 코드 발송
     * @param fullEmail (예: test@naver.com)
     * @return 중복이면 true, 발송 성공하면 false
     */
    @Transactional(readOnly = true)
    public boolean sendVerificationCode(String fullEmail) {

        // 1. DB에서 이메일 중복 확인
        if (memberMapper.existsByEmail(fullEmail) > 0) {
            return true; // 중복됨
        }

        // 2. 중복이 아니면 인증 코드 생성 및 발송
        sendCode(fullEmail);
        return false; // 발송 성공
    }

    private void sendCode(String receiver) {
        MimeMessage message = mailSender.createMimeMessage();
        int code = ThreadLocalRandom.current().nextInt(100000, 1000000); // 6자리 난수

        String title = "[BNK] 회원가입 인증코드입니다.";
        String content = "<h1>인증코드는 [" + code + "] 입니다.</h1>";

        try {
            message.setFrom(new InternetAddress(sender, "BNK 관리자", "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));
            message.setSubject(title);
            message.setContent(content, "text/html;charset=UTF-8");

            mailSender.send(message);

            // 3. 세션에 인증 코드 저장 (키 이름: emailVerificationCode)
            session.setAttribute("emailVerificationCode", String.valueOf(code));
            log.info("이메일 인증 코드 발송: {} -> {}", receiver, code);

        } catch (Exception e) {
            log.error("이메일 발송 실패: {}", e.getMessage());
            throw new RuntimeException("이메일 발송 중 오류가 발생했습니다.");
        }
    }

    // 사용자가 입력한 코드 검증
    public boolean verifyCode(String inputCode) {
        String savedCode = (String) session.getAttribute("emailVerificationCode");
        return savedCode != null && savedCode.equals(inputCode);
    }
}