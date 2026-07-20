package com.medicalcenter.apirsfinalproject.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tspecialist")
@PrimaryKeyJoinColumn(name = "idSpecialist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Specialist extends User {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idSpecialty", nullable = false)
    private Specialty especialidad;
}
