package com.example.HospitalManagementSystem.Service;

import com.example.HospitalManagementSystem.Model.Patient;
import com.example.HospitalManagementSystem.Repsitory.PatientRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepo patientRepo;

    public PatientService(PatientRepo patientRepo) {
        this.patientRepo = patientRepo;
    }

    public Patient savePatient(Patient patient) {
        if (patient.getName() == null || patient.getName().trim().isEmpty()) {
            throw new RuntimeException("Patient name is required");
        }
        if (patient.getAge() == null || patient.getAge() <= 0) {
            throw new RuntimeException("Valid patient age is required");
        }
        return patientRepo.save(patient);
    }

    public List<Patient> getAllPatients() {
        return patientRepo.findAll();
    }

    public Optional<Patient> getPatientById(Long id) {
        return patientRepo.findById(id);
    }

    public void deletePatient(Long id) {
        patientRepo.deleteById(id);
    }

    public Optional<Patient> findByEmail(String email) {
        return patientRepo.findByEmail(email);
    }

    public Optional<Patient> findByPhone(String phone) {
        return patientRepo.findByPhone(phone);
    }
}