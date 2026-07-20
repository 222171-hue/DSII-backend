package com.medicalcenter.apirsfinalproject.repository;

import com.medicalcenter.apirsfinalproject.entity.Appointment;
import com.medicalcenter.apirsfinalproject.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, String> {
    
    // Asignaremos la especialidad a través del especialista asociado.
    @Query("SELECT a FROM Appointment a WHERE a.student.id = :studentId AND a.status = :status AND a.specialist.especialidad.name = :specialtyName")
    List<Appointment> findByStudentIdAndStatusAndSpecialtyName(@Param("studentId") String studentId, @Param("status") AppointmentStatus status, @Param("specialtyName") String specialtyName);
    
    @Query("SELECT a FROM Appointment a WHERE a.specialist.especialidad.name = :specialtyName AND a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findBySpecialtyNameAndAppointmentDateBetween(@Param("specialtyName") String specialtyName, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    List<Appointment> findBySpecialistIdAndAppointmentDateBetween(String specialistId, LocalDate startDate, LocalDate endDate);
    
    List<Appointment> findByAppointmentDateBetween(LocalDate startDate, LocalDate endDate);
    
    boolean existsBySpecialistIdAndAppointmentDateAndStartTimeAndStatusNot(String specialistId, LocalDate appointmentDate, LocalTime startTime, AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.student.id = :studentId AND a.status = :status AND a.specialist.especialidad.name = :specialtyName")
    long countPendingAppointmentsByStudentAndSpecialty(@Param("studentId") String studentId, @Param("status") AppointmentStatus status, @Param("specialtyName") String specialtyName);
}
