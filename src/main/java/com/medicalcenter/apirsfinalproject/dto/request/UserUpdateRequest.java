package com.medicalcenter.apirsfinalproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotBlank
    private String nombre;

    @NotBlank
    private String apellidos;

    @NotBlank
    private String dni;

    @NotBlank
    @jakarta.validation.constraints.Email
    private String correo;

    @NotBlank
    private String celular;

    @NotBlank
    private String status;

    private String password;

    // Solo para estudiantes
    private String codigoEstudiantil;
    private String carrera;

    // Solo para especialistas
    private String especialidad;
}
