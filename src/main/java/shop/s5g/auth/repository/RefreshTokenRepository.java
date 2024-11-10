package shop.s5g.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REFRESH_TOKEN = "refresh_token";

    public boolean isExistRefreshToken(String loginIdAndRole) {
        return redisTemplate.opsForHash().hasKey(REFRESH_TOKEN, loginIdAndRole);
    }

    public void saveRefreshToken(String loginIdAndRole, String refreshToken) {
        redisTemplate.opsForHash().put(REFRESH_TOKEN, loginIdAndRole, refreshToken);
    }

    public String getRefreshToken(String loginIdAndRole) {
        return (String) redisTemplate.opsForHash().get(REFRESH_TOKEN, loginIdAndRole);
    }
    public void deleteRefreshToken(String loginIdAndRole) {
        redisTemplate.opsForHash().delete(REFRESH_TOKEN, loginIdAndRole);
    }
}
