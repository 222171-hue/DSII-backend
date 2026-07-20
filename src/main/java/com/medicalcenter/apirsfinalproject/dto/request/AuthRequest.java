package com.medicalcenter.apirsfinalproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {
    @NotBlank
    private String correo;
    
    @NotBlank
    private String password;
}
