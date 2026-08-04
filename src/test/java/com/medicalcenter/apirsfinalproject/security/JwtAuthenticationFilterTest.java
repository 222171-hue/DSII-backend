package com.medicalcenter.apirsfinalproject.security;

import com.medicalcenter.apirsfinalproject.entity.Role;
import com.medicalcenter.apirsfinalproject.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtUtils, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private UserDetails buildUserDetails() {
        User user = new User();
        user.setId("u1");
        user.setCorreo("juan@unamba.edu.pe");
        user.setRol(Role.STUDENT);
        user.setPassword("encrypted");
        return new CustomUserDetails(user);
    }

    @Test
    void continuesWhenNoAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void continuesWhenHeaderIsNotBearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void continuesWhenTokenIsInvalid() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtUtils.extractUsername(anyString())).thenThrow(new RuntimeException("bad token"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void setsAuthenticationWhenTokenValid() throws Exception {
        UserDetails userDetails = buildUserDetails();
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtils.extractUsername(anyString())).thenReturn("juan@unamba.edu.pe");
        when(userDetailsService.loadUserByUsername("juan@unamba.edu.pe")).thenReturn(userDetails);
        when(jwtUtils.validateToken(anyString(), any(UserDetails.class))).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void skipsAuthenticationWhenValidationFails() throws Exception {
        UserDetails userDetails = buildUserDetails();
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtils.extractUsername(anyString())).thenReturn("juan@unamba.edu.pe");
        when(userDetailsService.loadUserByUsername("juan@unamba.edu.pe")).thenReturn(userDetails);
        when(jwtUtils.validateToken(anyString(), any(UserDetails.class))).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
