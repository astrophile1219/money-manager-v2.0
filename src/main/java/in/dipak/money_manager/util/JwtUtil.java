package in.dipak.money_manager.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // =========================
    // CHANGE 1: Secret key (NO CHANGE in value)
    // =========================
    // NOTE: In production, move this to application.properties
    private static final String SECRET =
            "TmV3U2VjcmV0S2V5Rm9ySnd0MjU2Qml0c1dpdGhTb21lRXh0cmFTZWN1cml0eQ==";

    // =========================
    // CHANGE 2: Token expiration moved to CONSTANT
    // OLD: 1000 * 60 * 30  (30 minutes)
    // NEW: 24 HOURS
    // =========================
    private static final long JWT_EXPIRATION_TIME =
            1000 * 60 * 60 * 24; // 24 hours

    // =========================
    // 1. Generate Token
    // =========================
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    // =========================
    // 2. Create Token
    // =========================
    private String createToken(Map<String, Object> claims, String username) {

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))

                // =========================
                // CHANGE 3: Use constant instead of hard-coded value
                // =========================
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_TIME))

                .signWith(getSignKey(), Jwts.SIG.HS256)
                .compact();
    }

    // =========================
    // 3. Get Signing Key (JJWT 0.12.3 compatible)
    // =========================
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // =========================
    // 4. Extract Username
    // =========================
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // =========================
    // 5. Extract Expiration
    // =========================
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // =========================
    // 6. Generic Claim Extractor
    // =========================
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // =========================
    // 7. Extract All Claims
    // =========================
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSignKey()) // REQUIRED in jjwt 0.12.3
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // =========================
    // 8. Check Token Expiry
    // =========================
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // =========================
    // 9. Validate Token
    // =========================
    public Boolean validateToken(String token, UserDetails userDetails) {

        final String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }
}
