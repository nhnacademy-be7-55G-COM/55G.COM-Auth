package shop.s5g.auth.repository;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UUIDRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String UUID_PREFIX = "UUID:";
    @Value("${spring.jwt.token.refresh-expiration-time}")
    private long expireTime;

    public void saveLoginIdAndRole(String uuid, String loginId, String role) {
        String key = UUID_PREFIX + uuid;
        String value = loginId + ":" + role;
        redisTemplate.opsForValue().set(key, value, Duration.ofMillis(expireTime + 10000));
    }

    /**
     * UUID 존재 여부 확인
     */
    public boolean existsUUID(String uuid) {
        String key = UUID_PREFIX + uuid;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * UUID 삭제
     */
    public void deleteUUID(String uuid) {
        String key = UUID_PREFIX + uuid;
        redisTemplate.delete(key);
    }

    public String getLoginIdAndRole(String uuid) {
        String key = UUID_PREFIX + uuid;
        return (String) redisTemplate.opsForValue().get(key);
    }
}
