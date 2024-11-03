package shop.s5g.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private final long ACCESS_EXPIRATION_TIME;
    private final long REFRESH_EXPIRATION_TIME;
    private final SecretKey secretKey;

    public JwtProvider(@Value("${spring.jwt.secret}") String secretKey,
        @Value("${spring.jwt.token.access-expiration-time}") long accessExpirationTime,
        @Value("${spring.jwt.token.refresh-expiration-time}") long refreshExpirationTime) {
        this.secretKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8),
            SIG.HS256.key().build().getAlgorithm());
        this.ACCESS_EXPIRATION_TIME = accessExpirationTime;
        this.REFRESH_EXPIRATION_TIME = refreshExpirationTime;
    }

    public String createAccessToken(String username, String role) {
        return Jwts.builder()
            .claim("username", username)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION_TIME))
            .signWith(secretKey)
            .compact();
    }

}
