package com.medicalcenter.apirsfinalproject.service.impl;

import com.medicalcenter.apirsfinalproject.entity.Specialty;
import com.medicalcenter.apirsfinalproject.repository.SpecialtyRepository;
import com.medicalcenter.apirsfinalproject.service.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    @Override
    public List<Specialty> getAllSpecialties() {
        return specialtyRepository.findAll();
    }

    @Override
    public Specialty createSpecialty(Specialty specialty) {
        if (specialtyRepository.findByName(specialty.getName()).isPresent()) {
            throw new IllegalArgumentException("La especialidad ya existe: " + specialty.getName());
        }
        specialty.setId(UUID.randomUUID().toString());
        return specialtyRepository.save(specialty);
    }
}
