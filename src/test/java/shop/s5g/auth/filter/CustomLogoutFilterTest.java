package shop.s5g.auth.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

    @ParameterizedTest
    @CsvSource({
        "/api/other, GET, true, 200",  // Invalid URI
        "/api/auth/logout, GET, true, 200",  // Invalid method
        "/api/auth/logout, POST, false, 400"  // Token deletion fails
    })
    void testDoFilter_Parameterized(String uri, String method, boolean deleteTokenSuccess, int expectedStatus) throws Exception {
        // Given
        String refreshToken = "validRefreshToken";
        String uuid = "validUUID";

        request.setRequestURI(uri);
        request.setMethod(method);

        if (method.equals("POST") && uri.equals("/api/auth/logout")) {
            request.addHeader("Authorization", "Bearer " + refreshToken);
            when(jwtUtil.getUUID(refreshToken)).thenReturn(uuid);
            when(tokenService.deleteToken(uuid)).thenReturn(deleteTokenSuccess);
        }

        // When
        customLogoutFilter.doFilter(request, response, filterChain);

        // Then
        if (method.equals("POST") && uri.equals("/api/auth/logout")) {
            verify(tokenService, times(1)).deleteToken(uuid);
            if (!deleteTokenSuccess) {
                verify(filterChain, never()).doFilter(request, response);  // deleteToken 실패 시 filterChain 호출 금지
            }
        } else {
            verify(filterChain, times(1)).doFilter(request, response);  // 다른 URI나 HTTP 메서드는 filterChain 호출
        }

        assertEquals(expectedStatus, response.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
        "/api/auth/logout, POST, true",  // Valid logout request
        "/api/auth/logout, POST, false" // Valid logout request, but deletion fails
    })
    void testDoFilter_ValidLogout(String uri, String method, boolean deleteTokenSuccess) throws Exception {
        // Given
        String refreshToken = "validRefreshToken";
        String uuid = "validUUID";

        request.setRequestURI(uri);
        request.setMethod(method);
        request.addHeader("Authorization", "Bearer " + refreshToken);

        when(jwtUtil.getUUID(refreshToken)).thenReturn(uuid);
        when(tokenService.deleteToken(uuid)).thenReturn(deleteTokenSuccess);

        // When
        customLogoutFilter.doFilter(request, response, filterChain);

        // Then
        verify(tokenService, times(1)).deleteToken(uuid);
        if (deleteTokenSuccess) {
            assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        } else {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        }
    }
}