package shop.s5g.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import shop.s5g.auth.adapter.ShopUserAdapter;
import shop.s5g.auth.dto.LoginResponseDto;
import shop.s5g.auth.exception.AdminNotFoundException;
import shop.s5g.auth.exception.InvalidResponseException;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private ShopUserAdapter shopUserAdapter;

    @InjectMocks
    private AdminService adminService;

    @Test
    void testGetAdminSuccess() {
        String loginId = "admin1";
        String password = "password1";
        LoginResponseDto responseDto = new LoginResponseDto(loginId, password);
        ResponseEntity<LoginResponseDto> responseEntity = new ResponseEntity<>(responseDto, HttpStatus.OK);

        when(shopUserAdapter.getAdminInfo(loginId)).thenReturn(responseEntity);

        UserDetails admin = adminService.getAdmin(loginId);

        assertEquals(loginId, admin.getUsername(), "The username should match the loginId");
        assertEquals(password, admin.getPassword(), "The password should match the expected password");
        assertEquals("ROLE_ADMIN", admin.getAuthorities().iterator().next().getAuthority(), "The role should be ROLE_ADMIN");
    }

    @Test
    void testGetAdminEmptyBody() {
        String loginId = "admin1";
        ResponseEntity<LoginResponseDto> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(shopUserAdapter.getAdminInfo(loginId)).thenReturn(responseEntity);

        assertThrows(InvalidResponseException.class, () -> adminService.getAdmin(loginId), "Should throw InvalidResponseException if body is empty");
    }

    @Test
    void testGetAdminNotFound() {
        String loginId = "nonexistentAdmin";
        when(shopUserAdapter.getAdminInfo(loginId)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(AdminNotFoundException.class, () -> adminService.getAdmin(loginId), "Should throw AdminNotFoundException if admin is not found");
    }

    @Test
    void testGetAdminServerError() {
        String loginId = "admin1";
        when(shopUserAdapter.getAdminInfo(loginId)).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(AdminNotFoundException.class, () -> adminService.getAdmin(loginId), "Should throw AdminNotFoundException on server error");
    }

    @Test
    void testGetAdminNon2xxStatus() {
        String loginId = "admin1";
        ResponseEntity<LoginResponseDto> responseEntity = new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        when(shopUserAdapter.getAdminInfo(loginId)).thenReturn(responseEntity);

        assertThrows(AdminNotFoundException.class, () -> adminService.getAdmin(loginId), "Should throw AdminNotFoundException on non-2xx status");
    }
}