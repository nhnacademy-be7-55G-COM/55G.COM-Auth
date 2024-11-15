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
@Import(RedisConfig.class) // Redis 설정 클래스 추가
class RefreshTokenRepositoryTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository = new RefreshTokenRepository(redisTemplate);
    }

    @AfterEach
    void tearDown() {
        redisTemplate.delete("refresh_token");
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