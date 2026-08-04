package com.medicalcenter.apirsfinalproject.dto.request;

import com.medicalcenter.apirsfinalproject.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserRegistrationRequest extends BaseUserRequest {

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotNull
    private Role rol;
}
