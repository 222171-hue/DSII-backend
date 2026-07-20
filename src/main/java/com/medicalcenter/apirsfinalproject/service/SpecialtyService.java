package com.medicalcenter.apirsfinalproject.service;

import com.medicalcenter.apirsfinalproject.entity.Specialty;
import java.util.List;

public interface SpecialtyService {
    List<Specialty> getAllSpecialties();
    Specialty createSpecialty(Specialty specialty);
}
