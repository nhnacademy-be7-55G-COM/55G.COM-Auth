package shop.s5g.auth.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secretKey = "testSecretKey12345678901234567890";
    private final long accessExpirationTime = 60000;
    private final long refreshExpirationTime = 120000;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(secretKey, accessExpirationTime, refreshExpirationTime);
    }

    @Test
    void testCreateAccessToken() {
        String uuid = "test-uuid";
        String accessToken = jwtUtil.createAccessToken(uuid);

        assertNotNull(accessToken, "Access token should not be null");
    }

    @Test
    void testCreateRefreshToken() {
        String uuid = "test-uuid";
        String refreshToken = jwtUtil.createRefreshToken(uuid);

        assertNotNull(refreshToken, "Refresh token should not be null");
    }

    @Test
    void testGetUUIDFromAccessToken() {
        String uuid = "test-uuid";
        String accessToken = jwtUtil.createAccessToken(uuid);

        String extractedUUID = jwtUtil.getUUID(accessToken);

        assertEquals(uuid, extractedUUID, "Extracted UUID should match the original UUID");
    }

    @Test
    void testGetUUIDFromRefreshToken() {
        String uuid = "test-uuid";
        String refreshToken = jwtUtil.createRefreshToken(uuid);

        String extractedUUID = jwtUtil.getUUID(refreshToken);

        assertEquals(uuid, extractedUUID, "Extracted UUID should match the original UUID");
    }

    @Test
    void testGetUUIDWithInvalidToken() {
        String invalidToken = "invalid.token.string";

        assertThrows(JwtException.class, () -> jwtUtil.getUUID(invalidToken), "Should throw JwtException for invalid token");
    }

    @Test
    void testGetUUIDWithExpiredToken() throws InterruptedException {
        JwtUtil shortLivedJwtUtil = new JwtUtil(secretKey, 10, 10); // 10ms 만료 시간

        String uuid = "test-uuid";
        String accessToken = shortLivedJwtUtil.createAccessToken(uuid);

        Thread.sleep(20);

        assertThrows(JwtException.class, () -> shortLivedJwtUtil.getUUID(accessToken), "Should throw JwtException for expired token");
    }
}
