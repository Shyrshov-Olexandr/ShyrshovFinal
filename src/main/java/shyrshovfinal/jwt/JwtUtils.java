package shyrshovfinal.jwt;

import shyrshovfinal.models.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;


    public boolean validateJwtToken(String jwt) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(jwt);
            return true;
        } catch (MalformedJwtException e) {
            System.err.println(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }

        return false;
    }


    public String getUserNameFromJwtToken(String jwt) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(jwt).getBody().getSubject();
    }
    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(1200); //20min
        return Jwts.builder()
                .setSubject("refresh-token")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .claim("userId", user.getId())
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String generateAccessToken(User user) {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(600); // 10min
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }


}
