package com.medicalcenter.apirsfinalproject.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleIllegalArgumentExceptionReturnsBadRequest() {
        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgumentException(new IllegalArgumentException("mensaje"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("mensaje", response.getBody().get("error"));
    }

    @Test
    void handleValidationExceptionsReturnsFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "nombre", "El nombre es obligatorio");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El nombre es obligatorio", response.getBody().get("nombre"));
    }

    @Test
    void handleDataIntegrityViolationForDuplicateEmail() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "Duplicate entry 'juan@unamba.edu.pe' for key 'tuser.email'",
                new DuplicateKeyException("Duplicate entry 'juan@unamba.edu.pe' for key 'tuser.email'"));

        ResponseEntity<Map<String, String>> response = handler.handleDataIntegrityViolationException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("El correo ya se encuentra registrado.", response.getBody().get("error"));
    }

    @Test
    void handleDataIntegrityViolationForDuplicateStudentCode() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "Duplicate entry '202000' for key 'tstudent.studentCode'",
                new DuplicateKeyException("Duplicate entry '202000' for key 'tstudent.studentCode'"));

        ResponseEntity<Map<String, String>> response = handler.handleDataIntegrityViolationException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().get("error").contains("código estudiantil"));
    }

    @Test
    void handleDataIntegrityViolationForGenericValue() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "Duplicate entry '123' for key 'tuser.dni'",
                new DuplicateKeyException("Duplicate entry '123' for key 'tuser.dni'"));

        ResponseEntity<Map<String, String>> response = handler.handleDataIntegrityViolationException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().get("error").contains("'123'"));
    }

    @Test
    void handleDataIntegrityViolationWithoutQuotes() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "Duplicate entry for key 'tuser.email'",
                new DuplicateKeyException("Duplicate entry for key 'tuser.email'"));

        ResponseEntity<Map<String, String>> response = handler.handleDataIntegrityViolationException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleDataIntegrityViolationForNonDuplicate() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "Referential integrity violation",
                new DuplicateKeyException("Referential integrity violation"));

        ResponseEntity<Map<String, String>> response = handler.handleDataIntegrityViolationException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleNoResourceFoundExceptionReturnsNotFound() {
        NoResourceFoundException ex = new NoResourceFoundException(org.springframework.http.HttpMethod.GET, "no encontrado", "/api/foo");

        ResponseEntity<Map<String, String>> response = handler.handleNoResourceFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().get("error").contains("/api/foo"));
    }

    @Test
    void handleAccessDeniedExceptionReturnsForbidden() {
        ResponseEntity<Map<String, String>> response =
                handler.handleAccessDeniedException(new AccessDeniedException("denegado"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleGlobalExceptionReturnsServerError() {
        ResponseEntity<Map<String, String>> response =
                handler.handleGlobalException(new RuntimeException("algo fallo"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().get("error").contains("algo fallo"));
    }
}
