package shop.s5g.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import shop.s5g.auth.adapter.MemberAdapter;
import shop.s5g.auth.dto.LoginRequestDto;
import shop.s5g.auth.dto.TokenResponseDto;
import shop.s5g.auth.exception.JsonConvertException;
import shop.s5g.auth.jwt.JwtUtil;
import shop.s5g.auth.repository.RefreshTokenRepository;

public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberAdapter memberAdapter;

    public CustomLoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, RefreshTokenRepository refreshTokenRepository, MemberAdapter memberAdapter) {
        this.authenticationManager = authenticationManager;
        this.objectMapper = new ObjectMapper();
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
        this.memberAdapter = memberAdapter;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
        HttpServletResponse response) throws AuthenticationException {
        LoginRequestDto loginRequestDto = null;

        try {
            loginRequestDto = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);

        } catch (IOException e) {
            throw new JsonConvertException("Failed to convert JSON to MemberRequestDto");
        }

        String username = loginRequestDto.loginId();
        String password = loginRequestDto.password();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
        HttpServletResponse response, AuthenticationException failed)
        throws IOException, ServletException {
        //TODO 로그인 실패시 로직
        super.unsuccessfulAuthentication(request, response, failed);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
        HttpServletResponse response, FilterChain chain, Authentication authResult)
        throws IOException, ServletException {
        UserDetails user = (UserDetails) authResult.getPrincipal();
        String username = user.getUsername();
        Collection<? extends GrantedAuthority> authorities = authResult.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        String accessToken = jwtUtil.createAccessToken(username, role);
        String refreshToken = jwtUtil.createRefreshToken(username);

        memberAdapter.updateLatestLoginAt(username);

        refreshTokenRepository.saveRefreshToken(username, refreshToken);
        TokenResponseDto tokenResponseDto = new TokenResponseDto(accessToken, refreshToken);

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getOutputStream(), tokenResponseDto);
    }
}
