package com.medicalcenter.apirsfinalproject.controller;

import com.medicalcenter.apirsfinalproject.dto.request.AuthRequest;
import com.medicalcenter.apirsfinalproject.dto.response.AuthResponse;
import com.medicalcenter.apirsfinalproject.entity.Role;
import com.medicalcenter.apirsfinalproject.entity.Specialist;
import com.medicalcenter.apirsfinalproject.entity.Specialty;
import com.medicalcenter.apirsfinalproject.entity.User;
import com.medicalcenter.apirsfinalproject.security.CustomUserDetails;
import com.medicalcenter.apirsfinalproject.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginReturnsTokenForStudent() {
        User user = new User();
        user.setId("u1");
        user.setNombre("Juan");
        user.setApellidos("Perez");
        user.setCorreo("juan@unamba.edu.pe");
        user.setRol(Role.STUDENT);
        CustomUserDetails details = new CustomUserDetails(user);

        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(details);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtils.generateToken(details)).thenReturn("jwt-token");

        AuthRequest request = new AuthRequest();
        request.setCorreo("juan@unamba.edu.pe");
        request.setPassword("secret");

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertEquals(200, response.getStatusCode().value());
        AuthResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("jwt-token", body.getToken());
        assertEquals("Bearer", body.getType());
        assertEquals("STUDENT", body.getRol());
    }

    @Test
    void loginIncludesEspecialidadForSpecialist() {
        Specialty specialty = new Specialty();
        specialty.setName("Medicina General");
        Specialist specialist = new Specialist();
        specialist.setId("u2");
        specialist.setNombre("Doctor");
        specialist.setApellidos("Medico");
        specialist.setCorreo("doc@medico.com");
        specialist.setRol(Role.SPECIALIST);
        specialist.setEspecialidad(specialty);
        CustomUserDetails details = new CustomUserDetails(specialist);

        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(details);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtUtils.generateToken(details)).thenReturn("jwt-token");

        AuthRequest request = new AuthRequest();
        request.setCorreo("doc@medico.com");
        request.setPassword("secret");

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertEquals("Medicina General", response.getBody().getEspecialidad());
    }
}
