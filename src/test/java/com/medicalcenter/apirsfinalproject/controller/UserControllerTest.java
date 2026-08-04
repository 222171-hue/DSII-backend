package com.medicalcenter.apirsfinalproject.controller;

import com.medicalcenter.apirsfinalproject.dto.request.UserRegistrationRequest;
import com.medicalcenter.apirsfinalproject.dto.request.UserUpdateRequest;
import com.medicalcenter.apirsfinalproject.entity.Role;
import com.medicalcenter.apirsfinalproject.entity.User;
import com.medicalcenter.apirsfinalproject.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void registerUserReturnsCreated() {
        User user = new User();
        user.setId("u1");
        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(user);

        ResponseEntity<User> response = userController.registerUser(new UserRegistrationRequest());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("u1", response.getBody().getId());
    }

    @Test
    void getUsersByRoleReturnsList() {
        when(userService.getUsersByRole(Role.STUDENT)).thenReturn(List.of(new User()));

        ResponseEntity<List<User>> response = userController.getUsersByRole(Role.STUDENT);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getSpecialistsBySpecialtyReturnsList() {
        when(userService.getSpecialistsBySpecialty("Medicina")).thenReturn(List.of(new User()));

        ResponseEntity<List<User>> response = userController.getSpecialistsBySpecialty("Medicina");

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getAllUsersReturnsList() {
        when(userService.getAllUsers()).thenReturn(List.of(new User(), new User()));

        ResponseEntity<List<User>> response = userController.getAllUsers();

        assertEquals(2, response.getBody().size());
    }

    @Test
    void updateUserReturnsUser() {
        User user = new User();
        when(userService.updateUser(eq("u1"), any(UserUpdateRequest.class))).thenReturn(user);

        ResponseEntity<User> response = userController.updateUser("u1", new UserUpdateRequest());

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void deleteUserReturnsNoContent() {
        ResponseEntity<Void> response = userController.deleteUser("u1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).deleteUser("u1");
    }

    @Test
    void updateProfilePictureReturnsOk() {
        ResponseEntity<Void> response = userController.updateProfilePicture("u1", Map.of("profilePicture", "base64"));

        assertEquals(200, response.getStatusCode().value());
        verify(userService).updateProfilePicture(eq("u1"), anyString());
    }
}
