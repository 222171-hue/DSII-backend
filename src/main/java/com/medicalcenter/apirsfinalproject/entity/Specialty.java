package com.medicalcenter.apirsfinalproject.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tspecialty")
@Getter
@Setter
@NoArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "idSpecialty", length = 36, nullable = false))
public class Specialty extends BaseCatalogEntity {
}
