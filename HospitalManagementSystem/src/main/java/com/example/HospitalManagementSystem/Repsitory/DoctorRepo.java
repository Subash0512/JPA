package com.example.HospitalManagementSystem.Repsitory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.HospitalManagementSystem.Model.Doctor;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepo extends JpaRepository<Doctor, Long> {
    
    Optional<Doctor> findByEmail(String email);
    
    List<Doctor> findBySpecialization(String specialization);
    
    Optional<Doctor> findByPhone(String phone);
}