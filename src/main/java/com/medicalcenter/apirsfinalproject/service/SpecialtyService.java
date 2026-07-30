package com.medicalcenter.apirsfinalproject.service;

import com.medicalcenter.apirsfinalproject.entity.Specialty;
import java.util.List;

public interface SpecialtyService {
    List<Specialty> getAllSpecialties();
    Specialty createSpecialty(Specialty specialty);
    List<com.medicalcenter.apirsfinalproject.controller.SpecialtyController.SpecialtyWithSpecialistsDto> getSpecialtiesWithSpecialists();
    Specialty updateSpecialty(String id, Specialty specialty);
    void deleteSpecialty(String id);
}
