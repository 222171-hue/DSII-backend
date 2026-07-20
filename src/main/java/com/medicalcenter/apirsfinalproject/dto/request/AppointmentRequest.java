package com.medicalcenter.apirsfinalproject.dto.request;

import com.medicalcenter.apirsfinalproject.entity.Specialty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentRequest {
    @NotBlank
    private String specialistId;

    @NotBlank
    private String specialty;

    @NotNull
    @Future
    private LocalDateTime dateTime;

    @NotBlank
    private String reason;
}
