package yu.spring.gyeongsanlog.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

/*
 이메일 인증 상태를 Redis에 임시 보관한다.
 - code: 발송한 인증코드(만료 시간 지나면 자동 삭제)
 - verified: 코드 검증에 성공한 이메일 표시. 이 표시가 있어야 회원가입이 통과된다.
 - cooldown: 재발송 남용 방지용 짧은 TTL 키
 */
@Repository
@RequiredArgsConstructor
public class EmailVerificationRepository {

    private static final String CODE_PREFIX = "email-verify:code:";
    private static final String VERIFIED_PREFIX = "email-verify:verified:";
    private static final String COOLDOWN_PREFIX = "email-verify:cooldown:";

    private final StringRedisTemplate redisTemplate;

    public void saveCode(String email, String code, Duration ttl) {
        redisTemplate.opsForValue().set(CODE_PREFIX + email, code, ttl);
    }

    public Optional<String> findCode(String email) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(CODE_PREFIX + email));
    }

    public void deleteCode(String email) {
        redisTemplate.delete(CODE_PREFIX + email);
    }

    public void markVerified(String email, Duration ttl) {
        redisTemplate.opsForValue().set(VERIFIED_PREFIX + email, "true", ttl);
    }

    public boolean isVerified(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(VERIFIED_PREFIX + email));
    }

    public void deleteVerified(String email) {
        redisTemplate.delete(VERIFIED_PREFIX + email);
    }

    // 쿨다운 키를 새로 만들었으면 true(=발송 가능), 이미 있으면 false
    public boolean startCooldown(String email, Duration ttl) {
        Boolean created = redisTemplate.opsForValue().setIfAbsent(COOLDOWN_PREFIX + email, "1", ttl);
        return Boolean.TRUE.equals(created);
    }

    public void clearCooldown(String email) {
        redisTemplate.delete(COOLDOWN_PREFIX + email);
    }
}
