package com.example.HospitalManagementSystem.Repsitory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.HospitalManagementSystem.Model.Appointment;
import com.example.HospitalManagementSystem.Model.Doctor;
import com.example.HospitalManagementSystem.Model.Patient;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepo extends JpaRepository<Appointment, Long> {
    
    List<Appointment> findByPatient(Patient patient);
    
    List<Appointment> findByDoctor(Doctor doctor);
    
    List<Appointment> findByPatientId(Long patientId);
    
    List<Appointment> findByDoctorId(Long doctorId);
    
    List<Appointment> findByAppointmentTimeBetween(LocalDateTime start, LocalDateTime end);
    
    List<Appointment> findByStatus(String status);
}