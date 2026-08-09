package in.rautkart.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    /** The placeholder committed to the repository, so it can be recognised. */
    private static final String PUBLIC_PLACEHOLDER_SECRET =
            "cmF1dGthcnQtbG9jYWwtZGV2LXNlY3JldC1jaGFuZ2UtbWUtcGxlYXNlLTEyMzQ1Ng==";

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${rautkart.jwt.secret}") String secret,
                      @Value("${rautkart.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;

        if (PUBLIC_PLACEHOLDER_SECRET.equals(secret)) {
            log.warn("======================================================================");
            log.warn("  Running with the PUBLIC placeholder JWT secret from the repository.");
            log.warn("  Anyone who has read the source can forge tokens, including admin");
            log.warn("  tokens. Fine locally - never expose this to a network.");
            log.warn("  Set JWT_SECRET to a private value:  openssl rand -base64 32");
            log.warn("======================================================================");
        }
    }

    public String generateToken(AuthUser user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getUsername())
                .claims(Map.of("uid", user.getId(), "role", user.getRole()))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    /** Returns the email inside the token, or null when the token is unusable. */
    public String extractEmail(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
