package com.medicalcenter.apirsfinalproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserUpdateRequest extends BaseUserRequest {

    @NotBlank
    private String status;

    private String password;
}
