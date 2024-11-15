package shop.s5g.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import shop.s5g.auth.dto.LoginRequestDto;
import shop.s5g.auth.dto.TokenResponseDto;
import shop.s5g.auth.service.TokenService;

import jakarta.servlet.FilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAdminLoginFilterTest {

    @Mock
    private AuthenticationManager adminAuthenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private CustomAdminLoginFilter customAdminLoginFilter;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        customAdminLoginFilter = new CustomAdminLoginFilter(adminAuthenticationManager, objectMapper, tokenService);
    }

    @Test
    @DisplayName("attemptAuthentication - 관리자 로그인 성공")
    void attemptAuthentication_Success() throws Exception {
        // Mock Request
        MockHttpServletRequest request = new MockHttpServletRequest();
        LoginRequestDto loginRequestDto = new LoginRequestDto("adminUser", "adminPassword");
        request.setContent(objectMapper.writeValueAsBytes(loginRequestDto));

        // Mock Authentication
        Authentication mockAuth = new UsernamePasswordAuthenticationToken(
            "adminUser", "adminPassword", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(adminAuthenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);

        // Call Method
        Authentication result = customAdminLoginFilter.attemptAuthentication(request, new MockHttpServletResponse());

        // Verify
        assertNotNull(result);
        assertEquals("adminUser", result.getPrincipal());
        verify(adminAuthenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("successfulAuthentication - 관리자 로그인 성공 후 응답 검증")
    void successfulAuthentication_Success() throws Exception {
        // Mock Response
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Mock Authentication
        User user = new User("adminUser", "adminPassword",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        Authentication authResult = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        // Mock TokenService
        TokenResponseDto tokenResponseDto = new TokenResponseDto("adminAccessToken", "adminRefreshToken");
        when(tokenService.issueToken("adminUser", "ROLE_ADMIN")).thenReturn(tokenResponseDto);

        // Call Method
        customAdminLoginFilter.successfulAuthentication(
            new MockHttpServletRequest(), response, filterChain, authResult);

        // Verify Response
        assertEquals(200, response.getStatus());
        TokenResponseDto responseDto = objectMapper.readValue(response.getContentAsByteArray(), TokenResponseDto.class);
        assertEquals("adminAccessToken", responseDto.accessToken());
        assertEquals("adminRefreshToken", responseDto.refreshToken());
        verify(tokenService).issueToken("adminUser", "ROLE_ADMIN");
    }
}