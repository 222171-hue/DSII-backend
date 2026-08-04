package com.medicalcenter.apirsfinalproject.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final String ERROR_KEY = "error";
    private static final String REGISTRADO_MSG = "' ya se encuentra registrado.";

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> response = new HashMap<>();
        response.put(ERROR_KEY, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        Map<String, String> response = new HashMap<>();
        String message = ex.getMostSpecificCause().getMessage();
        if (message != null && message.contains("Duplicate entry")) {
            String duplicateValue = extractDuplicateValue(message);
            
            if (!duplicateValue.isEmpty()) {
                response.put(ERROR_KEY, determineDuplicateMessage(duplicateValue, message));
            } else {
                response.put(ERROR_KEY, "El dato ingresado ya se encuentra registrado en el sistema. Verifica que no haya duplicados.");
            }
        } else {
            response.put(ERROR_KEY, "Ocurrió un error de integridad de datos en la base de datos.");
        }
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoResourceFoundException(NoResourceFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put(ERROR_KEY, "Recurso no encontrado: " + ex.getResourcePath());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(Exception ex) {
        Map<String, String> response = new HashMap<>();
        response.put(ERROR_KEY, "Acceso denegado o token expirado. Por favor, inicie sesión nuevamente.");
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGlobalException(Exception ex) {
        Map<String, String> response = new HashMap<>();
        java.io.StringWriter sw = new java.io.StringWriter();
        ex.printStackTrace(new java.io.PrintWriter(sw));
        response.put(ERROR_KEY, "Ocurrió un error inesperado en el servidor: " + ex.getMessage() + "\nStack: " + sw.toString());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String extractDuplicateValue(String message) {
        try {
            int firstQuote = message.indexOf('\'');
            int secondQuote = message.indexOf('\'', firstQuote + 1);
            if (firstQuote != -1 && secondQuote != -1) {
                return message.substring(firstQuote + 1, secondQuote);
            }
        } catch (Exception e) {
            // Ignored, fallback to empty string
        }
        return "";
    }

    private String determineDuplicateMessage(String duplicateValue, String message) {
        if (duplicateValue.contains("@")) {
            return "El correo ya se encuentra registrado.";
        } else if (duplicateValue.matches("^\\\\d{8}$")) {
            return "El DNI '" + duplicateValue + REGISTRADO_MSG;
        } else if (message.toLowerCase().contains("tstudent")) {
            return "El código estudiantil '" + duplicateValue + REGISTRADO_MSG;
        } else {
            return "El valor '" + duplicateValue + REGISTRADO_MSG;
        }
    }
}
