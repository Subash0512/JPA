package com.example.HospitalManagementSystem.Repsitory;

import com.example.HospitalManagementSystem.Model.Patient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepo extends JpaRepository<Patient, Long> {

    Optional<Patient> findByEmail(String email);

    Optional<Patient> findByPhone(String phone);

    /*
     * Used to find the Patient record belonging to
     * the currently logged-in username.
     */
    Optional<Patient> findByUserUsername(String username);
}