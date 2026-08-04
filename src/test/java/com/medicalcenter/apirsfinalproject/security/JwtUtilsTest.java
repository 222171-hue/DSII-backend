package com.medicalcenter.apirsfinalproject.security;

import com.medicalcenter.apirsfinalproject.entity.Role;
import com.medicalcenter.apirsfinalproject.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilsTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtils = new JwtUtils();
        Field secretField = JwtUtils.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtUtils, SECRET);
        Field expirationField = JwtUtils.class.getDeclaredField("expiration");
        expirationField.setAccessible(true);
        expirationField.set(jwtUtils, 86400000L);
    }

    private CustomUserDetails buildUserDetails() {
        User user = new User();
        user.setId("u1");
        user.setNombre("Juan");
        user.setApellidos("Perez");
        user.setCorreo("juan@unamba.edu.pe");
        user.setRol(Role.STUDENT);
        user.setPassword("encrypted");
        return new CustomUserDetails(user);
    }

    @Test
    void generateTokenReturnsValidToken() {
        String token = jwtUtils.generateToken(buildUserDetails());

        assertNotNull(token);
        assertEquals("juan@unamba.edu.pe", jwtUtils.extractUsername(token));
        assertNotNull(jwtUtils.extractExpiration(token));
        assertNotNull(jwtUtils.extractClaim(token, claims -> claims.get("rol")));
        assertNotNull(jwtUtils.extractClaim(token, claims -> claims.get("id")));
    }

    @Test
    void validateTokenReturnsTrueForValidToken() {
        CustomUserDetails userDetails = buildUserDetails();
        String token = jwtUtils.generateToken(userDetails);

        assertTrue(jwtUtils.validateToken(token, userDetails));
    }

    @Test
    void validateTokenReturnsFalseForWrongUsername() {
        CustomUserDetails userDetails = buildUserDetails();
        String token = jwtUtils.generateToken(userDetails);

        User other = new User();
        other.setCorreo("otro@unamba.edu.pe");
        other.setRol(Role.ADMIN);
        assertFalse(jwtUtils.validateToken(token, new CustomUserDetails(other)));
    }

    @Test
    void extractUsernameThrowsForInvalidToken() {
        assertThrows(Exception.class, () -> jwtUtils.extractUsername("invalid.token.value"));
    }

    @Test
    void validateTokenThrowsForExpiredToken() throws Exception {
        Field expirationField = JwtUtils.class.getDeclaredField("expiration");
        expirationField.setAccessible(true);
        expirationField.set(jwtUtils, -1000L);
        String token = jwtUtils.generateToken(buildUserDetails());

        assertThrows(Exception.class, () -> jwtUtils.validateToken(token, buildUserDetails()));
    }
}
