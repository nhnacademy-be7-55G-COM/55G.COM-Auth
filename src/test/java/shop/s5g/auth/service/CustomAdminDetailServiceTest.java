package shop.s5g.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomAdminDetailServiceTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private CustomAdminDetailService customAdminDetailService;

    @Test
    void testLoadUserByUsernameSuccess() {
        // Given
        String username = "admin1";
        UserDetails mockUserDetails = new User(username, "password", List.of());
        when(adminService.getAdmin(username)).thenReturn(mockUserDetails);

        // When
        UserDetails userDetails = customAdminDetailService.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        // Given
        String username = "nonexistentAdmin";
        when(adminService.getAdmin(username)).thenReturn(null);

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> customAdminDetailService.loadUserByUsername(username),
            "Should throw UsernameNotFoundException if admin is not found");
    }
}