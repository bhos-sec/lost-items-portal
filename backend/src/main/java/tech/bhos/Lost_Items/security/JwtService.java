package tech.bhos.Lost_Items.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.bhos.Lost_Items.model.AppUser;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpirationSeconds;

    public JwtService(
            @Value("${auth.jwt.secret}") String jwtSecret,
            @Value("${auth.jwt.access-token-expiration-seconds}") long accessTokenExpirationSeconds
    ) {
        this.signingKey = Keys.hmacShaKeyFor(resolveSecret(jwtSecret));
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
    }

    public String generateAccessToken(AppUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenExpirationSeconds);
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claims(Map.of("uid", user.getUserId()))
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, AppUserPrincipal principal) {
        String email = extractEmail(token);
        return email.equalsIgnoreCase(principal.email()) && !isTokenExpired(token);
    }

    public long accessTokenExpirationSeconds() {
        return accessTokenExpirationSeconds;
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    private byte[] resolveSecret(String secret) {
        try {
            return Decoders.BASE64.decode(secret);
        } catch (Exception ex) {
            return Base64.getEncoder().encode(secret.getBytes(StandardCharsets.UTF_8));
        }
    }
}
