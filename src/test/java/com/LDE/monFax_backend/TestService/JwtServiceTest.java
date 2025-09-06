package com.LDE.monFax_backend.TestService;

import com.LDE.monFax_backend.models.User;
import com.LDE.monFax_backend.services.JwtService;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // On injecte une clé secrète de test (au moins 32 caractères pour HS256)
        ReflectionTestUtils.setField(jwtService, "secret", "mySuperSecretKeyForJwtTesting1234567890");
    }

    @Test
    void testGenerateTokenAndExtractClaims() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        // Génération du token
        String token = jwtService.generateToken(user);
        assertNotNull(token);

        // Extraction des claims
        Claims claims = jwtService.extractClaims(token);

        assertEquals("test@example.com", claims.getSubject());
        assertEquals("testuser", claims.get("username"));
        assertEquals(1, ((Number) claims.get("id")).longValue()); // conversion Number -> long
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void testExtractClaims_InvalidToken() {
        String invalidToken = "this.is.not.a.jwt";

        assertThrows(Exception.class, () -> jwtService.extractClaims(invalidToken));
    }
}

