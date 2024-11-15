package shop.s5g.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import shop.s5g.auth.service.TokenService;

@RequiredArgsConstructor
public class CustomAdminLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager adminAuthenticationManager;
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
        HttpServletResponse response) throws AuthenticationException {
        return CustomLoginFilter.getAuthentication(request, objectMapper, adminAuthenticationManager);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
        HttpServletResponse response, FilterChain chain, Authentication authResult)
        throws IOException, ServletException {

        CustomLoginFilter.executeSuccessLogin(response, authResult, tokenService, objectMapper);
    }
}
