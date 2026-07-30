package com.medicalcenter.apirsfinalproject.service.impl;

import com.medicalcenter.apirsfinalproject.dto.request.AppointmentRequest;
import com.medicalcenter.apirsfinalproject.entity.Appointment;
import com.medicalcenter.apirsfinalproject.entity.AppointmentStatus;
import com.medicalcenter.apirsfinalproject.entity.Specialist;
import com.medicalcenter.apirsfinalproject.entity.Specialty;
import com.medicalcenter.apirsfinalproject.entity.User;
import com.medicalcenter.apirsfinalproject.entity.Student;
import com.medicalcenter.apirsfinalproject.repository.AppointmentRepository;
import com.medicalcenter.apirsfinalproject.repository.SpecialistRepository;
import com.medicalcenter.apirsfinalproject.repository.StudentRepository;
import com.medicalcenter.apirsfinalproject.service.AppointmentService;
import com.medicalcenter.apirsfinalproject.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.annotation.PostConstruct;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private static final String APPOINTMENT_NOT_FOUND = "Appointment not found";
    private static final Logger logger = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final AppointmentRepository appointmentRepository;
    private final SpecialistRepository specialistRepository;
    private final StudentRepository studentRepository;
    private final JdbcTemplate jdbcTemplate;
    private final NotificationService notificationService;

    @PostConstruct
    public void init() {
        try {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
            alterAppointmentStudentColumn();
            jdbcTemplate.execute("ALTER TABLE tappointment MODIFY status VARCHAR(30) NOT NULL");
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
        } catch (Exception e) {
            logger.error("No se pudo alterar la tabla tappointment: {}", e.getMessage());
        }
    }

    private void alterAppointmentStudentColumn() {
        try {
            jdbcTemplate.execute("ALTER TABLE tappointment MODIFY id_student VARCHAR(36) NULL");
        } catch (Exception e1) {
            try {
                jdbcTemplate.execute("ALTER TABLE tappointment MODIFY idStudent VARCHAR(36) NULL");
            } catch (Exception e2) {
                logger.warn("Could not modify idStudent column", e2);
            }
        }
    }

    @Override
    @Transactional
    public Appointment bookAppointment(String studentId, AppointmentRequest request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Specialist specialist = specialistRepository.findById(request.getSpecialistId())
                .orElseThrow(() -> new IllegalArgumentException("Specialist not found"));
                
        if (!specialist.getEspecialidad().getName().equals(request.getSpecialty())) {
             throw new IllegalArgumentException("El especialista no pertenece a esa especialidad");
        }

        LocalDate appDate = request.getDateTime().toLocalDate();
        LocalTime startTime = request.getDateTime().toLocalTime();
        LocalTime endTime = startTime.plusMinutes(30);

        boolean exists = appointmentRepository.existsBySpecialistIdAndAppointmentDateAndStartTimeAndStatusNot(
                specialist.getId(), appDate, startTime, AppointmentStatus.CANCELADO_POR_ESTUDIANTE);
        
        if (exists) {
            throw new IllegalArgumentException("El horario ya está ocupado");
        }
        
        // Verifica limite por especialidad eliminado a pedido del usuario

        Appointment appointment = Appointment.builder()
                .id(UUID.randomUUID().toString())
                .student(student)
                .specialist(specialist)
                .appointmentDate(appDate)
                .startTime(startTime)
                .endTime(endTime)
                .status(AppointmentStatus.PENDIENTE)
                .reason(request.getReason())
                .build();

        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public void cancelAppointmentByStudent(String studentId, String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException(APPOINTMENT_NOT_FOUND));

        if (appointment.getStudent() == null || !appointment.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("Not authorized to cancel this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDIENTE) {
            throw new IllegalArgumentException("Only pending appointments can be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELADO_POR_ESTUDIANTE);
        appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public void changeAppointmentStatus(String specialistId, String appointmentId, AppointmentStatus newStatus, String cancelReason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException(APPOINTMENT_NOT_FOUND));

        if (!appointment.getSpecialist().getId().equals(specialistId)) {
            throw new IllegalArgumentException("Not authorized to modify this appointment");
        }

        appointment.setStatus(newStatus);
        
        if (newStatus == AppointmentStatus.CANCELADO_POR_ESPECIALISTA) {
            if (cancelReason != null && !cancelReason.trim().isEmpty()) {
                appointment.setCancelReason(cancelReason);
            }
            if (appointment.getStudent() != null) {
                String message = "Tu cita con " + appointment.getSpecialist().getNombre() + " " + appointment.getSpecialist().getApellidos() + 
                                 " el " + appointment.getAppointmentDate() + " a las " + appointment.getStartTime() + " ha sido cancelada por el especialista.";
                if (cancelReason != null && !cancelReason.trim().isEmpty()) {
                    message += " Motivo: " + cancelReason;
                }
                message += " Puedes reprogramar en caso lo requieras.";
                notificationService.createNotification(appointment.getStudent().getId(), message);
            }
        }
        
        appointmentRepository.save(appointment);
    }

    @Override
    public List<Appointment> getPendingAppointmentsForStudent(String studentId) {
        // Obtenemos todas y filtramos localmente para simplificar la consulta
        List<Appointment> studentAppointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getStudent() != null && a.getStudent().getId().equals(studentId))
                .toList();

        LocalDateTime now = LocalDateTime.now();
        
        for (Appointment app : studentAppointments) {
            if (app.getStatus() == AppointmentStatus.PENDIENTE) {
                LocalDateTime appDateTime = LocalDateTime.of(app.getAppointmentDate(), app.getStartTime());
                if (appDateTime.isBefore(now)) {
                    app.setStatus(AppointmentStatus.AUSENTE);
                    appointmentRepository.save(app);
                                    }
            }
        }
        
        return studentAppointments;
    }

    @Override
    public List<Appointment> getAppointmentsForSpecialist(String specialistId, LocalDateTime start, LocalDateTime end) {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        return appointmentRepository.findBySpecialistIdAndAppointmentDateBetween(specialistId, startDate, endDate);
    }

    @Override
    public Map<String, Object> getOccupiedSlotsForSpecialist(String specialistId, LocalDateTime start, LocalDateTime end) {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        List<Appointment> apps = appointmentRepository.findBySpecialistIdAndAppointmentDateBetween(specialistId, startDate, endDate);
        
        List<Map<String, Object>> occupiedSlots = apps.stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELADO_POR_ESTUDIANTE && a.getStatus() != AppointmentStatus.CANCELADO_POR_ESPECIALISTA)
                .map(a -> {
                    LocalDateTime dt = LocalDateTime.of(a.getAppointmentDate(), a.getStartTime());
                    Map<String, Object> slot = new HashMap<>();
                    slot.put("dateTime", dt);
                    slot.put("status", a.getStatus().name());
                    return slot;
                })
                .toList();
                
        Map<String, Object> response = new HashMap<>();
        response.put("specialistId", specialistId);
        response.put("occupiedSlots", occupiedSlots);
        return response;
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    @Transactional
    public Appointment blockSlot(String specialistId, LocalDateTime dateTime) {
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new IllegalArgumentException("Specialist not found"));

        LocalDate appDate = dateTime.toLocalDate();
        LocalTime startTime = dateTime.toLocalTime();
        LocalTime endTime = startTime.plusMinutes(30);

        boolean exists = appointmentRepository.existsBySpecialistIdAndAppointmentDateAndStartTimeAndStatusNot(
                specialistId, appDate, startTime, AppointmentStatus.CANCELADO_POR_ESTUDIANTE);

        if (exists) {
            throw new IllegalArgumentException("El horario ya está ocupado");
        }

        Appointment appointment = Appointment.builder()
                .id(UUID.randomUUID().toString())
                .student(null)
                .specialist(specialist)
                .appointmentDate(appDate)
                .startTime(startTime)
                .endTime(endTime)
                .status(AppointmentStatus.BLOQUEADO)
                .reason("Horario bloqueado por el especialista")
                .build();

        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public void unblockSlot(String specialistId, String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException(APPOINTMENT_NOT_FOUND));

        if (!appointment.getSpecialist().getId().equals(specialistId)) {
            throw new IllegalArgumentException("Not authorized to modify this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.BLOQUEADO) {
            throw new IllegalArgumentException("Only blocked appointments can be unblocked");
        }

        appointmentRepository.delete(appointment);
    }
}
