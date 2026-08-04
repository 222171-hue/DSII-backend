package com.medicalcenter.apirsfinalproject.controller;

import com.medicalcenter.apirsfinalproject.entity.Career;
import com.medicalcenter.apirsfinalproject.service.CareerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CareerControllerTest {

    @Mock
    private CareerService careerService;

    @InjectMocks
    private CareerController careerController;

    @Test
    void createCareerReturnsCreated() {
        when(careerService.saveCareer(any(Career.class))).thenReturn(new Career());

        ResponseEntity<Object> response = careerController.createCareer(new Career());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertInstanceOf(Career.class, response.getBody());
    }

    @Test
    void createCareerReturnsBadRequestOnError() {
        when(careerService.saveCareer(any(Career.class))).thenThrow(new IllegalArgumentException("Ya existe"));

        ResponseEntity<Object> response = careerController.createCareer(new Career());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Ya existe", ((CareerController.ErrorResponse) response.getBody()).error());
    }

    @Test
    void getAllCareersReturnsList() {
        when(careerService.getAllCareers()).thenReturn(List.of(new Career()));

        ResponseEntity<List<Career>> response = careerController.getAllCareers();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void updateCareerReturnsOk() {
        when(careerService.updateCareer(eq("c1"), any(Career.class))).thenReturn(new Career());

        ResponseEntity<Object> response = careerController.updateCareer("c1", new Career());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void updateCareerReturnsBadRequestOnError() {
        when(careerService.updateCareer(eq("c1"), any(Career.class))).thenThrow(new IllegalArgumentException("No encontrada"));

        ResponseEntity<Object> response = careerController.updateCareer("c1", new Career());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deleteCareerReturnsOk() {
        ResponseEntity<Object> response = careerController.deleteCareer("c1");

        assertEquals(200, response.getStatusCode().value());
        verify(careerService).deleteCareer("c1");
    }

    @Test
    void deleteCareerReturnsBadRequestOnError() {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("No encontrada"))
                .when(careerService).deleteCareer("c1");

        ResponseEntity<Object> response = careerController.deleteCareer("c1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
