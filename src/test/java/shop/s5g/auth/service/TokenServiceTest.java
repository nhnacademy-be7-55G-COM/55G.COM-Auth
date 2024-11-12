package shop.s5g.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.s5g.auth.adapter.ShopUserAdapter;
import shop.s5g.auth.dto.TokenResponseDto;
import shop.s5g.auth.dto.UserDetailResponseDto;
import shop.s5g.auth.exception.InvalidResponseException;
import shop.s5g.auth.jwt.JwtUtil;
import shop.s5g.auth.repository.RefreshTokenRepository;
import shop.s5g.auth.repository.UUIDRepository;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UUIDRepository uuidRepository;

    @Mock
    private ShopUserAdapter shopUserAdapter;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void testIssueTokenSuccess() {
        // Given
        String username = "user1";
        String role = "ROLE_MEMBER";
        String accessToken = "newAccessToken";
        String refreshToken = "newRefreshToken";

        // 스텁 설정
        when(uuidRepository.existsUUID(anyString())).thenReturn(false);  // 중복 UUID 없음
        when(jwtUtil.createAccessToken(anyString())).thenReturn(accessToken);
        when(jwtUtil.createRefreshToken(anyString())).thenReturn(refreshToken);

        doNothing().when(refreshTokenRepository).saveRefreshToken(anyString(), anyString());
        // 스텁에서 호출된 UUID 저장
        doNothing().when(uuidRepository).saveLoginIdAndRole(anyString(), eq(username), eq(role));

        // When
        TokenResponseDto response = tokenService.issueToken(username, role);

        // Then
        assertNotNull(response);
        assertEquals(accessToken, response.accessToken());
        assertEquals(refreshToken, response.refreshToken());

        // Verify interactions
        verify(uuidRepository, times(1)).existsUUID(anyString());
        verify(uuidRepository, times(1)).saveLoginIdAndRole(anyString(), eq(username), eq(role));
        verify(jwtUtil, times(1)).createAccessToken(anyString());
        verify(jwtUtil, times(1)).createRefreshToken(anyString());
    }

    @Test
    void testReissueTokenSuccess() {
        // Given
        String refreshToken = "validRefreshToken";
        String uuid = "validUUID";
        String username = "user1";
        String role = "ROLE_MEMBER";
        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";

        // 스텁 설정
        when(jwtUtil.getUUID(refreshToken)).thenReturn(uuid);
        when(uuidRepository.existsUUID(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            return id.equals(uuid);
        });

        when(uuidRepository.getLoginIdAndRole(uuid)).thenReturn(username + ":" + role);
        when(refreshTokenRepository.isExistRefreshToken(anyString())).thenReturn(true);
        when(refreshTokenRepository.getRefreshToken(anyString())).thenReturn(refreshToken);
        when(jwtUtil.createAccessToken(anyString())).thenReturn(newAccessToken);
        when(jwtUtil.createRefreshToken(anyString())).thenReturn(newRefreshToken);

        // 스텁에서 refreshToken 삭제
        doNothing().when(refreshTokenRepository).deleteRefreshToken(anyString());

        // When
        TokenResponseDto response = tokenService.reissueToken(refreshToken);

        // Then
        assertNotNull(response);
        assertEquals(newAccessToken, response.accessToken());
        assertEquals(newRefreshToken, response.refreshToken());

        // Verify interactions
        verify(jwtUtil, times(1)).getUUID(refreshToken);
        verify(uuidRepository, times(1)).existsUUID(uuid);
        verify(refreshTokenRepository, times(1)).isExistRefreshToken(anyString());
        verify(refreshTokenRepository, times(1)).getRefreshToken(anyString());
        verify(refreshTokenRepository, times(1)).deleteRefreshToken(anyString());
        verify(jwtUtil, times(1)).createAccessToken(anyString());
        verify(jwtUtil, times(1)).createRefreshToken(anyString());
    }

    @Test
    void testReissueTokenWithInvalidRefreshToken() {
        // Given
        String invalidRefreshToken = "invalidRefreshToken";

        // 스텁 설정
        when(jwtUtil.getUUID(invalidRefreshToken)).thenReturn("invalidUUID");
        when(uuidRepository.existsUUID("invalidUUID")).thenReturn(false);

        // When and Then
        assertThrows(InvalidResponseException.class, () -> tokenService.reissueToken(invalidRefreshToken));

        // Verify that methods are called
        verify(jwtUtil, times(1)).getUUID(invalidRefreshToken);
        verify(uuidRepository, times(2)).existsUUID("invalidUUID");
    }

    @Test
    void testDeleteTokenSuccess() {
        // Given
        String uuid = UUID.randomUUID().toString();
        String loginIdAndRole = "user1:ROLE_MEMBER";

        when(uuidRepository.existsUUID(uuid)).thenReturn(true);
        when(uuidRepository.getLoginIdAndRole(uuid)).thenReturn(loginIdAndRole);
        when(refreshTokenRepository.isExistRefreshToken(loginIdAndRole)).thenReturn(true);

        // When
        boolean result = tokenService.deleteToken(uuid);

        // Then
        assertTrue(result);
        verify(uuidRepository).deleteUUID(uuid);
        verify(refreshTokenRepository).deleteRefreshToken(loginIdAndRole);
    }

    @Test
    void testGetUserByUUIDSuccess() {
        // Given
        String uuid = UUID.randomUUID().toString();
        String loginIdAndRole = "user1:ROLE_MEMBER";

        when(uuidRepository.existsUUID(uuid)).thenReturn(true);
        when(uuidRepository.getLoginIdAndRole(uuid)).thenReturn(loginIdAndRole);

        // When
        UserDetailResponseDto response = tokenService.getUserByUUID(uuid);

        // Then
        assertNotNull(response);
        assertEquals("user1", response.username());
        assertEquals("ROLE_MEMBER", response.role());
    }

    @Test
    void testGetRoleByTokenSuccess() {
        // Given
        String token = "access-token";
        String uuid = UUID.randomUUID().toString();
        String loginIdAndRole = "user1:ROLE_MEMBER";

        when(jwtUtil.getUUID(token)).thenReturn(uuid);
        when(uuidRepository.existsUUID(uuid)).thenReturn(true);
        when(uuidRepository.getLoginIdAndRole(uuid)).thenReturn(loginIdAndRole);

        // When
        String role = tokenService.getRoleByToken(token);

        // Then
        assertEquals("ROLE_MEMBER", role);
    }
    @Test
    void testDeleteTokenWithInvalidUUID() {
        // Given
        String uuid = UUID.randomUUID().toString();
        when(uuidRepository.existsUUID(uuid)).thenReturn(false);

        // When
        boolean result = tokenService.deleteToken(uuid);

        // Then
        assertFalse(result);
    }

    @Test
    void testGetUserByUUIDWithInvalidUUID() {
        // Given
        String uuid = UUID.randomUUID().toString();
        when(uuidRepository.existsUUID(uuid)).thenReturn(false);

        // When & Then
        assertThrows(InvalidResponseException.class, () -> tokenService.getUserByUUID(uuid));
    }
}