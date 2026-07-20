package com.medicalcenter.apirsfinalproject.service;

import com.medicalcenter.apirsfinalproject.dto.request.AppointmentRequest;
import com.medicalcenter.apirsfinalproject.entity.Appointment;
import com.medicalcenter.apirsfinalproject.entity.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AppointmentService {
    Appointment bookAppointment(String studentId, AppointmentRequest request);
    void cancelAppointmentByStudent(String studentId, String appointmentId);
    void changeAppointmentStatus(String specialistId, String appointmentId, AppointmentStatus newStatus, String cancelReason);
    List<Appointment> getPendingAppointmentsForStudent(String studentId);
    List<Appointment> getAppointmentsForSpecialist(String specialistId, LocalDateTime start, LocalDateTime end);
    Map<String, Object> getOccupiedSlotsForSpecialist(String specialistId, LocalDateTime start, LocalDateTime end);
    List<Appointment> getAllAppointments();
    Appointment blockSlot(String specialistId, LocalDateTime dateTime);
    void unblockSlot(String specialistId, String appointmentId);
}
