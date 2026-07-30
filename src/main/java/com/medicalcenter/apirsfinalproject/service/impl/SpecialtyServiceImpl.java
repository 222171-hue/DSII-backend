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
    private final com.medicalcenter.apirsfinalproject.repository.SpecialistRepository specialistRepository;

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

    @Override
    public List<com.medicalcenter.apirsfinalproject.controller.SpecialtyController.SpecialtyWithSpecialistsDto> getSpecialtiesWithSpecialists() {
        return specialtyRepository.findAll().stream().map(specialty -> {
            var specialists = specialistRepository.findByEspecialidadName(specialty.getName()).stream().map(s -> 
                new com.medicalcenter.apirsfinalproject.controller.SpecialtyController.SpecialistDto(
                    s.getId(), s.getNombre(), s.getApellidos(), s.getCorreo()
                )
            ).toList();
            
            return new com.medicalcenter.apirsfinalproject.controller.SpecialtyController.SpecialtyWithSpecialistsDto(
                specialty.getId(),
                specialty.getName(),
                specialty.getDescription(),
                specialists
            );
        }).toList();
    }

    @Override
    public Specialty updateSpecialty(String id, Specialty specialty) {
        Specialty existing = specialtyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada"));
        
        if (!existing.getName().equalsIgnoreCase(specialty.getName()) && specialtyRepository.findByName(specialty.getName()).isPresent()) {
            throw new IllegalArgumentException("Ya existe otra especialidad con ese nombre");
        }
        
        existing.setName(specialty.getName());
        existing.setDescription(specialty.getDescription());
        return specialtyRepository.save(existing);
    }

    @Override
    public void deleteSpecialty(String id) {
        Specialty existing = specialtyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada"));
                
        if (!specialistRepository.findByEspecialidadName(existing.getName()).isEmpty()) {
            throw new IllegalArgumentException("No se puede eliminar la especialidad porque tiene especialistas asignados");
        }
        
        specialtyRepository.deleteById(id);
    }
}
