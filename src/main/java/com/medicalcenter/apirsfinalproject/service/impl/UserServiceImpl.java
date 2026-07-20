package com.medicalcenter.apirsfinalproject.service.impl;

import com.medicalcenter.apirsfinalproject.dto.request.UserRegistrationRequest;
import com.medicalcenter.apirsfinalproject.entity.Role;
import com.medicalcenter.apirsfinalproject.entity.User;
import com.medicalcenter.apirsfinalproject.entity.Student;
import com.medicalcenter.apirsfinalproject.entity.Specialist;
import com.medicalcenter.apirsfinalproject.entity.Specialty;
import com.medicalcenter.apirsfinalproject.repository.UserRepository;
import com.medicalcenter.apirsfinalproject.repository.StudentRepository;
import com.medicalcenter.apirsfinalproject.repository.SpecialistRepository;
import com.medicalcenter.apirsfinalproject.repository.SpecialtyRepository;
import com.medicalcenter.apirsfinalproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SpecialistRepository specialistRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(UserRegistrationRequest request) {
        if (userRepository.findByCorreo(request.getCorreo()).isPresent()) {
            throw new IllegalArgumentException("El correo ya está en uso");
        }
        if (userRepository.findByDni(request.getDni()).isPresent()) {
            throw new IllegalArgumentException("El DNI ya está registrado");
        }

        if (request.getRol() == Role.STUDENT && !request.getCorreo().endsWith("@unamba.edu.pe")) {
            throw new IllegalArgumentException("Los estudiantes deben usar un correo institucional (@unamba.edu.pe)");
        }

        String newId = UUID.randomUUID().toString();

        if (request.getRol() == Role.STUDENT) {
            Student student = new Student();
            student.setId(newId);
            student.setNombre(request.getNombre());
            student.setApellidos(request.getApellidos());
            student.setDni(request.getDni());
            student.setCorreo(request.getCorreo());
            student.setCelular(request.getCelular());
            student.setRol(request.getRol());
            student.setPassword(passwordEncoder.encode(request.getPassword()));
            student.setCodigoEstudiantil(request.getCodigoEstudiantil());
            student.setCarrera(request.getCarrera());
            return studentRepository.save(student);
            
        } else if (request.getRol() == Role.SPECIALIST) {
            Specialist specialist = new Specialist();
            specialist.setId(newId);
            specialist.setNombre(request.getNombre());
            specialist.setApellidos(request.getApellidos());
            specialist.setDni(request.getDni());
            specialist.setCorreo(request.getCorreo());
            specialist.setCelular(request.getCelular());
            specialist.setRol(request.getRol());
            specialist.setPassword(passwordEncoder.encode(request.getPassword()));
            
            Specialty specialty = specialtyRepository.findByName(request.getEspecialidad())
                .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada: " + request.getEspecialidad()));
            specialist.setEspecialidad(specialty);
            
            return specialistRepository.save(specialist);
            
        } else {
            // ADMIN
            User user = new User();
            user.setId(newId);
            user.setNombre(request.getNombre());
            user.setApellidos(request.getApellidos());
            user.setDni(request.getDni());
            user.setCorreo(request.getCorreo());
            user.setCelular(request.getCelular());
            user.setRol(request.getRol());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            return userRepository.save(user);
        }
    }

    @Override
    public List<User> getUsersByRole(Role role) {
        if (role == Role.STUDENT) {
            return studentRepository.findAll().stream().map(s -> (User) s).collect(Collectors.toList());
        } else if (role == Role.SPECIALIST) {
            return specialistRepository.findAll().stream().map(s -> (User) s).collect(Collectors.toList());
        }
        return userRepository.findAll().stream()
                .filter(u -> u.getRol() == role)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getSpecialistsBySpecialty(String specialtyName) {
        return specialistRepository.findByEspecialidadName(specialtyName).stream()
                .map(s -> (User) s)
                .collect(Collectors.toList());
    }

    @Override
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(String id, com.medicalcenter.apirsfinalproject.dto.request.UserUpdateRequest request) {
        User user = getUserById(id);
        user.setNombre(request.getNombre());
        user.setApellidos(request.getApellidos());
        user.setDni(request.getDni());
        user.setCorreo(request.getCorreo());
        user.setCelular(request.getCelular());
        user.setStatus(request.getStatus());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (user instanceof com.medicalcenter.apirsfinalproject.entity.Student student) {
            if (request.getCodigoEstudiantil() != null && !request.getCodigoEstudiantil().isBlank()) {
                student.setCodigoEstudiantil(request.getCodigoEstudiantil());
            }
            if (request.getCarrera() != null && !request.getCarrera().isBlank()) {
                student.setCarrera(request.getCarrera());
            }
        } else if (user instanceof com.medicalcenter.apirsfinalproject.entity.Specialist specialist) {
            if (request.getEspecialidad() != null && !request.getEspecialidad().isBlank()) {
                com.medicalcenter.apirsfinalproject.entity.Specialty spec = specialtyRepository.findByName(request.getEspecialidad())
                        .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada"));
                specialist.setEspecialidad(spec);
            }
        }

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}
