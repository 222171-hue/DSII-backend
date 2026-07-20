package com.medicalcenter.apirsfinalproject.controller;

import com.medicalcenter.apirsfinalproject.dto.request.AppointmentRequest;
import com.medicalcenter.apirsfinalproject.entity.Appointment;
import com.medicalcenter.apirsfinalproject.entity.AppointmentStatus;
import com.medicalcenter.apirsfinalproject.security.CustomUserDetails;
import com.medicalcenter.apirsfinalproject.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Appointment> bookAppointment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AppointmentRequest request) {
        return new ResponseEntity<>(appointmentService.bookAppointment(userDetails.getUser().getId(), request), HttpStatus.CREATED);
    }

    @GetMapping("/student/pending")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Appointment>> getPendingAppointmentsForStudent(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(appointmentService.getPendingAppointmentsForStudent(userDetails.getUser().getId()));
    }

    @PatchMapping("/{id}/cancel-student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> cancelAppointmentByStudent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String id) {
        appointmentService.cancelAppointmentByStudent(userDetails.getUser().getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/specialist")
    @PreAuthorize("hasRole('SPECIALIST')")
    public ResponseEntity<List<Appointment>> getAppointmentsForSpecialist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForSpecialist(userDetails.getUser().getId(), start, end));
    }

    @GetMapping("/admin/specialist/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Appointment>> getAppointmentsForSpecialistByAdmin(
            @PathVariable String id,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForSpecialist(id, start, end));
    }

    @GetMapping("/specialist/{id}/occupied")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getOccupiedSlots(
            @PathVariable("id") String id,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(appointmentService.getOccupiedSlotsForSpecialist(id, start, end));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SPECIALIST')")
    public ResponseEntity<Void> changeAppointmentStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        AppointmentStatus status = AppointmentStatus.valueOf(body.get("status"));
        String cancelReason = body.get("cancelReason");
        appointmentService.changeAppointmentStatus(userDetails.getUser().getId(), id, status, cancelReason);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @PostMapping("/specialist/block")
    @PreAuthorize("hasRole('SPECIALIST')")
    public ResponseEntity<?> blockSlot(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, String> body) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(body.get("dateTime"));
            return ResponseEntity.ok(appointmentService.blockSlot(userDetails.getUser().getId(), dateTime));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.getClass().getName()));
        }
    }

    @DeleteMapping("/specialist/block/{id}")
    @PreAuthorize("hasRole('SPECIALIST')")
    public ResponseEntity<Void> unblockSlot(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String id) {
        appointmentService.unblockSlot(userDetails.getUser().getId(), id);
        return ResponseEntity.noContent().build();
    }
}
