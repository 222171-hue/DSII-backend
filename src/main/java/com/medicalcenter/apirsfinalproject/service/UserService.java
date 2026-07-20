package com.medicalcenter.apirsfinalproject.service;

import com.medicalcenter.apirsfinalproject.dto.request.UserRegistrationRequest;
import com.medicalcenter.apirsfinalproject.entity.Role;
import com.medicalcenter.apirsfinalproject.entity.User;

import java.util.List;

public interface UserService {
    User registerUser(UserRegistrationRequest request);
    List<User> getUsersByRole(Role role);
    List<User> getSpecialistsBySpecialty(String specialtyName);
    User getUserById(String id);
    List<User> getAllUsers();
    User updateUser(String id, com.medicalcenter.apirsfinalproject.dto.request.UserUpdateRequest request);
    void deleteUser(String id);
}
