package shop.s5g.auth.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import shop.s5g.auth.adapter.ShopUserAdapter;
import shop.s5g.auth.dto.LoginRequestDto;
import shop.s5g.auth.dto.MemberStatusResponseDto;
import shop.s5g.auth.dto.TokenResponseDto;
import shop.s5g.auth.exception.JsonConvertException;
import shop.s5g.auth.service.TokenService;

@ExtendWith(MockitoExtension.class)
class CustomLoginFilterTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private CustomLoginFilter customLoginFilter;

    private ObjectMapper objectMapper;

    @Mock
    private ShopUserAdapter shopUserAdapter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        customLoginFilter = new CustomLoginFilter(authenticationManager, objectMapper, tokenService, shopUserAdapter);
    }

    @Test
    @DisplayName("attemptAuthentication - 성공적인 인증 요청")
    void attemptAuthentication_Success() throws Exception {
        // Mock Request
        MockHttpServletRequest request = new MockHttpServletRequest();
        LoginRequestDto loginRequestDto = new LoginRequestDto("testUser", "testPassword");
        request.setContent(objectMapper.writeValueAsBytes(loginRequestDto));

        // Mock Authentication
        Authentication mockAuth = new UsernamePasswordAuthenticationToken(
            "testUser", "testPassword", Collections.emptyList());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);

        // Call Method
        Authentication result = customLoginFilter.attemptAuthentication(request, new MockHttpServletResponse());

        // Verify
        assertNotNull(result);
        assertEquals("testUser", result.getPrincipal());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("attemptAuthentication - JSON 변환 실패")
    void attemptAuthentication_Fail_InvalidJson() {
        // Mock Request with invalid JSON
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent("invalid_json".getBytes());

        // Call Method and Assert Exception
        JsonConvertException exception = assertThrows(JsonConvertException.class, () ->
            customLoginFilter.attemptAuthentication(request, new MockHttpServletResponse()));

        assertEquals("Failed to convert JSON to LoginRequestDto", exception.getMessage());
    }

    @Test
    @DisplayName("successfulAuthentication - 성공적인 인증")
    void successfulAuthentication_Success() throws Exception {
        // Mock Response
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Mock Authentication
        User user = new User("testUser", "testPassword",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication authResult = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        // Mock TokenService
        TokenResponseDto tokenResponseDto = new TokenResponseDto("accessToken", "refreshToken");
        when(tokenService.issueToken("testUser", "ROLE_USER")).thenReturn(tokenResponseDto);

        MemberStatusResponseDto statusResponseDto = new MemberStatusResponseDto(1L, "ACTIVE");
        when(shopUserAdapter.getMemberStatus("testUser")).thenReturn(ResponseEntity.ok().body(statusResponseDto));
        // Call Method
        customLoginFilter.successfulAuthentication(
            new MockHttpServletRequest(), response, filterChain, authResult);

        // Verify Response
        assertEquals(200, response.getStatus());
        TokenResponseDto responseDto = objectMapper.readValue(response.getContentAsByteArray(), TokenResponseDto.class);
        assertEquals("accessToken", responseDto.accessToken());
        assertEquals("refreshToken", responseDto.refreshToken());
        verify(tokenService).issueToken("testUser", "ROLE_USER");
    }

    @Test
    @DisplayName("executeSuccessLogin - 성공적인 응답 작성")
    void executeSuccessLogin_Success() throws Exception {
        // Mock Response
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Mock Authentication
        User user = new User("testUser", "testPassword",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication authResult = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        // Mock TokenService
        TokenResponseDto tokenResponseDto = new TokenResponseDto("accessToken", "refreshToken");
        when(tokenService.issueToken("testUser", "ROLE_USER")).thenReturn(tokenResponseDto);

        // Call Static Method
        CustomLoginFilter.executeSuccessLogin(response, authResult, tokenService, objectMapper);

        // Verify Response
        assertEquals(200, response.getStatus());
        TokenResponseDto responseDto = objectMapper.readValue(response.getContentAsByteArray(), TokenResponseDto.class);
        assertEquals("accessToken", responseDto.accessToken());
        assertEquals("refreshToken", responseDto.refreshToken());
    }
}