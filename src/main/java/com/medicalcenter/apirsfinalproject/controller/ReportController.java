package com.medicalcenter.apirsfinalproject.controller;

import com.medicalcenter.apirsfinalproject.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.medicalcenter.apirsfinalproject.entity.Role;
import com.medicalcenter.apirsfinalproject.entity.Specialist;
import com.medicalcenter.apirsfinalproject.repository.SpecialistRepository;
import com.medicalcenter.apirsfinalproject.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final SpecialistRepository specialistRepository;

    @GetMapping("/appointments")
    @PreAuthorize("hasAnyRole('ADMIN', 'SPECIALIST')")
    public ResponseEntity<byte[]> getAppointmentsReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String specialty) {
        
        if (userDetails.getUser().getRol() == Role.SPECIALIST) {
            Specialist specialist = specialistRepository.findById(userDetails.getUser().getId()).orElse(null);
            if (specialist != null && specialist.getEspecialidad() != null) {
                specialty = specialist.getEspecialidad().getName();
            }
        }

        byte[] pdfBytes = reportService.generateAppointmentsReport(specialty);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "reporte_citas.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
