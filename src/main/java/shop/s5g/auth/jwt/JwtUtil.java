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
public class JwtUtil {

    private final long accessExpirationTime;
    private final long refreshExpirationTime;
    private final SecretKey secretKey;

    public JwtUtil(@Value("${spring.jwt.secret}") String secretKey,
        @Value("${spring.jwt.token.access-expiration-time}") long accessExpirationTime,
        @Value("${spring.jwt.token.refresh-expiration-time}") long refreshExpirationTime) {
        this.secretKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8),
            SIG.HS256.key().build().getAlgorithm());
        this.accessExpirationTime = accessExpirationTime;
        this.refreshExpirationTime = refreshExpirationTime;
    }

    public String createAccessToken(String username, String role) {
        return Jwts.builder()
            .claim("loginId", username)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + accessExpirationTime))
            .signWith(secretKey)
            .compact();
    }

    public String createRefreshToken(String username) {
        return Jwts.builder()
            .claim("loginId", username)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshExpirationTime))
            .signWith(secretKey)
            .compact();
    }

    public String getUsername(String token){
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("loginId")
            .toString();
    }
}
