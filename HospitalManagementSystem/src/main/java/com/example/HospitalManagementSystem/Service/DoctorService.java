package com.example.HospitalManagementSystem.Service;

import com.example.HospitalManagementSystem.Model.Doctor;
import com.example.HospitalManagementSystem.Repsitory.DoctorRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    private final DoctorRepo doctorRepo;

    public DoctorService(DoctorRepo doctorRepo) {
        this.doctorRepo = doctorRepo;
    }

    public Doctor saveDoctor(Doctor doctor) {
        if (doctor.getName() == null || doctor.getName().trim().isEmpty()) {
            throw new RuntimeException("Doctor name is required");
        }
        if (doctor.getSpecialization() == null || doctor.getSpecialization().trim().isEmpty()) {
            throw new RuntimeException("Doctor specialization is required");
        }
        if (doctor.getEmail() == null || doctor.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Doctor email is required");
        }
        return doctorRepo.save(doctor);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepo.findAll();
    }

    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepo.findById(id);
    }

    public void deleteDoctor(Long id) {
        doctorRepo.deleteById(id);
    }

    public Optional<Doctor> findByEmail(String email) {
        return doctorRepo.findByEmail(email);
    }

    public List<Doctor> findBySpecialization(String specialization) {
        return doctorRepo.findBySpecialization(specialization);
    }

    public Optional<Doctor> findByPhone(String phone) {
        return doctorRepo.findByPhone(phone);
    }
}