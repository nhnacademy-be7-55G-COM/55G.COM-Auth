package shop.s5g.auth.service;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import shop.s5g.auth.adapter.ShopUserAdapter;
import shop.s5g.auth.dto.LoginResponseDto;
import shop.s5g.auth.exception.InvalidResponseException;
import shop.s5g.auth.exception.MemberNotFoundException;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private ShopUserAdapter shopUserAdapter;

    @InjectMocks
    private MemberService memberService;

    @Test
    void testGetMemberSuccess() {
        // Given
        String loginId = "member1";
        String password = "password";
        LoginResponseDto responseDto = new LoginResponseDto(loginId, password);
        ResponseEntity<LoginResponseDto> response = new ResponseEntity<>(responseDto, HttpStatus.OK);

        when(shopUserAdapter.getUserInfo(loginId)).thenReturn(response);

        // When
        UserDetails userDetails = memberService.getMember(loginId);

        // Then
        assertNotNull(userDetails);
        assertEquals(loginId, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MEMBER")));
    }

    @Test
    void testGetMemberNotFound() {
        // Given
        String loginId = "nonexistentMember";
        when(shopUserAdapter.getUserInfo(loginId)).thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // When & Then
        assertThrows(MemberNotFoundException.class, () -> memberService.getMember(loginId),
            "Should throw MemberNotFoundException for non-existent member");
    }

    @Test
    void testGetMemberWithEmptyBody() {
        // Given
        String loginId = "member1";
        ResponseEntity<LoginResponseDto> response = new ResponseEntity<>(null, HttpStatus.OK);
        when(shopUserAdapter.getUserInfo(loginId)).thenReturn(response);

        // When & Then
        assertThrows(InvalidResponseException.class, () -> memberService.getMember(loginId),
            "Should throw InvalidResponseException when response body is empty");
    }

    @Test
    void testGetMemberClientError() {
        // Given
        String loginId = "member1";
        when(shopUserAdapter.getUserInfo(loginId)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When & Then
        assertThrows(MemberNotFoundException.class, () -> memberService.getMember(loginId),
            "Should throw MemberNotFoundException on client error");
    }

    @Test
    void testGetMemberServerError() {
        // Given
        String loginId = "member1";
        when(shopUserAdapter.getUserInfo(loginId)).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // When & Then
        assertThrows(MemberNotFoundException.class, () -> memberService.getMember(loginId),
            "Should throw MemberNotFoundException on server error");
    }
}