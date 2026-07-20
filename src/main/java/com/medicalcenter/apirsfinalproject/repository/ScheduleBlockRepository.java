package com.medicalcenter.apirsfinalproject.repository;

import com.medicalcenter.apirsfinalproject.entity.ScheduleBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleBlockRepository extends JpaRepository<ScheduleBlock, String> {
    List<ScheduleBlock> findBySpecialistIdAndBlockDateBetween(String specialistId, LocalDate startDate, LocalDate endDate);
}
