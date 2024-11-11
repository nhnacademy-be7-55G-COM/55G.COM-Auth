package shop.s5g.auth.repository;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UUIDRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String UUID_MAP = "UUID";

    public void saveLoginIdAndRole(String uuid, String loginId, String role) {
        redisTemplate.opsForHash().put(UUID_MAP, uuid, loginId + ":" + role);
    }

    public boolean existsUUID(String uuid) {
        return redisTemplate.opsForHash().hasKey(UUID_MAP, uuid);
    }

    public void deleteUUID(String uuid) {
        redisTemplate.opsForHash().delete(UUID_MAP, uuid);
    }

    public String getLoginIdAndRole(String uuid) {
        return Objects.requireNonNull(redisTemplate.opsForHash().get(UUID_MAP, uuid)).toString();
    }
}
