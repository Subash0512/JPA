package com.example.HospitalManagementSystem.Controller;

import com.example.HospitalManagementSystem.Model.Doctor;
import com.example.HospitalManagementSystem.Model.Patient;
import com.example.HospitalManagementSystem.Service.AuthService;
import com.example.HospitalManagementSystem.Repsitory.PatientRepo;
import com.example.HospitalManagementSystem.Repsitory.DoctorRepo;
import com.example.HospitalManagementSystem.Service.JwtService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthService authService;
    private final PatientRepo patientRepo;
    private final DoctorRepo doctorRepo;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            AuthService authService,
            PatientRepo patientRepo,
            DoctorRepo doctorRepo) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.authService = authService;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        String token =
                jwtService.generateToken(userDetails);

        String role =
                authentication.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(GrantedAuthority::getAuthority)
                        .orElse("ROLE_USER");

        Map<String, Object> response =
                new HashMap<>();

        response.put("message", "Login successful");
        response.put("username", authentication.getName());
        response.put("role", role);
        response.put("token", token);

        /*
         * Return the actual person's name for the frontend.
         *
         * ADMIN is intentionally unchanged.
         * PATIENT -> Patient.name
         * DOCTOR  -> Doctor.name
         */
        if ("ROLE_PATIENT".equals(role)) {

            patientRepo
                    .findByUserUsername(authentication.getName())
                    .ifPresent(patient ->
                            response.put("name", patient.getName())
                    );
        }

        else if ("ROLE_DOCTOR".equals(role)) {

            doctorRepo
                    .findByUserUsername(authentication.getName())
                    .ifPresent(doctor ->
                            response.put("name", doctor.getName())
                    );
        }

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // REGISTER PATIENT
    // =========================================================

    @PostMapping("/register/patient")
    public ResponseEntity<?> registerPatient(
            @RequestBody PatientRegistrationRequest request) {

        Patient patient = authService.registerPatient(
                request.getPatient(),
                request.getUsername(),
                request.getPassword()
        );

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "message",
                "Patient registered successfully"
        );

        response.put("patientId", patient.getId());
        response.put("username", request.getUsername());
        response.put("role", "ROLE_PATIENT");

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // REGISTER DOCTOR
    // =========================================================

    @PostMapping("/register/doctor")
    public ResponseEntity<?> registerDoctor(
            @RequestBody DoctorRegistrationRequest request) {

        Doctor doctor = authService.registerDoctor(
                request.getDoctor(),
                request.getUsername(),
                request.getPassword()
        );

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "message",
                "Doctor registered successfully"
        );

        response.put("doctorId", doctor.getId());
        response.put("username", request.getUsername());
        response.put("role", "ROLE_DOCTOR");

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // LOGIN REQUEST
    // =========================================================

    public static class LoginRequest {

        private String username;
        private String password;

        public LoginRequest() {
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


    // =========================================================
    // PATIENT REGISTRATION REQUEST
    // =========================================================

    public static class PatientRegistrationRequest {

        private String username;
        private String password;
        private Patient patient;

        public PatientRegistrationRequest() {
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

        public Patient getPatient() {
            return patient;
        }

        public void setPatient(Patient patient) {
            this.patient = patient;
        }
    }


    // =========================================================
    // DOCTOR REGISTRATION REQUEST
    // =========================================================

    public static class DoctorRegistrationRequest {

        private String username;
        private String password;
        private Doctor doctor;

        public DoctorRegistrationRequest() {
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

        public Doctor getDoctor() {
            return doctor;
        }

        public void setDoctor(Doctor doctor) {
            this.doctor = doctor;
        }
    }
 
}