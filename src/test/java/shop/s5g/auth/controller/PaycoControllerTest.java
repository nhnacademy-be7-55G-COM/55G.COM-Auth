package shop.s5g.auth.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import shop.s5g.auth.dto.TokenResponseDto;
import shop.s5g.auth.exception.AlreadyLinkAccountException;
import shop.s5g.auth.exception.MemberNotFoundException;
import shop.s5g.auth.service.PaycoService;
import shop.s5g.auth.service.TokenService;

@ExtendWith(MockitoExtension.class)
class PaycoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaycoService paycoService;

    @Mock
    private TokenService tokenService;
    @InjectMocks
    private PaycoController paycoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paycoController).build();
    }

    @Test
    void oauthIssueToken_Success() throws Exception {
        // Given
        String code = "validCode";
        TokenResponseDto tokenResponse = new TokenResponseDto("accessToken", "refreshToken");
        when(paycoService.getToken(code)).thenReturn(tokenResponse);
        when(paycoService.getPaycoId(tokenResponse.accessToken())).thenReturn("paycoId");
        when(paycoService.getMemberId("paycoId")).thenReturn("loginId");
        when(tokenService.issueToken("loginId", "ROLE_MEMBER")).thenReturn(tokenResponse);

        // When / Then
        mockMvc.perform(post("/api/auth/payco")
                .param("code", code)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("accessToken"))
            .andExpect(jsonPath("$.refreshToken").value("refreshToken"));

        verify(paycoService).getToken(code);
        verify(paycoService).getPaycoId(tokenResponse.accessToken());
        verify(paycoService).getMemberId("paycoId");
        verify(tokenService).issueToken("loginId", "ROLE_MEMBER");
    }

    @Test
    void linkAccount_Success() throws Exception {
        // Given
        String code = "validCode";
        String accessToken = "Bearer someToken";
        TokenResponseDto tokenResponse = new TokenResponseDto("accessToken", "refreshToken");
        when(paycoService.getToken(code)).thenReturn(tokenResponse);
        when(paycoService.getPaycoId(tokenResponse.accessToken())).thenReturn("paycoId");
        when(paycoService.linkAccount("paycoId", accessToken)).thenReturn("Link successful");

        // When / Then
        mockMvc.perform(post("/api/auth/payco/link")
                .param("code", code)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Link successful"));

        verify(paycoService).getToken(code);
        verify(paycoService).getPaycoId(tokenResponse.accessToken());
        verify(paycoService).linkAccount("paycoId", accessToken);
    }

    @Test
    void memberNotFoundExceptionHandler() throws Exception {
        // Given
        String code = "invalidCode";
        TokenResponseDto tokenResponse = new TokenResponseDto("accessToken", "refreshToken");
        when(paycoService.getToken(code)).thenReturn(tokenResponse);
        when(paycoService.getPaycoId(tokenResponse.accessToken())).thenReturn("paycoId");
        when(paycoService.getMemberId("paycoId")).thenThrow(new MemberNotFoundException("Member not found"));

        // When / Then
        mockMvc.perform(post("/api/auth/payco")
                .param("code", code)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Member not found"));

        verify(paycoService).getToken(code);
        verify(paycoService).getPaycoId(tokenResponse.accessToken());
        verify(paycoService).getMemberId("paycoId");
    }

    @Test
    void alreadyLinkAccountExceptionHandler() throws Exception {
        // Given
        String code = "validCode";
        String accessToken = "Bearer someToken";
        TokenResponseDto tokenResponse = new TokenResponseDto("accessToken", "refreshToken");
        when(paycoService.getToken(code)).thenReturn(tokenResponse);
        when(paycoService.getPaycoId(tokenResponse.accessToken())).thenReturn("paycoId");
        when(paycoService.linkAccount("paycoId", accessToken))
            .thenThrow(new AlreadyLinkAccountException("Account already linked"));

        // When / Then
        mockMvc.perform(post("/api/auth/payco/link")
                .param("code", code)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Account already linked"));

        verify(paycoService).getToken(code);
        verify(paycoService).getPaycoId(tokenResponse.accessToken());
        verify(paycoService).linkAccount("paycoId", accessToken);
    }
}