package com.medicalcenter.apirsfinalproject.service.impl;

import com.medicalcenter.apirsfinalproject.entity.Career;
import com.medicalcenter.apirsfinalproject.repository.CareerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CareerServiceImplTest {

    @Mock
    private CareerRepository careerRepository;

    @InjectMocks
    private CareerServiceImpl careerService;

    private Career buildCareer(String id, String name) {
        Career career = new Career();
        career.setId(id);
        career.setName(name);
        career.setDescription("Descripcion");
        return career;
    }

    @Test
    void saveCareerSuccess() {
        Career career = buildCareer(null, "Ingenieria");
        when(careerRepository.existsByName("Ingenieria")).thenReturn(false);
        when(careerRepository.save(any(Career.class))).thenAnswer(inv -> inv.getArgument(0));

        Career result = careerService.saveCareer(career);

        assertEquals("Ingenieria", result.getName());
        verify(careerRepository).save(career);
    }

    @Test
    void saveCareerThrowsWhenExists() {
        Career career = buildCareer(null, "Ingenieria");
        when(careerRepository.existsByName("Ingenieria")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> careerService.saveCareer(career));
    }

    @Test
    void getAllCareersReturnsAll() {
        when(careerRepository.findAll()).thenReturn(List.of(buildCareer("c1", "Derecho")));

        assertEquals(1, careerService.getAllCareers().size());
    }

    @Test
    void existsByNameDelegates() {
        when(careerRepository.existsByName("Derecho")).thenReturn(true);

        assertTrue(careerService.existsByName("Derecho"));
        assertFalse(careerService.existsByName("Otro"));
    }

    @Test
    void updateCareerSuccess() {
        Career existing = buildCareer("c1", "Ingenieria");
        Career updated = buildCareer("c1", "Ingenieria de Software");
        when(careerRepository.findById("c1")).thenReturn(Optional.of(existing));
        when(careerRepository.save(any(Career.class))).thenAnswer(inv -> inv.getArgument(0));

        Career result = careerService.updateCareer("c1", updated);

        assertEquals("Ingenieria de Software", result.getName());
    }

    @Test
    void updateCareerThrowsWhenNotFound() {
        when(careerRepository.findById("c1")).thenReturn(Optional.empty());

        Career career = buildCareer(null, "Derecho");
        assertThrows(RuntimeException.class,
                () -> careerService.updateCareer("c1", career));
    }

    @Test
    void updateCareerThrowsWhenNameConflict() {
        Career existing = buildCareer("c1", "Ingenieria");
        Career updated = buildCareer("c1", "Derecho");
        when(careerRepository.findById("c1")).thenReturn(Optional.of(existing));
        when(careerRepository.existsByName("Derecho")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> careerService.updateCareer("c1", updated));
    }

    @Test
    void deleteCareerSuccess() {
        when(careerRepository.existsById("c1")).thenReturn(true);

        careerService.deleteCareer("c1");

        verify(careerRepository).deleteById("c1");
    }

    @Test
    void deleteCareerThrowsWhenNotFound() {
        when(careerRepository.existsById("c1")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> careerService.deleteCareer("c1"));
    }
}
