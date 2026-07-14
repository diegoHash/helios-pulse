package com.helios.platform.pulse.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        String authority = userDetails.getAuthorities().iterator().next().getAuthority();
        if (authority.startsWith("ROLE_")) {
            authority = authority.substring(5);
        }
        extraClaims.put("role", authority);
        if (userDetails instanceof com.helios.platform.pulse.entities.UserEntity) {
            com.helios.platform.pulse.entities.UserEntity ue = (com.helios.platform.pulse.entities.UserEntity) userDetails;
            extraClaims.put("permissions", ue.getPermissions());
            extraClaims.put("operatorName", ue.getOperatorName());
            extraClaims.put("avatar", ue.getAvatar());
            extraClaims.put("email", ue.getEmail());
            extraClaims.put("hasTelegram", ue.getTelegramChatId() != null && !ue.getTelegramChatId().isEmpty());
            extraClaims.put("originApp", ue.getOriginApp());
        }
        return generateToken(extraClaims, userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // HS256 requires a key of at least 256 bits (32 bytes).
            // Instead of insecure zero-padding, we use a cryptographic hash to safely expand the short key to 256 bits.
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                return Keys.hmacShaKeyFor(md.digest(keyBytes));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA-256 not available", e);
            }
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
