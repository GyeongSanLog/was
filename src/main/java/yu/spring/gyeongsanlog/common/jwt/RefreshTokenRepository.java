package yu.spring.gyeongsanlog.common.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh-token:";

    private final StringRedisTemplate redisTemplate;

    public void save(String refreshToken, Long userId, long expirationMillis) {
        redisTemplate.opsForValue().set(
                key(refreshToken),
                String.valueOf(userId),
                Duration.ofMillis(expirationMillis)
        );
    }

    public Optional<Long> findUserIdByToken(String refreshToken) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(refreshToken)))
                .map(Long::valueOf);
    }

    public void deleteByToken(String refreshToken) {
        redisTemplate.delete(key(refreshToken));
    }

    private String key(String refreshToken) {
        return KEY_PREFIX + refreshToken;
    }
}
