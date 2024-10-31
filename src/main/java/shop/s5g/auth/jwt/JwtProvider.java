package shop.s5g.auth.jwt;

import io.jsonwebtoken.Jwts;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private final String SECRETKEY;

    public JwtProvider(@Value("${spring.jwt.secret}") String secretKey) {
        this.SECRETKEY = secretKey;
    }

}
