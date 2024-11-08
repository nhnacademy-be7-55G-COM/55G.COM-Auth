package shop.s5g.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.GenericFilterBean;
import shop.s5g.auth.jwt.JwtUtil;
import shop.s5g.auth.repository.RefreshTokenRepository;

@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
        FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestUri = request.getRequestURI();
        if (!requestUri.matches("/api/auth/logout")) {

            filterChain.doFilter(request, response);
            return;
        }

        if (!request.getMethod().equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = authorizationHeader.substring(7);
        String loginId = jwtUtil.getUsername(refreshToken);
        if(!refreshTokenRepository.isExistRefreshToken(loginId)){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        refreshTokenRepository.deleteRefreshToken(refreshToken);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
