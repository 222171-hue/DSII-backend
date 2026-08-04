package com.medicalcenter.apirsfinalproject.controller;

import com.medicalcenter.apirsfinalproject.dto.request.AppointmentRequest;
import com.medicalcenter.apirsfinalproject.entity.Appointment;
import com.medicalcenter.apirsfinalproject.entity.AppointmentStatus;
import com.medicalcenter.apirsfinalproject.entity.Role;
import com.medicalcenter.apirsfinalproject.entity.User;
import com.medicalcenter.apirsfinalproject.security.CustomUserDetails;
import com.medicalcenter.apirsfinalproject.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

    private CustomUserDetails buildDetails(String id, Role role) {
        User user = new User();
        user.setId(id);
        user.setCorreo(id + "@unamba.edu.pe");
        user.setRol(role);
        return new CustomUserDetails(user);
    }

    @Test
    void bookAppointmentReturnsCreated() {
        when(appointmentService.bookAppointment(eq("u1"), any(AppointmentRequest.class)))
                .thenReturn(new Appointment());
        CustomUserDetails details = buildDetails("u1", Role.STUDENT);

        ResponseEntity<Appointment> response = appointmentController.bookAppointment(details, new AppointmentRequest());

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void getPendingAppointmentsReturnsList() {
        when(appointmentService.getPendingAppointmentsForStudent("u1")).thenReturn(List.of(new Appointment()));
        CustomUserDetails details = buildDetails("u1", Role.STUDENT);

        ResponseEntity<List<Appointment>> response = appointmentController.getPendingAppointmentsForStudent(details);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void cancelAppointmentReturnsNoContent() {
        CustomUserDetails details = buildDetails("u1", Role.STUDENT);

        ResponseEntity<Void> response = appointmentController.cancelAppointmentByStudent(details, "app1");

        assertEquals(204, response.getStatusCode().value());
        verify(appointmentService).cancelAppointmentByStudent("u1", "app1");
    }

    @Test
    void getAppointmentsForSpecialistReturnsList() {
        when(appointmentService.getAppointmentsForSpecialist(eq("u1"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new Appointment()));
        CustomUserDetails details = buildDetails("u1", Role.SPECIALIST);

        ResponseEntity<List<Appointment>> response = appointmentController.getAppointmentsForSpecialist(
                details, LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getAppointmentsForSpecialistByAdminReturnsList() {
        when(appointmentService.getAppointmentsForSpecialist(eq("u2"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new Appointment()));

        ResponseEntity<List<Appointment>> response = appointmentController.getAppointmentsForSpecialistByAdmin(
                "u2", LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getOccupiedSlotsReturnsMap() {
        when(appointmentService.getOccupiedSlotsForSpecialist(eq("u2"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Map.of("specialistId", "u2"));

        ResponseEntity<Map<String, Object>> response = appointmentController.getOccupiedSlots(
                "u2", LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        assertEquals(200, response.getStatusCode().value());
        assertEquals("u2", response.getBody().get("specialistId"));
    }

    @Test
    void changeAppointmentStatusReturnsNoContent() {
        CustomUserDetails details = buildDetails("u1", Role.SPECIALIST);

        ResponseEntity<Void> response = appointmentController.changeAppointmentStatus(
                details, "app1", Map.of("status", "ATENDIDO", "cancelReason", ""));

        assertEquals(204, response.getStatusCode().value());
        verify(appointmentService).changeAppointmentStatus(eq("u1"), eq("app1"), eq(AppointmentStatus.ATENDIDO), anyString());
    }

    @Test
    void getAllAppointmentsReturnsList() {
        when(appointmentService.getAllAppointments()).thenReturn(List.of(new Appointment()));

        ResponseEntity<List<Appointment>> response = appointmentController.getAllAppointments();

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void blockSlotReturnsOk() {
        when(appointmentService.blockSlot(eq("u1"), any(LocalDateTime.class))).thenReturn(new Appointment());
        CustomUserDetails details = buildDetails("u1", Role.SPECIALIST);

        ResponseEntity<Object> response = appointmentController.blockSlot(
                details, Map.of("dateTime", "2026-08-10T10:00:00"));

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void blockSlotReturnsServerErrorOnBadDate() {
        CustomUserDetails details = buildDetails("u1", Role.SPECIALIST);

        ResponseEntity<Object> response = appointmentController.blockSlot(
                details, Map.of("dateTime", "not-a-date"));

        assertEquals(500, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Map);
    }

    @Test
    void unblockSlotReturnsNoContent() {
        CustomUserDetails details = buildDetails("u1", Role.SPECIALIST);

        ResponseEntity<Void> response = appointmentController.unblockSlot(details, "app1");

        assertEquals(204, response.getStatusCode().value());
        verify(appointmentService).unblockSlot("u1", "app1");
    }
}
