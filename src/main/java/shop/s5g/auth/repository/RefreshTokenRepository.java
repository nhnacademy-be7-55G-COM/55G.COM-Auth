package shop.s5g.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REFRESH_TOKEN = "refresh_token";

    public boolean isExistRefreshToken(String uuid) {
        return redisTemplate.opsForHash().hasKey(REFRESH_TOKEN, uuid);
    }

    public void saveRefreshToken(String uuid, String refreshToken) {
        redisTemplate.opsForHash().put(REFRESH_TOKEN, uuid, refreshToken);
    }

    public String getRefreshToken(String uuid) {
        return (String) redisTemplate.opsForHash().get(REFRESH_TOKEN, uuid);
    }
    public void deleteRefreshToken(String uuid) {
        redisTemplate.opsForHash().delete(REFRESH_TOKEN, uuid);
    }
}
