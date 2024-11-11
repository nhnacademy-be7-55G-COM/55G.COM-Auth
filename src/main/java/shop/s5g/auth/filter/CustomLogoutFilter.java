package shop.s5g.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.GenericFilterBean;
import shop.s5g.auth.jwt.JwtUtil;
import shop.s5g.auth.service.TokenService;

@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
        FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestUri = request.getRequestURI();
        if (!requestUri.matches("/api/auth/logout") && !requestUri.matches("/api/auth/admin/logout")) {
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
        String uuid = jwtUtil.getUUID(refreshToken);

        if(!tokenService.deleteToken(uuid)){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
