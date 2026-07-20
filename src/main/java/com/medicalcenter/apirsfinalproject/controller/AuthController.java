package com.medicalcenter.apirsfinalproject.controller;

import com.medicalcenter.apirsfinalproject.dto.request.AuthRequest;
import com.medicalcenter.apirsfinalproject.dto.response.AuthResponse;
import com.medicalcenter.apirsfinalproject.security.CustomUserDetails;
import com.medicalcenter.apirsfinalproject.security.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getCorreo(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        String jwt = jwtUtils.generateToken(userDetails);
        
        return ResponseEntity.ok(AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(userDetails.getUser().getId())
                .nombre(userDetails.getUser().getNombre())
                .apellidos(userDetails.getUser().getApellidos())
                .rol(userDetails.getUser().getRol().name())
                .build());
    }
}
