package com.medicalcenter.apirsfinalproject.service.impl;

import com.medicalcenter.apirsfinalproject.controller.SpecialtyController;
import com.medicalcenter.apirsfinalproject.entity.Specialist;
import com.medicalcenter.apirsfinalproject.entity.Specialty;
import com.medicalcenter.apirsfinalproject.repository.SpecialistRepository;
import com.medicalcenter.apirsfinalproject.repository.SpecialtyRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SpecialtyServiceImplTest {

    @Mock
    private SpecialtyRepository specialtyRepository;
    @Mock
    private SpecialistRepository specialistRepository;

    @InjectMocks
    private SpecialtyServiceImpl specialtyService;

    private Specialty buildSpecialty(String id, String name) {
        Specialty specialty = new Specialty();
        specialty.setId(id);
        specialty.setName(name);
        specialty.setDescription("Descripcion");
        return specialty;
    }

    @Test
    void getAllSpecialtiesReturnsAll() {
        when(specialtyRepository.findAll()).thenReturn(List.of(buildSpecialty("s1", "Medicina")));

        assertEquals(1, specialtyService.getAllSpecialties().size());
    }

    @Test
    void createSpecialtySuccess() {
        Specialty specialty = buildSpecialty(null, "Medicina");
        when(specialtyRepository.findByName("Medicina")).thenReturn(Optional.empty());
        when(specialtyRepository.save(any(Specialty.class))).thenAnswer(inv -> inv.getArgument(0));

        Specialty result = specialtyService.createSpecialty(specialty);

        assertNotNull(result.getId());
        verify(specialtyRepository).save(specialty);
    }

    @Test
    void createSpecialtyThrowsWhenExists() {
        Specialty specialty = buildSpecialty(null, "Medicina");
        when(specialtyRepository.findByName("Medicina")).thenReturn(Optional.of(buildSpecialty("s1", "Medicina")));

        assertThrows(IllegalArgumentException.class, () -> specialtyService.createSpecialty(specialty));
    }

    @Test
    void getSpecialtiesWithSpecialistsMapsDtos() {
        Specialist specialist = new Specialist();
        specialist.setId("sp1");
        specialist.setNombre("Doctor");
        specialist.setApellidos("Medico");
        specialist.setCorreo("doc@medico.com");
        Specialty specialty = buildSpecialty("s1", "Medicina");
        when(specialtyRepository.findAll()).thenReturn(List.of(specialty));
        when(specialistRepository.findByEspecialidadName("Medicina")).thenReturn(List.of(specialist));

        List<SpecialtyController.SpecialtyWithSpecialistsDto> result = specialtyService.getSpecialtiesWithSpecialists();

        assertEquals(1, result.size());
        assertEquals("Medicina", result.get(0).name());
        assertEquals(1, result.get(0).specialists().size());
        assertEquals("sp1", result.get(0).specialists().get(0).id());
    }

    @Test
    void updateSpecialtySuccess() {
        Specialty existing = buildSpecialty("s1", "Medicina");
        Specialty updated = buildSpecialty("s1", "Medicina General");
        when(specialtyRepository.findById("s1")).thenReturn(Optional.of(existing));
        when(specialtyRepository.save(any(Specialty.class))).thenAnswer(inv -> inv.getArgument(0));

        Specialty result = specialtyService.updateSpecialty("s1", updated);

        assertEquals("Medicina General", result.getName());
    }

    @Test
    void updateSpecialtyThrowsWhenNotFound() {
        when(specialtyRepository.findById("s1")).thenReturn(Optional.empty());

        Specialty specialty = buildSpecialty(null, "Medicina");
        assertThrows(IllegalArgumentException.class,
                () -> specialtyService.updateSpecialty("s1", specialty));
    }

    @Test
    void updateSpecialtyThrowsWhenNameConflict() {
        Specialty existing = buildSpecialty("s1", "Medicina");
        Specialty updated = buildSpecialty("s1", "Odontologia");
        when(specialtyRepository.findById("s1")).thenReturn(Optional.of(existing));
        when(specialtyRepository.findByName("Odontologia")).thenReturn(Optional.of(buildSpecialty("s2", "Odontologia")));

        assertThrows(IllegalArgumentException.class,
                () -> specialtyService.updateSpecialty("s1", updated));
    }

    @Test
    void deleteSpecialtySuccess() {
        Specialty existing = buildSpecialty("s1", "Medicina");
        when(specialtyRepository.findById("s1")).thenReturn(Optional.of(existing));
        when(specialistRepository.findByEspecialidadName("Medicina")).thenReturn(List.of());

        specialtyService.deleteSpecialty("s1");

        verify(specialtyRepository).deleteById("s1");
    }

    @Test
    void deleteSpecialtyThrowsWhenNotFound() {
        when(specialtyRepository.findById("s1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> specialtyService.deleteSpecialty("s1"));
    }

    @Test
    void deleteSpecialtyThrowsWhenHasSpecialists() {
        Specialty existing = buildSpecialty("s1", "Medicina");
        when(specialtyRepository.findById("s1")).thenReturn(Optional.of(existing));
        when(specialistRepository.findByEspecialidadName("Medicina")).thenReturn(List.of(new Specialist()));

        assertThrows(IllegalArgumentException.class, () -> specialtyService.deleteSpecialty("s1"));
    }
}
