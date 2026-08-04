package com.medicalcenter.apirsfinalproject.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tcareer")
@Getter
@Setter
@NoArgsConstructor
@AttributeOverride(name = "id", column = @Column(name = "idCareer", length = 36, nullable = false))
public class Career extends BaseCatalogEntity {
}
