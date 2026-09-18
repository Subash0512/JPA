package com.example.HospitalManagementSystem.Service;

import com.example.HospitalManagementSystem.Model.Doctor;
import com.example.HospitalManagementSystem.Model.User;
import com.example.HospitalManagementSystem.Repsitory.DoctorRepo;
import com.example.HospitalManagementSystem.Repsitory.UserRepository;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
public class ExistingDoctorAccountService {

    private final DoctorRepo doctorRepo;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ExistingDoctorAccountService(
            DoctorRepo doctorRepo,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.doctorRepo = doctorRepo;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================
    // LINK EXISTING DOCTOR TO LOGIN ACCOUNT
    // =========================================================

    public Doctor createDoctorLogin(
            Long doctorId,
            String username,
            String password) {

        // =====================================================
        // CHECK ADMIN AUTHORIZATION
        // =====================================================

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Authentication is required"
            );
        }

        String role =
                authentication.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(authority ->
                                authority.getAuthority())
                        .orElse("");

        if (!role.equals("ROLE_ADMIN")) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only admin can create doctor login accounts"
            );
        }

        // =====================================================
        // FIND EXISTING DOCTOR
        // =====================================================

        Doctor doctor =
                doctorRepo.findById(doctorId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Doctor not found with ID: "
                                                + doctorId
                                )
                        );

        // =====================================================
        // CHECK USERNAME
        // =====================================================

        if (userRepository.existsByUsername(username)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists"
            );
        }

        // =====================================================
        // CHECK WHETHER DOCTOR ALREADY HAS ACCOUNT
        // =====================================================

        if (doctor.getUser() != null) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This doctor already has a login account"
            );
        }

        // =====================================================
        // CREATE USER
        // =====================================================

        User user = new User();

        user.setUsername(username);

        user.setPassword(
                passwordEncoder.encode(password)
        );

        user.setRole("DOCTOR");

        user.setEnabled(true);

        User savedUser =
                userRepository.save(user);

        // =====================================================
        // LINK USER TO EXISTING DOCTOR
        // =====================================================

        doctor.setUser(savedUser);

        return doctorRepo.save(doctor);
    }
    // =========================================================
    // LOGIN STATUS
    // =========================================================

    @Transactional(readOnly = true)
    public Map<String, Object> getDoctorLoginStatus(Long doctorId) {

        requireAdmin();

        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Doctor not found with ID: " + doctorId));

        Map<String, Object> response = new HashMap<>();
        response.put("doctorId", doctor.getId());
        response.put("loginCreated", doctor.getUser() != null);
        response.put("enabled", doctor.getUser() != null && doctor.getUser().isEnabled());
        response.put("username", doctor.getUser() != null ? doctor.getUser().getUsername() : null);
        return response;
    }

    // =========================================================
    // ENABLE / DISABLE LOGIN
    // =========================================================

    @Transactional
    public Map<String, Object> updateDoctorLoginStatus(Long doctorId, boolean enabled) {

        requireAdmin();

        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Doctor not found with ID: " + doctorId));

        if (doctor.getUser() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "This doctor does not have a login account yet");
        }

        User user = doctor.getUser();
        user.setEnabled(enabled);
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", enabled ? "Doctor login enabled" : "Doctor login disabled");
        response.put("doctorId", doctorId);
        response.put("enabled", enabled);
        return response;
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================

    @Transactional
    public Map<String, Object> resetDoctorPassword(Long doctorId, String password) {

        requireAdmin();

        if (password == null || password.trim().length() < 6) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }

        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Doctor not found with ID: " + doctorId));

        if (doctor.getUser() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "This doctor does not have a login account yet");
        }

        User user = doctor.getUser();
        user.setPassword(passwordEncoder.encode(password.trim()));
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Doctor password reset successfully");
        response.put("doctorId", doctorId);
        return response;
    }

    private void requireAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (!isAdmin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only admin can manage doctor login accounts");
        }
    }

}
