package com.medicalcenter.apirsfinalproject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tuser")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "idUser", length = 36, nullable = false)
    private String id;

    @Column(name = "firstName", length = 70, nullable = false)
    private String nombre; // Mantenemos el nombre de campo en Java pero apuntamos a firstName

    @Column(name = "surName", length = 70, nullable = false)
    private String apellidos;

    @Column(name = "dni", length = 8, nullable = false, unique = true)
    private String dni;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String correo;

    @Column(name = "password", length = 255, nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;

    @Column(name = "phone", length = 15, nullable = false)
    private String celular;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role rol;

    @Column(name = "status", length = 20)
    private String status = "ACTIVO";

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;
}
