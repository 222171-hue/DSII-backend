package com.medicalcenter.apirsfinalproject.service.impl;

import com.medicalcenter.apirsfinalproject.dto.request.UserRegistrationRequest;
import com.medicalcenter.apirsfinalproject.dto.request.UserUpdateRequest;
import com.medicalcenter.apirsfinalproject.entity.Role;
import com.medicalcenter.apirsfinalproject.entity.Specialist;
import com.medicalcenter.apirsfinalproject.entity.Specialty;
import com.medicalcenter.apirsfinalproject.entity.Student;
import com.medicalcenter.apirsfinalproject.entity.User;
import com.medicalcenter.apirsfinalproject.repository.SpecialistRepository;
import com.medicalcenter.apirsfinalproject.repository.SpecialtyRepository;
import com.medicalcenter.apirsfinalproject.repository.StudentRepository;
import com.medicalcenter.apirsfinalproject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private SpecialistRepository specialistRepository;
    @Mock
    private SpecialtyRepository specialtyRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegistrationRequest buildRequest(Role role, String correo) {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setNombre("Juan");
        request.setApellidos("Perez");
        request.setDni("12345678");
        request.setCorreo(correo);
        request.setCelular("987654321");
        request.setPassword("secret123");
        request.setRol(role);
        request.setCodigoEstudiantil("202000");
        request.setCarrera("Ingenieria");
        request.setEspecialidad("Medicina General");
        return request;
    }

    @Test
    void registerStudentSavesStudent() {
        UserRegistrationRequest request = buildRequest(Role.STUDENT, "juan@unamba.edu.pe");
        when(userRepository.findByCorreo(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByDni(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("encrypted");
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.registerUser(request);

        assertNotNull(saved);
        assertEquals(Role.STUDENT, saved.getRol());
        assertEquals("202000", ((Student) saved).getCodigoEstudiantil());
        verify(studentRepository).save(any(Student.class));
        verify(messagingTemplate).convertAndSend("/topic/users", "UPDATE");
    }

    @Test
    void registerStudentRejectsNonInstitutionalEmail() {
        UserRegistrationRequest request = buildRequest(Role.STUDENT, "juan@gmail.com");
        when(userRepository.findByCorreo(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByDni(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));
        assertEquals("Los estudiantes deben usar un correo institucional (@unamba.edu.pe)", ex.getMessage());
    }

    @Test
    void registerSpecialistSavesSpecialist() {
        UserRegistrationRequest request = buildRequest(Role.SPECIALIST, "doc@medico.com");
        Specialty specialty = new Specialty();
        specialty.setId("s1");
        specialty.setName("Medicina General");
        when(userRepository.findByCorreo(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByDni(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encrypted");
        when(specialtyRepository.findByName("Medicina General")).thenReturn(Optional.of(specialty));
        when(specialistRepository.save(any(Specialist.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.registerUser(request);

        assertNotNull(saved);
        assertEquals(Role.SPECIALIST, saved.getRol());
        assertEquals("Medicina General", ((Specialist) saved).getEspecialidad().getName());
        verify(specialistRepository).save(any(Specialist.class));
    }

    @Test
    void registerSpecialistThrowsWhenSpecialtyMissing() {
        UserRegistrationRequest request = buildRequest(Role.SPECIALIST, "doc@medico.com");
        when(userRepository.findByCorreo(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByDni(anyString())).thenReturn(Optional.empty());
        when(specialtyRepository.findByName("Medicina General")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));
        assertEquals("Especialidad no encontrada: Medicina General", ex.getMessage());
    }

    @Test
    void registerAdminSavesUser() {
        UserRegistrationRequest request = buildRequest(Role.ADMIN, "admin@unamba.edu.pe");
        when(userRepository.findByCorreo(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByDni(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encrypted");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.registerUser(request);

        assertNotNull(saved);
        assertEquals(Role.ADMIN, saved.getRol());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUserThrowsWhenEmailExists() {
        UserRegistrationRequest request = buildRequest(Role.ADMIN, "admin@unamba.edu.pe");
        when(userRepository.findByCorreo(anyString())).thenReturn(Optional.of(new User()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));
        assertEquals("El correo ya está en uso", ex.getMessage());
    }

    @Test
    void registerUserThrowsWhenDniExists() {
        UserRegistrationRequest request = buildRequest(Role.ADMIN, "admin@unamba.edu.pe");
        when(userRepository.findByCorreo(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByDni(anyString())).thenReturn(Optional.of(new User()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));
        assertEquals("El DNI ya está registrado", ex.getMessage());
    }

    @Test
    void getUsersByRoleStudentReturnsStudents() {
        Student student = new Student();
        student.setRol(Role.STUDENT);
        when(studentRepository.findAll()).thenReturn(List.of(student));

        List<User> result = userService.getUsersByRole(Role.STUDENT);

        assertEquals(1, result.size());
        assertEquals(Role.STUDENT, result.get(0).getRol());
    }

    @Test
    void getUsersByRoleSpecialistReturnsSpecialists() {
        Specialist specialist = new Specialist();
        specialist.setRol(Role.SPECIALIST);
        when(specialistRepository.findAll()).thenReturn(List.of(specialist));

        List<User> result = userService.getUsersByRole(Role.SPECIALIST);

        assertEquals(1, result.size());
        assertEquals(Role.SPECIALIST, result.get(0).getRol());
    }

    @Test
    void getUsersByRoleAdminReturnsFilteredUsers() {
        User admin = new User();
        admin.setRol(Role.ADMIN);
        User student = new User();
        student.setRol(Role.STUDENT);
        when(userRepository.findAll()).thenReturn(List.of(admin, student));

        List<User> result = userService.getUsersByRole(Role.ADMIN);

        assertEquals(1, result.size());
        assertEquals(Role.ADMIN, result.get(0).getRol());
    }

    @Test
    void getSpecialistsBySpecialtyReturnsMappedUsers() {
        Specialist specialist = new Specialist();
        specialist.setRol(Role.SPECIALIST);
        when(specialistRepository.findByEspecialidadName("Medicina")).thenReturn(List.of(specialist));

        List<User> result = userService.getSpecialistsBySpecialty("Medicina");

        assertEquals(1, result.size());
        assertEquals(Role.SPECIALIST, result.get(0).getRol());
    }

    @Test
    void getUserByIdReturnsUser() {
        User user = new User();
        user.setId("u1");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        assertEquals("u1", userService.getUserById("u1").getId());
    }

    @Test
    void getUserByIdThrowsWhenNotFound() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getUserById("u1"));
    }

    @Test
    void getAllUsersReturnsAll() {
        when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));

        assertEquals(2, userService.getAllUsers().size());
    }

    @Test
    void updateUserUpdatesCommonFields() {
        User user = new User();
        user.setId("u1");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("newpass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserUpdateRequest request = new UserUpdateRequest();
        request.setNombre("Nuevo");
        request.setApellidos("Nombre");
        request.setDni("87654321");
        request.setCorreo("nuevo@unamba.edu.pe");
        request.setCelular("999888777");
        request.setStatus("ACTIVO");
        request.setPassword("abc123");

        User updated = userService.updateUser("u1", request);

        assertEquals("Nuevo", updated.getNombre());
        assertEquals("newpass", updated.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void updateStudentUpdatesExtraFields() {
        Student student = new Student();
        student.setId("u1");
        when(userRepository.findById("u1")).thenReturn(Optional.of(student));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserUpdateRequest request = new UserUpdateRequest();
        request.setNombre("Nuevo");
        request.setApellidos("Nombre");
        request.setDni("87654321");
        request.setCorreo("nuevo@unamba.edu.pe");
        request.setCelular("999888777");
        request.setStatus("ACTIVO");
        request.setCodigoEstudiantil("202100");
        request.setCarrera("Derecho");

        userService.updateUser("u1", request);

        assertEquals("202100", student.getCodigoEstudiantil());
        assertEquals("Derecho", student.getCarrera());
    }

    @Test
    void updateSpecialistUpdatesEspecialidad() {
        Specialist specialist = new Specialist();
        specialist.setId("u1");
        Specialty newSpec = new Specialty();
        newSpec.setId("s9");
        newSpec.setName("Psicologia");
        when(userRepository.findById("u1")).thenReturn(Optional.of(specialist));
        when(specialtyRepository.findByName("Psicologia")).thenReturn(Optional.of(newSpec));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserUpdateRequest request = new UserUpdateRequest();
        request.setNombre("Nuevo");
        request.setApellidos("Nombre");
        request.setDni("87654321");
        request.setCorreo("nuevo@unamba.edu.pe");
        request.setCelular("999888777");
        request.setStatus("ACTIVO");
        request.setEspecialidad("Psicologia");

        userService.updateUser("u1", request);

        assertEquals("Psicologia", specialist.getEspecialidad().getName());
    }

    @Test
    void updateSpecialistThrowsWhenSpecialtyMissing() {
        Specialist specialist = new Specialist();
        specialist.setId("u1");
        when(userRepository.findById("u1")).thenReturn(Optional.of(specialist));
        when(specialtyRepository.findByName("NoExiste")).thenReturn(Optional.empty());

        UserUpdateRequest request = new UserUpdateRequest();
        request.setNombre("Nuevo");
        request.setApellidos("Nombre");
        request.setDni("87654321");
        request.setCorreo("nuevo@unamba.edu.pe");
        request.setCelular("999888777");
        request.setStatus("ACTIVO");
        request.setEspecialidad("NoExiste");

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser("u1", request));
    }

    @Test
    void updateUserWithoutPasswordKeepsOldPassword() {
        User user = new User();
        user.setId("u1");
        user.setPassword("original");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserUpdateRequest request = new UserUpdateRequest();
        request.setNombre("Nuevo");
        request.setApellidos("Nombre");
        request.setDni("87654321");
        request.setCorreo("nuevo@unamba.edu.pe");
        request.setCelular("999888777");
        request.setStatus("ACTIVO");

        User updated = userService.updateUser("u1", request);

        assertEquals("original", updated.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateProfilePictureSetsImage() {
        User user = new User();
        user.setId("u1");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        userService.updateProfilePicture("u1", "base64image");

        assertEquals("base64image", user.getProfilePicture());
        verify(userRepository).save(user);
    }

    @Test
    void deleteUserDeletesById() {
        userService.deleteUser("u1");

        verify(userRepository).deleteById("u1");
        verify(messagingTemplate).convertAndSend("/topic/users", "UPDATE");
    }
}
