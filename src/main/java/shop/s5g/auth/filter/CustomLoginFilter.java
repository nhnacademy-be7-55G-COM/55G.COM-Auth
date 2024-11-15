package shop.s5g.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import shop.s5g.auth.dto.LoginRequestDto;
import shop.s5g.auth.dto.TokenResponseDto;
import shop.s5g.auth.exception.JsonConvertException;
import shop.s5g.auth.service.TokenService;

@RequiredArgsConstructor
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager memberAuthenticationManager;
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
        HttpServletResponse response) throws AuthenticationException {
        return getAuthentication(request, objectMapper, memberAuthenticationManager);

    }

    static Authentication getAuthentication(HttpServletRequest request, ObjectMapper objectMapper,
        AuthenticationManager memberAuthenticationManager) {
        LoginRequestDto loginRequestDto = null;

        try {
            loginRequestDto = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);

        } catch (IOException e) {
            throw new JsonConvertException("Failed to convert JSON to LoginRequestDto");
        }

        String username = loginRequestDto.loginId();
        String password = loginRequestDto.password();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        return memberAuthenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
        HttpServletResponse response, FilterChain chain, Authentication authResult)
        throws IOException, ServletException {

        executeSuccessLogin(response, authResult, tokenService, objectMapper);
    }

    static void executeSuccessLogin(HttpServletResponse response, Authentication authResult,
        TokenService tokenService, ObjectMapper objectMapper) throws IOException {
        UserDetails user = (UserDetails) authResult.getPrincipal();
        String username = user.getUsername();
        Collection<? extends GrantedAuthority> authorities = authResult.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        TokenResponseDto tokenResponseDto = tokenService.issueToken(username, role);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getOutputStream(), tokenResponseDto);
    }
}
