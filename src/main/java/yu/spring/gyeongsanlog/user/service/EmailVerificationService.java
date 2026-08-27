package yu.spring.gyeongsanlog.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import yu.spring.gyeongsanlog.common.exception.BusinessException;
import yu.spring.gyeongsanlog.common.exception.ErrorCode;
import yu.spring.gyeongsanlog.user.domain.Provider;
import yu.spring.gyeongsanlog.user.dto.EmailSendRequest;
import yu.spring.gyeongsanlog.user.dto.EmailVerifyRequest;
import yu.spring.gyeongsanlog.user.repository.EmailVerificationRepository;
import yu.spring.gyeongsanlog.user.repository.UserRepository;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(30);
    private static final Duration COOLDOWN = Duration.ofSeconds(60);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendCode(EmailSendRequest request) {
        String email = request.getEmail();

        if (userRepository.existsByEmailAndProvider(email, Provider.LOCAL)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (!emailVerificationRepository.startCooldown(email, COOLDOWN)) {
            throw new BusinessException(ErrorCode.EMAIL_SEND_TOO_OFTEN);
        }

        String code = generateCode();
        emailVerificationRepository.saveCode(email, code, CODE_TTL);
        send(email, code);
    }

    public void verifyCode(EmailVerifyRequest request) {
        String savedCode = emailVerificationRepository.findCode(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_CODE_MISMATCH));

        if (!savedCode.equals(request.getCode())) {
            throw new BusinessException(ErrorCode.EMAIL_CODE_MISMATCH);
        }

        // 한 번 쓴 코드는 재사용되지 않도록 지우고, 인증 완료 표시만 남긴다
        emailVerificationRepository.deleteCode(request.getEmail());
        emailVerificationRepository.markVerified(request.getEmail(), VERIFIED_TTL);
    }

    public boolean isVerified(String email) {
        return emailVerificationRepository.isVerified(email);
    }

    // 가입이 끝나면 인증 표시를 지워 같은 표시로 중복 가입되지 않게 한다
    public void clearVerified(String email) {
        emailVerificationRepository.deleteVerified(email);
    }

    private void send(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("[셋로그] 이메일 인증코드");
        message.setText("인증코드는 " + code + " 입니다.\n"
                + CODE_TTL.toMinutes() + "분 안에 입력해주세요.");

        try {
            mailSender.send(message);
        } catch (MailException e) {
            // 발송이 실패했으면 쿨다운에 걸어두지 않는다.

            emailVerificationRepository.clearCooldown(email);
            emailVerificationRepository.deleteCode(email);
            log.error("인증 메일 발송 실패. email: {}", email, e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
