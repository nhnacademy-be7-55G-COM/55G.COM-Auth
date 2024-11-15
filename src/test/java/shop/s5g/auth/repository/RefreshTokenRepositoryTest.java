package shop.s5g.auth.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import shop.s5g.auth.config.RedisConfig;

@ExtendWith(SpringExtension.class)
@DisabledIf("#{T(org.springframework.util.StringUtils).hasText(environment['spring.profiles.active']) && environment['spring.profiles.active'].contains('disable-redis')}")
@Testcontainers(disabledWithoutDocker = true)
class RefreshTokenRepositoryTest {

    private static RedisTemplate<String, Object> redisTemplate;
    private static final String REDIS_IMAGE = "redis:7.0.8-alpine";
    private static final int REDIS_PORT = 6379;
    private static final GenericContainer REDIS_CONTAINER;
    private static RefreshTokenRepository refreshTokenRepository;

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
        refreshTokenRepository = new RefreshTokenRepository(redisTemplate);
    }

    @BeforeEach
    void flushRedis() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
    }

    @Test
    @DisplayName("RefreshToken 저장 및 조회")
    void saveAndGetRefreshToken() {
        // Given
        String loginIdAndRole = "user_role";
        String refreshToken = "sample_refresh_token";

        // When
        refreshTokenRepository.saveRefreshToken(loginIdAndRole, refreshToken);
        String retrievedToken = refreshTokenRepository.getRefreshToken(loginIdAndRole);

        // Then
        assertNotNull(retrievedToken);
        assertEquals(refreshToken, retrievedToken);
    }

    @Test
    @DisplayName("RefreshToken 존재 여부 확인")
    void isExistRefreshToken() {
        // Given
        String loginIdAndRole = "user_role";
        String refreshToken = "sample_refresh_token";

        // When
        refreshTokenRepository.saveRefreshToken(loginIdAndRole, refreshToken);
        boolean exists = refreshTokenRepository.isExistRefreshToken(loginIdAndRole);

        // Then
        assertTrue(exists);
    }

    @Test
    @DisplayName("RefreshToken 삭제")
    void deleteRefreshToken() {
        // Given
        String loginIdAndRole = "user_role";
        String refreshToken = "sample_refresh_token";

        refreshTokenRepository.saveRefreshToken(loginIdAndRole, refreshToken);
        assertTrue(refreshTokenRepository.isExistRefreshToken(loginIdAndRole));

        // When
        refreshTokenRepository.deleteRefreshToken(loginIdAndRole);

        // Then
        assertFalse(refreshTokenRepository.isExistRefreshToken(loginIdAndRole));
    }
}