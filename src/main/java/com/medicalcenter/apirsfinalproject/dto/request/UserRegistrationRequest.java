package com.medicalcenter.apirsfinalproject.dto.request;

import com.medicalcenter.apirsfinalproject.entity.Role;
import com.medicalcenter.apirsfinalproject.entity.Specialty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRegistrationRequest {
    @NotBlank
    private String nombre;

    @NotBlank
    private String apellidos;

    @NotBlank
    private String dni;

    @NotBlank
    @Email
    private String correo;

    @NotBlank
    private String celular;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotNull
    private Role rol;

    // Solo para estudiantes
    private String codigoEstudiantil;
    private String carrera;

    // Solo para especialistas
    private String especialidad;
}
