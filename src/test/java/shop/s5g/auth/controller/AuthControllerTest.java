package shop.s5g.auth.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import shop.s5g.auth.dto.TokenResponseDto;
import shop.s5g.auth.dto.UserDetailResponseDto;
import shop.s5g.auth.service.TokenService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenService tokenService;

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissueToken_Success() throws Exception {
        // Mocking
        String refreshToken = "sample_refresh_token";
        TokenResponseDto mockResponse = new TokenResponseDto("new_access_token", "new_refresh_token");
        Mockito.when(tokenService.reissueToken(refreshToken)).thenReturn(mockResponse);

        // Perform Request
        mockMvc.perform(post("/api/auth/reissue")
                .header("Authorization", "Bearer " + refreshToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("new_access_token"))
            .andExpect(jsonPath("$.refreshToken").value("new_refresh_token"));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - Authorization 헤더 없음")
    void reissueToken_Failure_NoHeader() throws Exception {
        mockMvc.perform(post("/api/auth/reissue"))
            .andExpect(status().isBadRequest());
    }
    @Test
    @DisplayName("토큰 재발급 실패 - Authorization 헤더 다른 걸로 시작")
    void reissueToken_Failure_HeaderDiffrence() throws Exception {
        mockMvc.perform(post("/api/auth/reissue")
                .header("Authorization", "Ba "))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("UUID로 사용자 정보 가져오기 성공")
    void getUserDetail_Success() throws Exception {
        // Mocking
        String uuid = "1234-5678-91011";
        UserDetailResponseDto mockResponse = new UserDetailResponseDto("user1" , "ROLE_USER");
        Mockito.when(tokenService.getUserByUUID(uuid)).thenReturn(mockResponse);

        // Perform Request
        mockMvc.perform(get("/api/auth/id/{uuid}", uuid))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("user1"))
            .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    @DisplayName("역할 가져오기 성공")
    void getRole_Success() throws Exception {
        // Mocking
        String accessToken = "sample_access_token";
        String role = "ROLE_ADMIN";
        Mockito.when(tokenService.getRoleByToken(accessToken)).thenReturn(role);

        // Perform Request
        mockMvc.perform(get("/api/auth/role")
                .header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("역할 가져오기 실패 - Authorization 헤더 없음")
    void getRole_Failure_NoHeader() throws Exception {
        mockMvc.perform(get("/api/auth/role"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("역할 가져오기 실패 - Authorization 헤더 다른 걸로 시작")
    void getRole_Failure_HeaderDiffrence() throws Exception {
        mockMvc.perform(get("/api/auth/role")
                .header("Authorization", "Ba "))
            .andExpect(status().isBadRequest());
    }
}