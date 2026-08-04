package com.medicalcenter.apirsfinalproject.controller;

import com.medicalcenter.apirsfinalproject.entity.Role;
import com.medicalcenter.apirsfinalproject.entity.Specialist;
import com.medicalcenter.apirsfinalproject.entity.Specialty;
import com.medicalcenter.apirsfinalproject.entity.User;
import com.medicalcenter.apirsfinalproject.repository.SpecialistRepository;
import com.medicalcenter.apirsfinalproject.security.CustomUserDetails;
import com.medicalcenter.apirsfinalproject.service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;
    @Mock
    private SpecialistRepository specialistRepository;

    @InjectMocks
    private ReportController reportController;

    @Test
    void getAppointmentsReportReturnsPdfForAdmin() {
        when(reportService.generateAppointmentsReport(any(), any(), any())).thenReturn(new byte[]{37, 80, 68, 70});

        User user = new User();
        user.setRol(Role.ADMIN);
        CustomUserDetails details = new CustomUserDetails(user);

        ResponseEntity<byte[]> response = reportController.getAppointmentsReport(
                details, "Medicina", LocalDate.now(), LocalDate.now());

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
    }

    @Test
    void getAppointmentsReportOverridesSpecialtyForSpecialist() {
        Specialty specialty = new Specialty();
        specialty.setName("Odontologia");
        Specialist specialist = new Specialist();
        specialist.setId("u1");
        specialist.setEspecialidad(specialty);
        when(specialistRepository.findById("u1")).thenReturn(Optional.of(specialist));
        when(reportService.generateAppointmentsReport(any(), any(), any())).thenReturn(new byte[]{37, 80, 68, 70});

        User user = new User();
        user.setId("u1");
        user.setRol(Role.SPECIALIST);
        CustomUserDetails details = new CustomUserDetails(user);

        ResponseEntity<byte[]> response = reportController.getAppointmentsReport(
                details, "ignored", LocalDate.now(), LocalDate.now());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getAppointmentsReportHandlesSpecialistWithoutEspecialidad() {
        Specialist specialist = new Specialist();
        specialist.setId("u1");
        when(specialistRepository.findById("u1")).thenReturn(Optional.of(specialist));
        when(reportService.generateAppointmentsReport(any(), any(), any())).thenReturn(new byte[]{37, 80, 68, 70});

        User user = new User();
        user.setId("u1");
        user.setRol(Role.SPECIALIST);
        CustomUserDetails details = new CustomUserDetails(user);

        ResponseEntity<byte[]> response = reportController.getAppointmentsReport(
                details, null, null, null);

        assertEquals(200, response.getStatusCode().value());
    }
}
