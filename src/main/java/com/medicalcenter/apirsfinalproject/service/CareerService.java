package com.medicalcenter.apirsfinalproject.service;

import com.medicalcenter.apirsfinalproject.entity.Career;
import java.util.List;

public interface CareerService {
    Career saveCareer(Career career);
    List<Career> getAllCareers();
    boolean existsByName(String name);
    Career updateCareer(String id, Career career);
    void deleteCareer(String id);
}
