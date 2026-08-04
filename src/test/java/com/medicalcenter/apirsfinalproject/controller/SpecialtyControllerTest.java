package com.medicalcenter.apirsfinalproject.controller;

import com.medicalcenter.apirsfinalproject.entity.Specialty;
import com.medicalcenter.apirsfinalproject.service.SpecialtyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpecialtyControllerTest {

    @Mock
    private SpecialtyService specialtyService;

    @InjectMocks
    private SpecialtyController specialtyController;

    @Test
    void getAllSpecialtiesReturnsList() {
        when(specialtyService.getAllSpecialties()).thenReturn(List.of(new Specialty()));

        ResponseEntity<List<Specialty>> response = specialtyController.getAllSpecialties();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void createSpecialtyReturnsOk() {
        when(specialtyService.createSpecialty(any(Specialty.class))).thenReturn(new Specialty());

        ResponseEntity<Object> response = specialtyController.createSpecialty(new Specialty());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void createSpecialtyReturnsBadRequestOnError() {
        when(specialtyService.createSpecialty(any(Specialty.class))).thenThrow(new IllegalArgumentException("Ya existe"));

        ResponseEntity<Object> response = specialtyController.createSpecialty(new Specialty());

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void getSpecialtiesWithSpecialistsReturnsList() {
        when(specialtyService.getSpecialtiesWithSpecialists()).thenReturn(List.of());

        ResponseEntity<List<SpecialtyController.SpecialtyWithSpecialistsDto>> response =
                specialtyController.getSpecialtiesWithSpecialists();

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void updateSpecialtyReturnsOk() {
        when(specialtyService.updateSpecialty(eq("s1"), any(Specialty.class))).thenReturn(new Specialty());

        ResponseEntity<Object> response = specialtyController.updateSpecialty("s1", new Specialty());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void updateSpecialtyReturnsBadRequestOnError() {
        when(specialtyService.updateSpecialty(eq("s1"), any(Specialty.class)))
                .thenThrow(new IllegalArgumentException("No encontrada"));

        ResponseEntity<Object> response = specialtyController.updateSpecialty("s1", new Specialty());

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void deleteSpecialtyReturnsOk() {
        ResponseEntity<Object> response = specialtyController.deleteSpecialty("s1");

        assertEquals(200, response.getStatusCode().value());
        verify(specialtyService).deleteSpecialty("s1");
    }

    @Test
    void deleteSpecialtyReturnsBadRequestOnError() {
        doThrow(new IllegalArgumentException("No encontrada")).when(specialtyService).deleteSpecialty("s1");

        ResponseEntity<Object> response = specialtyController.deleteSpecialty("s1");

        assertEquals(400, response.getStatusCode().value());
    }
}
