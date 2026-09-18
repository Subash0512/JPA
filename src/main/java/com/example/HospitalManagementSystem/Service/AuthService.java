package com.example.HospitalManagementSystem.Service;

import com.example.HospitalManagementSystem.Model.Doctor;
import com.example.HospitalManagementSystem.Model.Patient;
import com.example.HospitalManagementSystem.Model.User;
import com.example.HospitalManagementSystem.Repsitory.DoctorRepo;
import com.example.HospitalManagementSystem.Repsitory.PatientRepo;
import com.example.HospitalManagementSystem.Repsitory.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepo patientRepo;
    private final DoctorRepo doctorRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PatientRepo patientRepo,
            DoctorRepo doctorRepo,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================
    // REGISTER PATIENT
    // =========================================================

    public Patient registerPatient(
            Patient patient,
            String username,
            String password) {

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Username already exists"
            );
        }

        if (patientRepo.findByEmail(patient.getEmail()).isPresent()) {
            throw new IllegalArgumentException(
                    "Patient email already exists"
            );
        }

        User user = new User();

        user.setUsername(username);
        user.setPassword(
                passwordEncoder.encode(password)
        );
        user.setRole("PATIENT");
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        patient.setUser(savedUser);

        return patientRepo.save(patient);
    }


    // =========================================================
    // REGISTER DOCTOR
    // =========================================================

    public Doctor registerDoctor(
            Doctor doctor,
            String username,
            String password) {

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Username already exists"
            );
        }

        if (doctorRepo.findByEmail(doctor.getEmail()).isPresent()) {
            throw new IllegalArgumentException(
                    "Doctor email already exists"
            );
        }

        User user = new User();

        user.setUsername(username);
        user.setPassword(
                passwordEncoder.encode(password)
        );
        user.setRole("DOCTOR");
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        doctor.setUser(savedUser);

        return doctorRepo.save(doctor);
    }
 
}