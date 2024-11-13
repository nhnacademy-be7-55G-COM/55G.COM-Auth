package shop.s5g.auth.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import shop.s5g.auth.jwt.JwtUtil;
import shop.s5g.auth.service.TokenService;

@ExtendWith(MockitoExtension.class)
class CustomLogoutFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private CustomLogoutFilter customLogoutFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
    }

    @Test
    void testDoFilter_Success() throws Exception {
        // Given
        String refreshToken = "validRefreshToken";
        String uuid = "validUUID";

        request.setRequestURI("/api/auth/logout");
        request.setMethod("POST");
        request.addHeader("Authorization", "Bearer " + refreshToken);

        when(jwtUtil.getUUID(refreshToken)).thenReturn(uuid);
        when(tokenService.deleteToken(uuid)).thenReturn(true);

        // When
        customLogoutFilter.doFilter(request, response, filterChain);

        // Then
        verify(tokenService, times(1)).deleteToken(uuid);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    void testDoFilter_TokenDeletionFails() throws Exception {
        // Given
        String refreshToken = "validRefreshToken";
        String uuid = "validUUID";

        request.setRequestURI("/api/auth/logout");
        request.setMethod("POST");
        request.addHeader("Authorization", "Bearer " + refreshToken);

        when(jwtUtil.getUUID(refreshToken)).thenReturn(uuid);
        when(tokenService.deleteToken(uuid)).thenReturn(false);

        // When
        customLogoutFilter.doFilter(request, response, filterChain);

        // Then
        verify(tokenService, times(1)).deleteToken(uuid);
        verify(filterChain, never()).doFilter(request, response);  // deleteToken 실패 시 filterChain이 호출되지 않음
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    void testDoFilter_InvalidUri() throws Exception {
        // Given
        request.setRequestURI("/api/other");
        request.setMethod("POST");

        // When
        customLogoutFilter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);  // 다른 URI의 경우 filterChain 호출
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    void testDoFilter_InvalidMethod() throws Exception {
        // Given
        request.setRequestURI("/api/auth/logout");
        request.setMethod("GET");

        // When
        customLogoutFilter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);  // HTTP 메소드가 POST가 아닌 경우 filterChain 호출
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    void testDoFilter_MissingAuthorizationHeader() throws Exception {
        // Given
        request.setRequestURI("/api/auth/logout");
        request.setMethod("POST");

        // When
        customLogoutFilter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);  // Authorization 헤더가 없을 경우 filterChain 호출
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
}