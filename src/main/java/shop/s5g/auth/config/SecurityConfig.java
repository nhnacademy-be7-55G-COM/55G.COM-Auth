package shop.s5g.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import shop.s5g.auth.adapter.ShopUserAdapter;
import shop.s5g.auth.filter.CustomAdminLoginFilter;
import shop.s5g.auth.filter.CustomLoginFilter;
import shop.s5g.auth.filter.CustomLogoutFilter;
import shop.s5g.auth.jwt.JwtUtil;
import shop.s5g.auth.service.CustomAdminDetailService;
import shop.s5g.auth.service.CustomUserDetailService;
import shop.s5g.auth.service.TokenService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final ShopUserAdapter shopUserAdapter;
    private final CustomAdminDetailService customAdminDetailService;
    private final CustomUserDetailService customUserDetailService;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(AbstractHttpConfigurer::disable)

            .csrf(AbstractHttpConfigurer::disable)

            .formLogin(AbstractHttpConfigurer::disable)

            .httpBasic(AbstractHttpConfigurer::disable)

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(authorizeRequests ->
            authorizeRequests.anyRequest().permitAll());

        CustomLoginFilter customLoginFilter = new CustomLoginFilter(
            memberAuthenticationManager(), objectMapper, tokenService, shopUserAdapter);

        CustomAdminLoginFilter customAdminLoginFilter = new CustomAdminLoginFilter(
            adminAuthenticationManager(), objectMapper, tokenService);

        customAdminLoginFilter.setFilterProcessesUrl("/api/auth/admin/login");
        customLoginFilter.setFilterProcessesUrl("/api/auth/login");

        http.addFilterBefore(customLoginFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(customAdminLoginFilter, UsernamePasswordAuthenticationFilter.class);

        http.addFilterBefore(new CustomLogoutFilter(jwtUtil, tokenService), LogoutFilter.class);
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public DaoAuthenticationProvider memberAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailService);
        provider.setPasswordEncoder(bCryptPasswordEncoder());
        return provider;
    }

    @Bean
    public DaoAuthenticationProvider adminAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customAdminDetailService);
        provider.setPasswordEncoder(bCryptPasswordEncoder());
        return provider;
    }

    @Bean
    @Primary
    public AuthenticationManager memberAuthenticationManager() {
        return new ProviderManager(Collections.singletonList(memberAuthenticationProvider()));
    }

    @Bean
    public AuthenticationManager adminAuthenticationManager() {
        return new ProviderManager(Collections.singletonList(adminAuthenticationProvider()));
    }
}
