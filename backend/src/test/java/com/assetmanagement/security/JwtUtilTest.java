package com.assetmanagement.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.ExpiredJwtException;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET = "verysecretkeythatisatleast256bitslong1234567890";
    private static final Long EXPIRATION = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class GenerateTokenTests {

        @Test
        @DisplayName("Should generate token with correct subject")
        void shouldGenerateTokenWithCorrectSubject() {
            String email = "test@example.com";
            String role = "ADMIN";

            String token = jwtUtil.generateToken(email, role);

            assertNotNull(token);
            assertEquals(email, jwtUtil.extractUsername(token));
        }

        @Test
        @DisplayName("Should generate token with correct role claim")
        void shouldGenerateTokenWithCorrectRole() {
            String email = "test@example.com";
            String role = "EMPLOYEE";

            String token = jwtUtil.generateToken(email, role);

            assertEquals(role, jwtUtil.extractRole(token));
        }

        @Test
        @DisplayName("Should generate different tokens for different users")
        void shouldGenerateDifferentTokensForDifferentUsers() {
            String token1 = jwtUtil.generateToken("user1@example.com", "ADMIN");
            String token2 = jwtUtil.generateToken("user2@example.com", "EMPLOYEE");

            assertNotEquals(token1, token2);
        }
    }

    @Nested
    @DisplayName("Token Extraction Tests")
    class ExtractClaimsTests {

        @Test
        @DisplayName("Should extract username from token")
        void shouldExtractUsername() {
            String email = "admin@company.com";
            String token = jwtUtil.generateToken(email, "ADMIN");

            String extractedUsername = jwtUtil.extractUsername(token);

            assertEquals(email, extractedUsername);
        }

        @Test
        @DisplayName("Should extract role from token")
        void shouldExtractRole() {
            String role = "ADMIN";
            String token = jwtUtil.generateToken("test@example.com", role);

            String extractedRole = jwtUtil.extractRole(token);

            assertEquals(role, extractedRole);
        }

        @Test
        @DisplayName("Should extract expiration date from token")
        void shouldExtractExpiration() {
            String token = jwtUtil.generateToken("test@example.com", "ADMIN");

            Date expiration = jwtUtil.extractExpiration(token);

            assertNotNull(expiration);
            assertTrue(expiration.after(new Date()));
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class ValidateTokenTests {

        @Test
        @DisplayName("Should validate token for correct user")
        void shouldValidateTokenForCorrectUser() {
            String email = "test@example.com";
            String token = jwtUtil.generateToken(email, "ADMIN");
            UserDetails userDetails = new User(email, "password",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

            assertTrue(jwtUtil.validateToken(token, userDetails));
        }

        @Test
        @DisplayName("Should not validate token for different user")
        void shouldNotValidateTokenForDifferentUser() {
            String token = jwtUtil.generateToken("user1@example.com", "ADMIN");
            UserDetails userDetails = new User("user2@example.com", "password",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

            assertFalse(jwtUtil.validateToken(token, userDetails));
        }

        @Test
        @DisplayName("Should throw ExpiredJwtException for expired token")
        void shouldThrowExceptionForExpiredToken() {
            JwtUtil expiredJwtUtil = new JwtUtil();
            ReflectionTestUtils.setField(expiredJwtUtil, "secret", SECRET);
            ReflectionTestUtils.setField(expiredJwtUtil, "expiration", -1000L); // Already expired

            String token = expiredJwtUtil.generateToken("test@example.com", "ADMIN");
            UserDetails userDetails = new User("test@example.com", "password",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

            // JJWT throws ExpiredJwtException during token parsing, before validateToken can return false
            // This is expected behavior - the exception is caught in JwtAuthenticationFilter
            assertThrows(ExpiredJwtException.class,
                    () -> expiredJwtUtil.validateToken(token, userDetails));
        }
    }

    @Nested
    @DisplayName("Token Format Tests")
    class TokenFormatTests {

        @Test
        @DisplayName("Generated token should have three parts separated by dots")
        void tokenShouldHaveThreeParts() {
            String token = jwtUtil.generateToken("test@example.com", "ADMIN");

            String[] parts = token.split("\\.");
            assertEquals(3, parts.length, "JWT should have header, payload, and signature");
        }

        @Test
        @DisplayName("Should throw exception for invalid token")
        void shouldThrowExceptionForInvalidToken() {
            assertThrows(Exception.class, () -> jwtUtil.extractUsername("invalid.token.here"));
        }

        @Test
        @DisplayName("Should throw exception for malformed token")
        void shouldThrowExceptionForMalformedToken() {
            assertThrows(Exception.class, () -> jwtUtil.extractUsername("not-a-jwt"));
        }
    }
}
