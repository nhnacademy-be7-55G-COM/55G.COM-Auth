package shop.s5g.auth.repository;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REFRESH_TOKEN = "refresh_token:";

    @Value("${spring.jwt.token.refresh-expiration-time}")
    private long expireTime;


    public boolean isExistRefreshToken(String loginIdAndRole) {
        String key = REFRESH_TOKEN + loginIdAndRole;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void saveRefreshToken(String loginIdAndRole, String refreshToken) {
        redisTemplate.opsForValue().set(REFRESH_TOKEN + loginIdAndRole, refreshToken, Duration.ofMillis(expireTime + 10000));
    }

    public String getRefreshToken(String loginIdAndRole) {
        String key = REFRESH_TOKEN + loginIdAndRole;
        return (String) redisTemplate.opsForValue().get(key);
    }
    public void deleteRefreshToken(String loginIdAndRole) {
        String key = REFRESH_TOKEN + loginIdAndRole;
        redisTemplate.delete(key);
    }
}
