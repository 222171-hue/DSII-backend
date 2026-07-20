package com.medicalcenter.apirsfinalproject.repository;

import com.medicalcenter.apirsfinalproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByCorreo(String correo);
    Optional<User> findByDni(String dni);
}
