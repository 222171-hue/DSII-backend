package com.medicalcenter.apirsfinalproject.repository;

import com.medicalcenter.apirsfinalproject.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, String> {
}
