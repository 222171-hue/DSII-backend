package com.medicalcenter.apirsfinalproject.controller;

import com.medicalcenter.apirsfinalproject.entity.Specialty;
import com.medicalcenter.apirsfinalproject.service.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specialties")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://dsii-frontend-production.up.railway.app"})
@SuppressWarnings("java:S4684")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    @GetMapping
    public ResponseEntity<List<Specialty>> getAllSpecialties() {
        return ResponseEntity.ok(specialtyService.getAllSpecialties());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createSpecialty(@RequestBody Specialty specialty) {
        try {
            Specialty created = specialtyService.createSpecialty(specialty);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/with-specialists")
    public ResponseEntity<List<SpecialtyWithSpecialistsDto>> getSpecialtiesWithSpecialists() {
        return ResponseEntity.ok(specialtyService.getSpecialtiesWithSpecialists());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateSpecialty(@PathVariable String id, @RequestBody Specialty specialty) {
        try {
            Specialty updatedSpecialty = specialtyService.updateSpecialty(id, specialty);
            return ResponseEntity.ok(updatedSpecialty);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteSpecialty(@PathVariable String id) {
        try {
            specialtyService.deleteSpecialty(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    public record ErrorResponse(String error) {}
    public record SpecialistDto(String id, String nombre, String apellidos, String correo) {}
    public record SpecialtyWithSpecialistsDto(String id, String name, String description, List<SpecialistDto> specialists) {}
}
