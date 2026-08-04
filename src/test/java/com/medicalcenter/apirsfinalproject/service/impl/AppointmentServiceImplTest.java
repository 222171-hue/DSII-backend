package com.medicalcenter.apirsfinalproject.service.impl;

import com.medicalcenter.apirsfinalproject.dto.request.AppointmentRequest;
import com.medicalcenter.apirsfinalproject.entity.Appointment;
import com.medicalcenter.apirsfinalproject.entity.AppointmentStatus;
import com.medicalcenter.apirsfinalproject.entity.Role;
import com.medicalcenter.apirsfinalproject.entity.Specialist;
import com.medicalcenter.apirsfinalproject.entity.Specialty;
import com.medicalcenter.apirsfinalproject.entity.Student;
import com.medicalcenter.apirsfinalproject.entity.User;
import com.medicalcenter.apirsfinalproject.repository.AppointmentRepository;
import com.medicalcenter.apirsfinalproject.repository.SpecialistRepository;
import com.medicalcenter.apirsfinalproject.repository.StudentRepository;
import com.medicalcenter.apirsfinalproject.repository.UserRepository;
import com.medicalcenter.apirsfinalproject.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private SpecialistRepository specialistRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private NotificationService notificationService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Student buildStudent() {
        Student student = new Student();
        student.setId("student1");
        student.setNombre("Juan");
        student.setApellidos("Perez");
        student.setRol(Role.STUDENT);
        return student;
    }

    private Specialist buildSpecialist(String specialtyName) {
        Specialty specialty = new Specialty();
        specialty.setId("spec-1");
        specialty.setName(specialtyName);
        Specialist specialist = new Specialist();
        specialist.setId("specialist1");
        specialist.setNombre("Doctor");
        specialist.setApellidos("Medico");
        specialist.setEspecialidad(specialty);
        specialist.setRol(Role.SPECIALIST);
        return specialist;
    }

    private Appointment buildAppointment(String id, Specialist specialist, Student student, AppointmentStatus status) {
        return Appointment.builder()
                .id(id)
                .specialist(specialist)
                .student(student)
                .appointmentDate(java.time.LocalDate.of(2026, 8, 10))
                .startTime(java.time.LocalTime.of(10, 0))
                .endTime(java.time.LocalTime.of(10, 30))
                .status(status)
                .build();
    }

    private AppointmentRequest buildRequest(String specialtyName) {
        AppointmentRequest request = new AppointmentRequest();
        request.setSpecialistId("specialist1");
        request.setSpecialty(specialtyName);
        request.setDateTime(LocalDateTime.of(2026, 8, 10, 10, 0));
        request.setReason("Dolor de cabeza");
        return request;
    }

    @Test
    void bookAppointmentSuccess() {
        Student student = buildStudent();
        Specialist specialist = buildSpecialist("Medicina General");
        when(studentRepository.findById("student1")).thenReturn(Optional.of(student));
        when(specialistRepository.findById("specialist1")).thenReturn(Optional.of(specialist));
        when(appointmentRepository.existsBySpecialtyNameAndAppointmentDateAndStartTimeAndStatusNot(anyString(), any(), any(), any()))
                .thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findByRol(Role.SPECIALIST)).thenReturn(List.of(specialist));
        when(userRepository.findByRol(Role.ADMIN)).thenReturn(List.of(buildAdmin()));

        Appointment result = appointmentService.bookAppointment("student1", buildRequest("Medicina General"));

        assertNotNull(result);
        assertEquals(AppointmentStatus.PENDIENTE, result.getStatus());
        assertEquals("specialist1", result.getSpecialist().getId());
        verify(notificationService).createNotification(eq("specialist1"), anyString());
        verify(notificationService).createNotification(eq("admin1"), anyString());
        verify(messagingTemplate).convertAndSend("/topic/appointments", "UPDATE");
    }

    private User buildAdmin() {
        User admin = new User();
        admin.setId("admin1");
        admin.setRol(Role.ADMIN);
        return admin;
    }

    @Test
    void bookAppointmentThrowsWhenStudentMissing() {
        when(studentRepository.findById("student1")).thenReturn(Optional.empty());

        AppointmentRequest request = buildRequest("Medicina General");
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.bookAppointment("student1", request));
    }

    @Test
    void bookAppointmentThrowsWhenSpecialistMissing() {
        when(studentRepository.findById("student1")).thenReturn(Optional.of(buildStudent()));
        when(specialistRepository.findById("specialist1")).thenReturn(Optional.empty());

        AppointmentRequest request = buildRequest("Medicina General");
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.bookAppointment("student1", request));
    }

    @Test
    void bookAppointmentThrowsWhenSpecialtyMismatch() {
        Student student = buildStudent();
        Specialist specialist = buildSpecialist("Odontologia");
        when(studentRepository.findById("student1")).thenReturn(Optional.of(student));
        when(specialistRepository.findById("specialist1")).thenReturn(Optional.of(specialist));

        AppointmentRequest request = buildRequest("Medicina General");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.bookAppointment("student1", request));
        assertEquals("El especialista no pertenece a esa especialidad", ex.getMessage());
    }

    @Test
    void bookAppointmentThrowsWhenSlotOccupied() {
        Student student = buildStudent();
        Specialist specialist = buildSpecialist("Medicina General");
        when(studentRepository.findById("student1")).thenReturn(Optional.of(student));
        when(specialistRepository.findById("specialist1")).thenReturn(Optional.of(specialist));
        when(appointmentRepository.existsBySpecialtyNameAndAppointmentDateAndStartTimeAndStatusNot(anyString(), any(), any(), any()))
                .thenReturn(true);

        AppointmentRequest request = buildRequest("Medicina General");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> appointmentService.bookAppointment("student1", request));
        assertEquals("El horario ya está ocupado", ex.getMessage());
    }

    @Test
    void cancelAppointmentByStudentSuccess() {
        Student student = buildStudent();
        Specialist specialist = buildSpecialist("Medicina General");
        Appointment appointment = buildAppointment("app1", specialist, student, AppointmentStatus.PENDIENTE);
        when(appointmentRepository.findById("app1")).thenReturn(Optional.of(appointment));

        appointmentService.cancelAppointmentByStudent("student1", "app1");

        assertEquals(AppointmentStatus.CANCELADO_POR_ESTUDIANTE, appointment.getStatus());
        verify(appointmentRepository).save(appointment);
        verify(messagingTemplate).convertAndSend("/topic/appointments", "UPDATE");
    }

    @Test
    void cancelAppointmentByStudentThrowsWhenNotFound() {
        when(appointmentRepository.findById("app1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.cancelAppointmentByStudent("student1", "app1"));
    }

    @Test
    void cancelAppointmentByStudentThrowsWhenNotAuthorized() {
        Student other = new Student();
        other.setId("other");
        Appointment appointment = buildAppointment("app1", buildSpecialist("Medicina General"), other, AppointmentStatus.PENDIENTE);
        when(appointmentRepository.findById("app1")).thenReturn(Optional.of(appointment));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.cancelAppointmentByStudent("student1", "app1"));
    }

    @Test
    void cancelAppointmentByStudentThrowsWhenNotPending() {
        Appointment appointment = buildAppointment("app1", buildSpecialist("Medicina General"), buildStudent(), AppointmentStatus.ATENDIDO);
        when(appointmentRepository.findById("app1")).thenReturn(Optional.of(appointment));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.cancelAppointmentByStudent("student1", "app1"));
    }

    @Test
    void changeAppointmentStatusSuccess() {
        Student student = buildStudent();
        Specialist specialist = buildSpecialist("Medicina General");
        Appointment appointment = buildAppointment("app1", specialist, student, AppointmentStatus.PENDIENTE);
        when(appointmentRepository.findById("app1")).thenReturn(Optional.of(appointment));
        when(specialistRepository.findById("specialist1")).thenReturn(Optional.of(specialist));

        appointmentService.changeAppointmentStatus("specialist1", "app1", AppointmentStatus.ATENDIDO, null);

        assertEquals(AppointmentStatus.ATENDIDO, appointment.getStatus());
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void changeAppointmentStatusCancelsWithReasonAndNotification() {
        Student student = buildStudent();
        Specialist specialist = buildSpecialist("Medicina General");
        Appointment appointment = buildAppointment("app1", specialist, student, AppointmentStatus.PENDIENTE);
        when(appointmentRepository.findById("app1")).thenReturn(Optional.of(appointment));
        when(specialistRepository.findById("specialist1")).thenReturn(Optional.of(specialist));

        appointmentService.changeAppointmentStatus("specialist1", "app1",
                AppointmentStatus.CANCELADO_POR_ESPECIALISTA, "No atiende los jueves");

        assertEquals(AppointmentStatus.CANCELADO_POR_ESPECIALISTA, appointment.getStatus());
        assertEquals("No atiende los jueves", appointment.getCancelReason());
        verify(notificationService).createNotification(eq("student1"), anyString());
    }

    @Test
    void changeAppointmentStatusThrowsWhenAppointmentMissing() {
        when(appointmentRepository.findById("app1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.changeAppointmentStatus("specialist1", "app1", AppointmentStatus.ATENDIDO, null));
    }

    @Test
    void changeAppointmentStatusThrowsWhenSpecialistMissing() {
        Specialist specialist = buildSpecialist("Medicina General");
        Appointment appointment = buildAppointment("app1", specialist, buildStudent(), AppointmentStatus.PENDIENTE);
        when(appointmentRepository.findById("app1")).thenReturn(Optional.of(appointment));
        when(specialistRepository.findById("specialist1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.changeAppointmentStatus("specialist1", "app1", AppointmentStatus.ATENDIDO, null));
    }

    @Test
    void changeAppointmentStatusThrowsWhenNotAuthorized() {
        Specialty otherSpecialty = new Specialty();
        otherSpecialty.setId("other-spec");
        otherSpecialty.setName("Odontologia");
        Specialist otherSpecialist = new Specialist();
        otherSpecialist.setId("other");
        otherSpecialist.setEspecialidad(otherSpecialty);

        Specialist specialist = buildSpecialist("Medicina General");
        Appointment appointment = buildAppointment("app1", specialist, buildStudent(), AppointmentStatus.PENDIENTE);
        when(appointmentRepository.findById("app1")).thenReturn(Optional.of(appointment));
        when(specialistRepository.findById("specialist1")).thenReturn(Optional.of(otherSpecialist));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.changeAppointmentStatus("specialist1", "app1", AppointmentStatus.ATENDIDO, null));
    }

    @Test
    void getPendingAppointmentsForStudentFiltersByStudent() {
        Student student = buildStudent();
        Student other = new Student();
        other.setId("other");
        Specialist specialist = buildSpecialist("Medicina General");
        Appointment mine = buildAppointment("app1", specialist, student, AppointmentStatus.PENDIENTE);
        Appointment theirs = buildAppointment("app2", specialist, other, AppointmentStatus.PENDIENTE);
        when(appointmentRepository.findAll()).thenReturn(List.of(mine, theirs));

        List<Appointment> result = appointmentService.getPendingAppointmentsForStudent("student1");

        assertEquals(1, result.size());
        assertEquals("app1", result.get(0).getId());
    }

    @Test
    void getAppointmentsForSpecialistReturnsByRange() {
        Specialist specialist = buildSpecialist("Medicina General");
        Appointment appointment = buildAppointment("app1", specialist, buildStudent(), AppointmentStatus.PENDIENTE);
        when(specialistRepository.findById("specialist1")).thenReturn(Optional.of(specialist));
        when(appointmentRepository.findBySpecialtyNameAndAppointmentDateBetween(anyString(), any(), any()))
                .thenReturn(List.of(appointment));

        List<Appointment> result = appointmentService.getAppointmentsForSpecialist(
                "specialist1",
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 8, 31, 23, 59));

        assertEquals(1, result.size());
    }

    @Test
    void getAppointmentsForSpecialistThrowsWhenMissing() {
        when(specialistRepository.findById("specialist1")).thenReturn(Optional.empty());

        LocalDateTime start = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 8, 31, 23, 59);
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.getAppointmentsForSpecialist(
                        "specialist1",
                        start,
                        end));
    }

    @Test
    void getOccupiedSlotsForSpecialistReturnsMap() {
        Specialist specialist = buildSpecialist("Medicina General");
        Appointment active = buildAppointment("app1", specialist, buildStudent(), AppointmentStatus.PENDIENTE);
        Appointment cancelled = buildAppointment("app2", specialist, buildStudent(), AppointmentStatus.CANCELADO_POR_ESTUDIANTE);
        when(specialistRepository.findById("specialist1")).thenReturn(Optional.of(specialist));
        when(appointmentRepository.findBySpecialtyNameAndAppointmentDateBetween(anyString(), any(), any()))
                .thenReturn(List.of(active, cancelled));

        Map<String, Object> result = appointmentService.getOccupiedSlotsForSpecialist(
                "specialist1",
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 8, 31, 23, 59));

        assertEquals("specialist1", result.get("specialistId"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> slots = (List<Map<String, Object>>) result.get("occupiedSlots");
        assertEquals(1, slots.size());
        assertEquals("PENDIENTE", slots.get(0).get("status"));
    }

    @Test
    void getAllAppointmentsReturnsAll() {
        when(appointmentRepository.findAll()).thenReturn(List.of(new Appointment(), new Appointment()));

        assertEquals(2, appointmentService.getAllAppointments().size());
    }

    @Test
    void blockSlotSuccess() {
        Specialist specialist = buildSpecialist("Medicina General");
        when(specialistRepository.findById("specialist1")).thenReturn(Optional.of(specialist));
        when(appointmentRepository.existsBySpecialtyNameAndAppointmentDateAndStartTimeAndStatusNot(anyString(), any(), any(), any()))
                .thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment result = appointmentService.blockSlot("specialist1", LocalDateTime.of(2026, 8, 10, 11, 0));

        assertNotNull(result);
        assertEquals(AppointmentStatus.BLOQUEADO, result.getStatus());
        assertEquals("Horario bloqueado por el especialista", result.getReason());
    }

    @Test
    void blockSlotThrowsWhenOccupied() {
        Specialist specialist = buildSpecialist("Medicina General");
        when(specialistRepository.findById("specialist1")).thenReturn(Optional.of(specialist));
        when(appointmentRepository.existsBySpecialtyNameAndAppointmentDateAndStartTimeAndStatusNot(anyString(), any(), any(), any()))
                .thenReturn(true);

        LocalDateTime dt = LocalDateTime.of(2026, 8, 10, 11, 0);
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.blockSlot("specialist1", dt));
    }

    @Test
    void blockSlotThrowsWhenSpecialistMissing() {
        when(specialistRepository.findById("specialist1")).thenReturn(Optional.empty());

        LocalDateTime dt = LocalDateTime.of(2026, 8, 10, 11, 0);
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.blockSlot("specialist1", dt));
    }

    @Test
    void unblockSlotSuccess() {
        Specialist specialist = buildSpecialist("Medicina General");
        Appointment appointment = buildAppointment("app1", specialist, null, AppointmentStatus.BLOQUEADO);
        when(appointmentRepository.findById("app1")).thenReturn(Optional.of(appointment));
        when(specialistRepository.findById("specialist1")).thenReturn(Optional.of(specialist));

        appointmentService.unblockSlot("specialist1", "app1");

        verify(appointmentRepository).delete(appointment);
        verify(messagingTemplate).convertAndSend("/topic/appointments", "UPDATE");
    }

    @Test
    void unblockSlotThrowsWhenAppointmentMissing() {
        when(appointmentRepository.findById("app1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.unblockSlot("specialist1", "app1"));
    }

    @Test
    void unblockSlotThrowsWhenSpecialistMissing() {
        Specialist specialist = buildSpecialist("Medicina General");
        Appointment appointment = buildAppointment("app1", specialist, null, AppointmentStatus.BLOQUEADO);
        when(appointmentRepository.findById("app1")).thenReturn(Optional.of(appointment));
        when(specialistRepository.findById("specialist1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.unblockSlot("specialist1", "app1"));
    }

    @Test
    void unblockSlotThrowsWhenNotBlocked() {
        Specialist specialist = buildSpecialist("Medicina General");
        Appointment appointment = buildAppointment("app1", specialist, null, AppointmentStatus.PENDIENTE);
        when(appointmentRepository.findById("app1")).thenReturn(Optional.of(appointment));
        when(specialistRepository.findById("specialist1")).thenReturn(Optional.of(specialist));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.unblockSlot("specialist1", "app1"));
    }

    @Test
    void unblockSlotThrowsWhenNotAuthorized() {
        Specialty otherSpecialty = new Specialty();
        otherSpecialty.setId("other-spec");
        otherSpecialty.setName("Odontologia");
        Specialist otherSpecialist = new Specialist();
        otherSpecialist.setId("other");
        otherSpecialist.setEspecialidad(otherSpecialty);

        Specialist specialist = buildSpecialist("Medicina General");
        Appointment appointment = buildAppointment("app1", specialist, null, AppointmentStatus.BLOQUEADO);
        when(appointmentRepository.findById("app1")).thenReturn(Optional.of(appointment));
        when(specialistRepository.findById("specialist1")).thenReturn(Optional.of(otherSpecialist));

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.unblockSlot("specialist1", "app1"));
    }

    @Test
    void initHandlesDatabaseExceptionGracefully() {
        org.mockito.Mockito.doThrow(new RuntimeException("db down")).when(jdbcTemplate).execute(anyString());

        appointmentService.init();

        assertTrue(true);
    }
}
