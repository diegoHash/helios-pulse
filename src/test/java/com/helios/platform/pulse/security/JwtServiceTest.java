package com.helios.platform.pulse.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Set values that would normally be injected by @Value
        ReflectionTestUtils.setField(jwtService, "secretKey", "short-secret-that-will-be-hashed-to-256-bits");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1000 * 60 * 60); // 1 hour

        userDetails = new User("testuser", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void testGenerateAndExtractUsername() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);

        String extractedUsername = jwtService.extractUsername(token);
        assertEquals("testuser", extractedUsername);
    }

    @Test
    void testIsTokenValid() {
        String token = jwtService.generateToken(userDetails);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testIsTokenValid_WrongUser() {
        String token = jwtService.generateToken(userDetails);
        UserDetails wrongUser = new User("wronguser", "password", Collections.emptyList());
        assertFalse(jwtService.isTokenValid(token, wrongUser));
    }

    @Test
    void testTokenExpiration() {
        // Create a token that expires immediately
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000);
        String token = jwtService.generateToken(userDetails);

        // Expect an ExpiredJwtException when parsing it
        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testRoleExtraction() {
        String token = jwtService.generateToken(userDetails);
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

        // "ROLE_ADMIN" should be mapped to "ADMIN"
        assertEquals("ADMIN", role);
    }
}
