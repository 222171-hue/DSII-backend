package com.medicalcenter.apirsfinalproject.controller;

import com.medicalcenter.apirsfinalproject.entity.Career;
import com.medicalcenter.apirsfinalproject.service.CareerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/careers")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "https://dsii-frontend-production.up.railway.app"})
@SuppressWarnings("java:S4684")
public class CareerController {
    // @SuppressWarnings("java:S4684")

    private final CareerService careerService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createCareer(@RequestBody Career career) {
        try {
            Career savedCareer = careerService.saveCareer(career);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCareer);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Career>> getAllCareers() {
        return ResponseEntity.ok(careerService.getAllCareers());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateCareer(@PathVariable String id, @RequestBody Career career) {
        try {
            Career updatedCareer = careerService.updateCareer(id, career);
            return ResponseEntity.ok(updatedCareer);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteCareer(@PathVariable String id) {
        try {
            careerService.deleteCareer(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    public record ErrorResponse(String error) {}
}
