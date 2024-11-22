package shop.s5g.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import shop.s5g.auth.adapter.PaycoMemberInfoAdapter;
import shop.s5g.auth.adapter.PaycoTokenAdapter;
import shop.s5g.auth.adapter.ShopUserAdapter;
import shop.s5g.auth.dto.MemberLoginIdResponseDto;
import shop.s5g.auth.dto.MessageDto;
import shop.s5g.auth.dto.TokenResponseDto;
import shop.s5g.auth.dto.payco.DataDto;
import shop.s5g.auth.dto.payco.HeaderDto;
import shop.s5g.auth.dto.payco.MemberDto;
import shop.s5g.auth.dto.payco.PaycoMemberResponseDto;
import shop.s5g.auth.dto.payco.PaycoResponseDto;
import shop.s5g.auth.exception.AlreadyLinkAccountException;
import shop.s5g.auth.exception.MemberNotFoundException;
import shop.s5g.auth.exception.PaycoGetMemberInfoFailedException;
import shop.s5g.auth.exception.PaycoGetTokenFailedException;
import shop.s5g.auth.exception.PaycoLinkAccountFailedException;

@ExtendWith(MockitoExtension.class)
class PaycoServiceTest {
    @Mock
    private PaycoTokenAdapter paycoTokenAdapter;

    @Mock
    private PaycoMemberInfoAdapter paycoMemberInfoAdapter;

    @Mock
    private ShopUserAdapter shopUserAdapter;

    @InjectMocks
    private PaycoService paycoService;

    @BeforeEach
    void setUp() {
        paycoService = new PaycoService(paycoTokenAdapter, paycoMemberInfoAdapter, shopUserAdapter);
    }

    @Test
    void getToken_Success() {
        // Given
        String code = "validCode";
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        params.put("client_id", null);
        params.put("client_secret", null);

        PaycoResponseDto mockResponse = new PaycoResponseDto(
            "accessToken",
            "accessTokenSecret",
            "refreshToken",
            "Bearer",
            "3600",
            "stateValue"
        );
        when(paycoTokenAdapter.getToken(params)).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        TokenResponseDto result = paycoService.getToken(code);

        // Then
        assertNotNull(result);
        assertEquals("accessToken", result.accessToken());
        assertEquals("refreshToken", result.refreshToken());
    }

    @Test
    void getToken_Failure() {
        // Given
        String code = "invalidCode";
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        params.put("client_id", null); // Mock 환경에서 null 처리
        params.put("client_secret", null); // Mock 환경에서 null 처리

        // Create a FeignException with 400 status (or relevant failure status)
        RequestTemplate requestTemplate = new RequestTemplate();
        requestTemplate.method("POST");
        requestTemplate.uri("/oauth/token");

        Request request = Request.create(
            Request.HttpMethod.POST,
            "/oauth/token",
            Collections.emptyMap(),
            null,
            StandardCharsets.UTF_8,
            requestTemplate
        );

        FeignException exception = FeignException.errorStatus(
            "Bad Request",
            Response.builder()
                .status(400)
                .reason("Bad Request")
                .request(request)
                .headers(Collections.emptyMap())
                .build()
        );

        when(paycoTokenAdapter.getToken(params)).thenThrow(exception);

        // When / Then
        assertThrows(PaycoGetTokenFailedException.class, () -> paycoService.getToken(code));
    }

    @Test
    void getPaycoId_Success() {
        // Given
        String accessToken = "validAccessToken";
        HeaderDto header = new HeaderDto(true, 0, "Success");
        MemberDto member = new MemberDto("12345");
        DataDto data = new DataDto(member);

        PaycoMemberResponseDto mockResponse = new PaycoMemberResponseDto(header, data);

        when(paycoMemberInfoAdapter.getMemberInfo(null, accessToken))
            .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        String result = paycoService.getPaycoId(accessToken);

        // Then
        assertNotNull(result);
        assertEquals("12345", result);
    }

    @Test
    void getPaycoId_Failure_InvalidResponse() {
        // Given
        String accessToken = "invalidAccessToken";
        HeaderDto header = new HeaderDto(false, 500, "Failure");
        PaycoMemberResponseDto mockResponse = new PaycoMemberResponseDto(header, null);

        when(paycoMemberInfoAdapter.getMemberInfo(null, accessToken))
            .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.INTERNAL_SERVER_ERROR));

        // When / Then
        assertThrows(PaycoGetMemberInfoFailedException.class, () -> paycoService.getPaycoId(accessToken));
    }

    @Test
    void getPaycoId_Failure_Exception() {
        // Given
        String accessToken = "invalidAccessToken";
        when(paycoMemberInfoAdapter.getMemberInfo(null, accessToken))
            .thenThrow(FeignException.class);

        // When / Then
        assertThrows(PaycoGetMemberInfoFailedException.class, () -> paycoService.getPaycoId(accessToken));
    }
    @Test
    void getMemberId_Success() {
        // Given
        String paycoId = "validPaycoId";
        MemberLoginIdResponseDto mockResponse = new MemberLoginIdResponseDto("loginId123");
        when(shopUserAdapter.getMemberIdByPaycoId(paycoId))
            .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        String result = paycoService.getMemberId(paycoId);

        // Then
        assertNotNull(result);
        assertEquals("loginId123", result);
    }

    @Test
    void getMemberId_NotFound() {
        // Given
        String paycoId = "notFoundPaycoId";

        // Create a FeignException with 404 status
        RequestTemplate requestTemplate = new RequestTemplate();
        requestTemplate.method(Request.HttpMethod.GET);
        requestTemplate.uri("/api/shop/member/payco?payco_id=" + paycoId);

        Request request = Request.create(
            Request.HttpMethod.GET,
            "/api/shop/member/payco?payco_id=" + paycoId,
            Collections.emptyMap(),
            null,
            StandardCharsets.UTF_8,
            requestTemplate
        );

        FeignException exception = FeignException.errorStatus(
            "Not Found",
            Response.builder()
                .status(404)
                .reason("Not Found")
                .request(request)
                .headers(Collections.emptyMap())
                .build()
        );

        when(shopUserAdapter.getMemberIdByPaycoId(paycoId)).thenThrow(exception);

        // When / Then
        assertThrows(MemberNotFoundException.class, () -> paycoService.getMemberId(paycoId));
    }

    @Test
    void linkAccount_Success() {
        // Given
        String paycoId = "paycoId";
        String accessToken = "accessToken";
        MessageDto mockResponse = new MessageDto("Account linked successfully");
        when(shopUserAdapter.linkAccount(paycoId, accessToken))
            .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // When
        String result = paycoService.linkAccount(paycoId, accessToken);

        // Then
        assertNotNull(result);
        assertEquals("Account linked successfully", result);
    }

    @Test
    void linkAccount_AlreadyLinked() {
        // Given
        String paycoId = "paycoId";
        String accessToken = "accessToken";

        // Create a FeignException with 409 status (Conflict)
        RequestTemplate requestTemplate = new RequestTemplate();
        requestTemplate.method("POST");
        requestTemplate.uri("/api/shop/member/link");

        Request request = Request.create(
            Request.HttpMethod.POST,
            "/api/shop/member/link",
            Collections.emptyMap(),
            null,
            StandardCharsets.UTF_8,
            requestTemplate
        );

        FeignException exception = FeignException.errorStatus(
            "Conflict",
            Response.builder()
                .status(409)
                .reason("Conflict")
                .request(request)
                .headers(Collections.emptyMap())
                .build()
        );

        when(shopUserAdapter.linkAccount(paycoId, accessToken)).thenThrow(exception);

        // When / Then
        assertThrows(
            AlreadyLinkAccountException.class,
            () -> paycoService.linkAccount(paycoId, accessToken)
        );
    }

    @Test
    void linkAccount_Failure() {
        // Given
        String paycoId = "paycoId";
        String accessToken = "accessToken";
        when(shopUserAdapter.linkAccount(paycoId, accessToken)).thenThrow(FeignException.class);

        // When / Then
        assertThrows(PaycoLinkAccountFailedException.class, () -> paycoService.linkAccount(paycoId, accessToken));
    }

    @Test
    void getMemberId_PaycoGetMemberInfoFailed() {
        // Given
        String paycoId = "invalidPaycoId";


        when(shopUserAdapter.getMemberIdByPaycoId(paycoId)).thenThrow(FeignException.class);

        // When / Then
        assertThrows(
            PaycoGetMemberInfoFailedException.class,
            () -> paycoService.getMemberId(paycoId)
        );
    }
}