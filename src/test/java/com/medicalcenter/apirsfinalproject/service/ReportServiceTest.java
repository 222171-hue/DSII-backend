package com.medicalcenter.apirsfinalproject.service;

import com.medicalcenter.apirsfinalproject.entity.Appointment;
import com.medicalcenter.apirsfinalproject.entity.AppointmentStatus;
import com.medicalcenter.apirsfinalproject.entity.Specialist;
import com.medicalcenter.apirsfinalproject.entity.Specialty;
import com.medicalcenter.apirsfinalproject.entity.Student;
import com.medicalcenter.apirsfinalproject.repository.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private ReportService reportService;

    private Student buildStudent() {
        Student student = new Student();
        student.setNombre("Juan");
        student.setApellidos("Perez");
        student.setCarrera("Ingenieria");
        return student;
    }

    private Specialist buildSpecialist(String specialtyName) {
        Specialty specialty = new Specialty();
        specialty.setName(specialtyName);
        Specialist specialist = new Specialist();
        specialist.setEspecialidad(specialty);
        return specialist;
    }

    private Appointment buildAppointment(String specialtyName, LocalDate date) {
        return Appointment.builder()
                .specialist(buildSpecialist(specialtyName))
                .student(buildStudent())
                .appointmentDate(date)
                .startTime(LocalTime.of(9, 0))
                .status(AppointmentStatus.PENDIENTE)
                .build();
    }

    @Test
    void generateAppointmentsReportWithoutFilters() {
        when(appointmentRepository.findAll()).thenReturn(List.of(
                buildAppointment("Medicina General", LocalDate.of(2026, 8, 1)),
                buildAppointment("Odontologia", LocalDate.of(2026, 8, 2))
        ));

        byte[] pdf = reportService.generateAppointmentsReport(null, null, null);

        String header = new String(pdf, 0, Math.min(5, pdf.length), StandardCharsets.ISO_8859_1);
        assertTrue(header.startsWith("%PDF"));
    }

    @Test
    void generateAppointmentsReportFiltersBySpecialty() {
        when(appointmentRepository.findAll()).thenReturn(List.of(
                buildAppointment("Medicina General", LocalDate.of(2026, 8, 1)),
                buildAppointment("Odontologia", LocalDate.of(2026, 8, 2))
        ));

        byte[] pdf = reportService.generateAppointmentsReport("odontologia", null, null);

        String header = new String(pdf, 0, Math.min(5, pdf.length), StandardCharsets.ISO_8859_1);
        assertTrue(header.startsWith("%PDF"));
    }

    @Test
    void generateAppointmentsReportFiltersByStartDate() {
        when(appointmentRepository.findAll()).thenReturn(List.of(
                buildAppointment("Medicina General", LocalDate.of(2026, 8, 1)),
                buildAppointment("Medicina General", LocalDate.of(2026, 8, 15))
        ));

        byte[] pdf = reportService.generateAppointmentsReport(null, LocalDate.of(2026, 8, 10), null);

        String header = new String(pdf, 0, Math.min(5, pdf.length), StandardCharsets.ISO_8859_1);
        assertTrue(header.startsWith("%PDF"));
    }

    @Test
    void generateAppointmentsReportFiltersByEndDate() {
        when(appointmentRepository.findAll()).thenReturn(List.of(
                buildAppointment("Medicina General", LocalDate.of(2026, 8, 1)),
                buildAppointment("Medicina General", LocalDate.of(2026, 8, 15))
        ));

        byte[] pdf = reportService.generateAppointmentsReport(null, null, LocalDate.of(2026, 8, 10));

        String header = new String(pdf, 0, Math.min(5, pdf.length), StandardCharsets.ISO_8859_1);
        assertTrue(header.startsWith("%PDF"));
    }

    @Test
    void generateAppointmentsReportHandlesAppointmentWithoutStudent() {
        Appointment blocked = Appointment.builder()
                .specialist(buildSpecialist("Medicina General"))
                .student(null)
                .appointmentDate(LocalDate.of(2026, 8, 1))
                .startTime(LocalTime.of(9, 0))
                .status(AppointmentStatus.BLOQUEADO)
                .build();
        when(appointmentRepository.findAll()).thenReturn(List.of(blocked));

        byte[] pdf = reportService.generateAppointmentsReport(null, null, null);

        String header = new String(pdf, 0, Math.min(5, pdf.length), StandardCharsets.ISO_8859_1);
        assertTrue(header.startsWith("%PDF"));
    }

    @Test
    void generateAppointmentsReportHandlesAppointmentWithoutSpecialist() {
        Appointment withoutSpecialist = Appointment.builder()
                .specialist(null)
                .student(buildStudent())
                .appointmentDate(LocalDate.of(2026, 8, 1))
                .startTime(LocalTime.of(9, 0))
                .status(AppointmentStatus.PENDIENTE)
                .build();
        when(appointmentRepository.findAll()).thenReturn(List.of(withoutSpecialist));

        byte[] pdf = reportService.generateAppointmentsReport(null, null, null);

        assertTrue(pdf.length > 0);
    }

    @Test
    void generateAppointmentsReportHandlesEmptyList() {
        when(appointmentRepository.findAll()).thenReturn(List.of());

        byte[] pdf = reportService.generateAppointmentsReport(null, null, null);

        assertTrue(pdf.length > 0);
    }
}
