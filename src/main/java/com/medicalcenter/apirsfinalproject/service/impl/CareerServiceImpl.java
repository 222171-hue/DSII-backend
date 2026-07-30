package com.medicalcenter.apirsfinalproject.service.impl;

import com.medicalcenter.apirsfinalproject.entity.Career;
import com.medicalcenter.apirsfinalproject.repository.CareerRepository;
import com.medicalcenter.apirsfinalproject.service.CareerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CareerServiceImpl implements CareerService {

    private final CareerRepository careerRepository;

    @Override
    public Career saveCareer(Career career) {
        if (careerRepository.existsByName(career.getName())) {
            throw new IllegalArgumentException("Ya existe una carrera con ese nombre");
        }
        return careerRepository.save(career);
    }

    @Override
    public List<Career> getAllCareers() {
        return careerRepository.findAll();
    }

    @Override
    public boolean existsByName(String name) {
        return careerRepository.existsByName(name);
    }

    @Override
    public Career updateCareer(String id, Career career) {
        Career existing = careerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carrera no encontrada"));
        
        if (!existing.getName().equalsIgnoreCase(career.getName()) && careerRepository.existsByName(career.getName())) {
            throw new IllegalArgumentException("Ya existe otra carrera con ese nombre");
        }
        
        existing.setName(career.getName());
        existing.setDescription(career.getDescription());
        return careerRepository.save(existing);
    }

    @Override
    public void deleteCareer(String id) {
        if (!careerRepository.existsById(id)) {
            throw new IllegalArgumentException("Carrera no encontrada");
        }
        careerRepository.deleteById(id);
    }
}
