package shop.s5g.auth.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(SpringExtension.class)
@DisabledIf("#{T(org.springframework.util.StringUtils).hasText(environment['spring.profiles.active']) && environment['spring.profiles.active'].contains('disable-redis')}")
@Testcontainers(disabledWithoutDocker = true)
class UUIDRepositoryTest {

    private static RedisTemplate<String, Object> redisTemplate;
    private static final String REDIS_IMAGE = "redis:7.0.8-alpine";
    private static final int REDIS_PORT = 6379;
    private static final GenericContainer REDIS_CONTAINER;
    private static UUIDRepository uuidRepository;

    static {
        REDIS_CONTAINER = new GenericContainer(REDIS_IMAGE)
            .withExposedPorts(REDIS_PORT)
            .withReuse(true);
        REDIS_CONTAINER.start();
    }

    @BeforeAll
    static void setUp() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(REDIS_CONTAINER.getHost(),
            REDIS_CONTAINER.getMappedPort(REDIS_PORT));
        factory.start();
        factory.afterPropertiesSet();

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.afterPropertiesSet();
        uuidRepository = new UUIDRepository(redisTemplate);
    }

    @BeforeEach
    void flushRedis() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
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
