package com.medicalcenter.apirsfinalproject.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tstudent")
@PrimaryKeyJoinColumn(name = "idStudent")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Student extends User {

    @Column(name = "studentCode", length = 20, nullable = false, unique = true)
    private String codigoEstudiantil;

    @Column(name = "major", length = 100, nullable = false)
    private String carrera;
}
