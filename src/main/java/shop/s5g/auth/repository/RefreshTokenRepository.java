package shop.s5g.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String refreshTokenKey = "refresh_token";

    public boolean isExistRefreshToken(String loginId) {
        return redisTemplate.opsForHash().hasKey(refreshTokenKey, loginId);
    }

    public void saveRefreshToken(String loginId, String refreshToken) {
        redisTemplate.opsForHash().put(refreshTokenKey, loginId, refreshToken);
    }

    public String getRefreshToken(String loginId) {
        return (String) redisTemplate.opsForHash().get(refreshTokenKey, loginId);
    }
    public void deleteRefreshToken(String loginId) {
        redisTemplate.opsForHash().delete(refreshTokenKey, loginId);
    }
}
