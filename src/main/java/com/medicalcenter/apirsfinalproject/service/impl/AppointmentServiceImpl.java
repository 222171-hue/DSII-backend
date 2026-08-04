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
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private static final String SPECIALIST_NOT_FOUND = "Specialist not found";
    private static final String AT_TEXT = " a las ";
    private static final String UPDATE_MESSAGE = "UPDATE";
    private static final String APPOINTMENTS_TOPIC = "/topic/appointments";
    private static final Logger logger = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final AppointmentRepository appointmentRepository;
    private final SpecialistRepository specialistRepository;
    private final StudentRepository studentRepository;
    private final com.medicalcenter.apirsfinalproject.repository.UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

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
                .orElseThrow(() -> new IllegalArgumentException(SPECIALIST_NOT_FOUND));
                
        if (!specialist.getEspecialidad().getName().equals(request.getSpecialty())) {
             throw new IllegalArgumentException("El especialista no pertenece a esa especialidad");
        }

        LocalDate appDate = request.getDateTime().toLocalDate();
        LocalTime startTime = request.getDateTime().toLocalTime();
        LocalTime endTime = startTime.plusMinutes(30);

        boolean exists = appointmentRepository.existsBySpecialtyNameAndAppointmentDateAndStartTimeAndStatusNot(
                specialist.getEspecialidad().getName(), appDate, startTime, AppointmentStatus.CANCELADO_POR_ESTUDIANTE);
        
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

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Notificar a todos los especialistas de la misma especialidad
        String msgToSpecialist = "Se ha agendado una nueva cita con el estudiante " + student.getNombre() + " " + student.getApellidos() + 
                                 " para el " + appDate + AT_TEXT + startTime + ".";
        java.util.List<User> specialists = userRepository.findByRol(com.medicalcenter.apirsfinalproject.entity.Role.SPECIALIST);
        logger.info("Found {} specialists in total.", specialists.size());
        for (User spec : specialists) {
            specialistRepository.findById(spec.getId()).ifPresent(s -> {
                logger.info("Checking specialist {} with specialty {}", s.getNombre(), s.getEspecialidad().getName());
                if (s.getEspecialidad().getName().equals(request.getSpecialty())) {
                    logger.info("Specialty matches! Creating notification for specialist {}", s.getId());
                    notificationService.createNotification(s.getId(), msgToSpecialist);
                }
            });
        }

        // Notificar a todos los administradores
        String msgToAdmin = "El estudiante " + student.getNombre() + " " + student.getApellidos() + 
                            " ha registrado una nueva cita en la especialidad de " + specialist.getEspecialidad().getName() + 
                            " para el " + appDate + AT_TEXT + startTime + ".";
        java.util.List<User> admins = userRepository.findByRol(com.medicalcenter.apirsfinalproject.entity.Role.ADMIN);
        logger.info("Found {} admins in total.", admins.size());
        for (User admin : admins) {
            notificationService.createNotification(admin.getId(), msgToAdmin);
        }

        messagingTemplate.convertAndSend(APPOINTMENTS_TOPIC, UPDATE_MESSAGE);

        return savedAppointment;
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
        messagingTemplate.convertAndSend(APPOINTMENTS_TOPIC, UPDATE_MESSAGE);
    }

    @Override
    @Transactional
    public void changeAppointmentStatus(String specialistId, String appointmentId, AppointmentStatus newStatus, String cancelReason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException(APPOINTMENT_NOT_FOUND));

        Specialist modifier = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new IllegalArgumentException(SPECIALIST_NOT_FOUND));

        if (!appointment.getSpecialist().getEspecialidad().getId().equals(modifier.getEspecialidad().getId())) {
            throw new IllegalArgumentException("Not authorized to modify this appointment");
        }

        appointment.setStatus(newStatus);
        
        if (newStatus == AppointmentStatus.CANCELADO_POR_ESPECIALISTA) {
            if (cancelReason != null && !cancelReason.trim().isEmpty()) {
                appointment.setCancelReason(cancelReason);
            }
            if (appointment.getStudent() != null) {
                String message = "Tu cita con " + appointment.getSpecialist().getNombre() + " " + appointment.getSpecialist().getApellidos() + 
                                 " el " + appointment.getAppointmentDate() + AT_TEXT + appointment.getStartTime() + " ha sido cancelada por el especialista.";
                if (cancelReason != null && !cancelReason.trim().isEmpty()) {
                    message += " Motivo: " + cancelReason;
                }
                message += " Puedes reprogramar en caso lo requieras.";
                notificationService.createNotification(appointment.getStudent().getId(), message);
            }
        }
        
        appointmentRepository.save(appointment);
        messagingTemplate.convertAndSend(APPOINTMENTS_TOPIC, UPDATE_MESSAGE);
    }

    @Override
    public List<Appointment> getPendingAppointmentsForStudent(String studentId) {
        // Obtenemos todas y filtramos localmente para simplificar la consulta
        return appointmentRepository.findAll().stream()
                .filter(a -> a.getStudent() != null && a.getStudent().getId().equals(studentId))
                .toList();
    }

    @Override
    public List<Appointment> getAppointmentsForSpecialist(String specialistId, LocalDateTime start, LocalDateTime end) {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new IllegalArgumentException(SPECIALIST_NOT_FOUND));
        return appointmentRepository.findBySpecialtyNameAndAppointmentDateBetween(specialist.getEspecialidad().getName(), startDate, endDate);
    }

    @Override
    public Map<String, Object> getOccupiedSlotsForSpecialist(String specialistId, LocalDateTime start, LocalDateTime end) {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new IllegalArgumentException(SPECIALIST_NOT_FOUND));
        List<Appointment> apps = appointmentRepository.findBySpecialtyNameAndAppointmentDateBetween(specialist.getEspecialidad().getName(), startDate, endDate);
        
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
                .orElseThrow(() -> new IllegalArgumentException(SPECIALIST_NOT_FOUND));

        LocalDate appDate = dateTime.toLocalDate();
        LocalTime startTime = dateTime.toLocalTime();
        LocalTime endTime = startTime.plusMinutes(30);

        boolean exists = appointmentRepository.existsBySpecialtyNameAndAppointmentDateAndStartTimeAndStatusNot(
                specialist.getEspecialidad().getName(), appDate, startTime, AppointmentStatus.CANCELADO_POR_ESTUDIANTE);

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

        Appointment saved = appointmentRepository.save(appointment);
        messagingTemplate.convertAndSend(APPOINTMENTS_TOPIC, UPDATE_MESSAGE);
        return saved;
    }

    @Override
    @Transactional
    public void unblockSlot(String specialistId, String appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException(APPOINTMENT_NOT_FOUND));

        Specialist modifier = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new IllegalArgumentException(SPECIALIST_NOT_FOUND));

        if (!appointment.getSpecialist().getEspecialidad().getId().equals(modifier.getEspecialidad().getId())) {
            throw new IllegalArgumentException("Not authorized to modify this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.BLOQUEADO) {
            throw new IllegalArgumentException("Only blocked appointments can be unblocked");
        }

        appointmentRepository.delete(appointment);
        messagingTemplate.convertAndSend(APPOINTMENTS_TOPIC, UPDATE_MESSAGE);
    }
}
