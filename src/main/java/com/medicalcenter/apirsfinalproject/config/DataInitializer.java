package com.medicalcenter.apirsfinalproject.config;

import com.medicalcenter.apirsfinalproject.entity.Role;
import com.medicalcenter.apirsfinalproject.entity.User;
import com.medicalcenter.apirsfinalproject.entity.Specialty;
import com.medicalcenter.apirsfinalproject.entity.Specialist;
import com.medicalcenter.apirsfinalproject.repository.SpecialistRepository;
import com.medicalcenter.apirsfinalproject.repository.SpecialtyRepository;
import com.medicalcenter.apirsfinalproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor

@SuppressWarnings("java:S6437")
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

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
            logger.info("Especialidades por defecto creadas.");
        }

        // Crear un especialista por defecto solo para las especialidades por defecto si no existe ninguno
        List<String> defaultSpecialties = List.of("Medicina General", "Odontología", "Psicología");
                for (String name : defaultSpecialties) {
            specialtyRepository.findByName(name).ifPresent(s -> {
                if (specialistRepository.findByEspecialidadName(s.getName()).isEmpty()) {
                    Specialist doc = new Specialist();
                    doc.setId(UUID.randomUUID().toString());
                    doc.setNombre("Doctor");
                    doc.setApellidos(s.getName());
                    
                    // Asegurar que el DNI sea único usando un hash corto o el UUID
                    String uniqueSuffix = UUID.randomUUID().toString().substring(0, 4);
                    doc.setDni("1000" + uniqueSuffix);
                    
                    String emailPrefix = s.getName().toLowerCase()
                        .replace(" ", "")
                        .replace("í", "i")
                        .replace("ó", "o");
                    doc.setCorreo(emailPrefix + "_" + uniqueSuffix + "@medico.com");
                    doc.setCelular("9876" + uniqueSuffix);
                    doc.setPassword(passwordEncoder.encode("doctor123" /* nosonar */));
                    doc.setRol(Role.SPECIALIST);
                    doc.setEspecialidad(s);
                    specialistRepository.save(doc);
                    logger.info("Especialista creado: " + doc.getCorreo() + " / doctor123");
                }
            });
            }

        if (userRepository.findByCorreo("admin@unamba.edu.pe").isEmpty()) {
            User admin = new User();
            admin.setId(UUID.randomUUID().toString());
            admin.setNombre("Super");
            admin.setApellidos("Admin");
            admin.setDni("00000000");
            admin.setCorreo("admin@unamba.edu.pe");
            admin.setCelular("999999999");
            admin.setPassword(passwordEncoder.encode("admin123" /* nosonar */));
            admin.setRol(Role.ADMIN);
            userRepository.save(admin);
            logger.info("Administrador por defecto creado: admin@unamba.edu.pe / admin123");
        }
    }
}
