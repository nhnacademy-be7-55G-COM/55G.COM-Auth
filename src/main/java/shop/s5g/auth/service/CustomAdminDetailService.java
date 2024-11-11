package shop.s5g.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomAdminDetailService implements UserDetailsService {

    private final AdminService adminService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails userDetails = adminService.getAdmin(username);
        if (userDetails == null) {
            throw new UsernameNotFoundException(username);
        }
        return userDetails;
    }
}
