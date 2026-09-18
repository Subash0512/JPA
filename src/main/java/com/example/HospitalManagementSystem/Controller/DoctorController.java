package com.example.HospitalManagementSystem.Controller;

import jakarta.validation.Valid;

import com.example.HospitalManagementSystem.DTO.DoctorDTO;
import com.example.HospitalManagementSystem.Model.Doctor;
import com.example.HospitalManagementSystem.Service.DoctorService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/doctors")
@CrossOrigin(origins = "*")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }


    // =========================================================
    // GET ALL DOCTORS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<DoctorDTO>> getAllDoctors() {

        List<DoctorDTO> doctors =
                doctorService
                        .getAllDoctors()
                        .stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(doctors);
    }


    // =========================================================
    // GET MY PROFILE
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<Doctor> getMyProfile() {

        Doctor doctor =
                doctorService.getMyProfile();

        return ResponseEntity.ok(doctor);
    }


    // =========================================================
    // GET DOCTOR BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDTO> getDoctorById(
            @PathVariable Long id) {

        return doctorService
                .getDoctorById(id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity.notFound().build()
                );
    }


    // =========================================================
    // CONVERT DOCTOR → DTO
    // =========================================================

    private DoctorDTO toDTO(Doctor doctor) {

        return new DoctorDTO(
                doctor.getId(),
                doctor.getName(),
                doctor.getSpecialization(),
                doctor.getEmail(),
                doctor.getPhone()
        );
    }


    // =========================================================
    // CREATE DOCTOR + LOGIN
    // =========================================================

    @PostMapping
    public ResponseEntity<?> createDoctor(
            @Valid @RequestBody DoctorAccountRequest request) {

        Doctor savedDoctor =
                doctorService.createDoctorWithLogin(
                        request.getDoctor(),
                        request.getUsername(),
                        request.getPassword()
                );

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "message",
                "Doctor and login account created successfully"
        );

        response.put(
                "doctorId",
                savedDoctor.getId()
        );

        response.put(
                "doctorName",
                savedDoctor.getName()
        );

        response.put(
                "username",
                request.getUsername()
        );

        response.put(
                "role",
                "ROLE_DOCTOR"
        );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // UPDATE DOCTOR
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<Doctor> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody Doctor updatedDoctor) {

        Doctor savedDoctor =
                doctorService.updateDoctor(
                        id,
                        updatedDoctor
                );

        return ResponseEntity.ok(savedDoctor);
    }


    // =========================================================
    // DELETE DOCTOR
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(
            @PathVariable Long id) {

        doctorService.deleteDoctor(id);

        return ResponseEntity.noContent().build();
    }


    // =========================================================
    // REQUEST BODY FOR CREATE
    // =========================================================

    public static class DoctorAccountRequest {

        private Doctor doctor;

        private String username;

        private String password;


        public DoctorAccountRequest() {
        }


        public Doctor getDoctor() {
            return doctor;
        }


        public void setDoctor(Doctor doctor) {
            this.doctor = doctor;
        }


        public String getUsername() {
            return username;
        }


        public void setUsername(String username) {
            this.username = username;
        }


        public String getPassword() {
            return password;
        }


        public void setPassword(String password) {
            this.password = password;
        }
    }
}