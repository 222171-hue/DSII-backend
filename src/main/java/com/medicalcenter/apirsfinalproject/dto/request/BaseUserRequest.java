package com.medicalcenter.apirsfinalproject.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public abstract class BaseUserRequest {
    @NotBlank
    @Pattern(regexp = "^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    private String nombre;

    @NotBlank
    @Pattern(regexp = "^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]+$", message = "Los apellidos solo pueden contener letras y espacios")
    private String apellidos;

    @NotBlank
    @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener 8 dígitos numéricos")
    private String dni;

    @NotBlank
    @Email
    private String correo;

    @NotBlank
    @Pattern(regexp = "^\\d{9}$", message = "El celular debe tener 9 dígitos numéricos")
    private String celular;

    // Solo para estudiantes
    @Pattern(regexp = "^\\d{6}$", message = "El código estudiantil debe tener 6 dígitos numéricos")
    private String codigoEstudiantil;
    private String carrera;

    // Solo para especialistas
    private String especialidad;
}
