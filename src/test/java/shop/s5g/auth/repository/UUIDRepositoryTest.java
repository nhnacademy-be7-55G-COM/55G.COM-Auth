package shop.s5g.auth.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import shop.s5g.auth.config.RedisConfig;

@DataRedisTest
@Import(RedisConfig.class)
class UUIDRepositoryTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private UUIDRepository uuidRepository;

    @BeforeEach
    void setUp() {
        uuidRepository = new UUIDRepository(redisTemplate);
    }

    @AfterEach
    void tearDown() {
        redisTemplate.delete("UUID");
    }

    @Test
    @DisplayName("UUID 저장 및 조회")
    void saveAndGetLoginIdAndRole() {
        // Given
        String uuid = "test-uuid";
        String loginId = "user123";
        String role = "ROLE_USER";

        // When
        uuidRepository.saveLoginIdAndRole(uuid, loginId, role);
        String result = uuidRepository.getLoginIdAndRole(uuid);

        // Then
        assertNotNull(result);
        assertEquals("user123:ROLE_USER", result);
    }

    @Test
    @DisplayName("UUID 존재 여부 확인")
    void existsUUID() {
        // Given
        String uuid = "test-uuid";
        String loginId = "user123";
        String role = "ROLE_USER";

        // When
        uuidRepository.saveLoginIdAndRole(uuid, loginId, role);
        boolean exists = uuidRepository.existsUUID(uuid);

        // Then
        assertTrue(exists);
    }

    @Test
    @DisplayName("UUID 삭제")
    void deleteUUID() {
        // Given
        String uuid = "test-uuid";
        String loginId = "user123";
        String role = "ROLE_USER";

        uuidRepository.saveLoginIdAndRole(uuid, loginId, role);
        assertTrue(uuidRepository.existsUUID(uuid)); // UUID가 저장되어 있는지 확인

        // When
        uuidRepository.deleteUUID(uuid);

        // Then
        assertFalse(uuidRepository.existsUUID(uuid));
    }

    @Test
    @DisplayName("존재하지 않는 UUID 조회 시 NullPointerException 발생")
    void getNonExistentUUID() {
        // Given
        String uuid = "non-existent-uuid";

        // When & Then
        assertThrows(NullPointerException.class, () -> uuidRepository.getLoginIdAndRole(uuid));
    }
}
