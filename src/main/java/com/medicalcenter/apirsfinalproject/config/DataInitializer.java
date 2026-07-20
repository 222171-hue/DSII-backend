package com.medicalcenter.apirsfinalproject.config;

import com.medicalcenter.apirsfinalproject.entity.Role;
import com.medicalcenter.apirsfinalproject.entity.User;
import com.medicalcenter.apirsfinalproject.entity.Specialty;
import com.medicalcenter.apirsfinalproject.entity.Specialist;
import com.medicalcenter.apirsfinalproject.repository.SpecialistRepository;
import com.medicalcenter.apirsfinalproject.repository.SpecialtyRepository;
import com.medicalcenter.apirsfinalproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SpecialistRepository specialistRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (specialtyRepository.count() == 0) {
            List<String> defaultSpecialties = List.of("Medicina General", "Odontología", "Psicología");
            for (String name : defaultSpecialties) {
                Specialty s = new Specialty();
                s.setId(UUID.randomUUID().toString());
                s.setName(name);
                s.setDescription("Especialidad de " + name);
                specialtyRepository.save(s);
            }
            System.out.println("Especialidades por defecto creadas.");
        }

        // Crear un especialista por defecto para cada especialidad si no existe ninguno en esa especialidad
        List<Specialty> allSpecialties = specialtyRepository.findAll();
        int counter = 1;
        for (Specialty s : allSpecialties) {
            if (specialistRepository.findByEspecialidadName(s.getName()).isEmpty()) {
                Specialist doc = new Specialist();
                doc.setId(UUID.randomUUID().toString());
                doc.setNombre("Doctor");
                doc.setApellidos(s.getName());
                doc.setDni("1000000" + counter);
                
                String emailPrefix = s.getName().toLowerCase()
                    .replace(" ", "")
                    .replace("í", "i")
                    .replace("ó", "o");
                doc.setCorreo(emailPrefix + "@medico.com");
                doc.setCelular("98765432" + counter);
                doc.setPassword(passwordEncoder.encode("doctor123"));
                doc.setRol(Role.SPECIALIST);
                doc.setEspecialidad(s);
                specialistRepository.save(doc);
                System.out.println("Especialista creado: " + doc.getCorreo() + " / doctor123");
            }
            counter++;
        }

        if (userRepository.findByCorreo("admin@unamba.edu.pe").isEmpty()) {
            User admin = new User();
            admin.setId(UUID.randomUUID().toString());
            admin.setNombre("Super");
            admin.setApellidos("Admin");
            admin.setDni("00000000");
            admin.setCorreo("admin@unamba.edu.pe");
            admin.setCelular("999999999");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Administrador por defecto creado: admin@unamba.edu.pe / admin123");
        }
    }
}
