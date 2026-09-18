package com.example.HospitalManagementSystem.Controller;

import com.example.HospitalManagementSystem.Model.User;
import com.example.HospitalManagementSystem.Repsitory.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/account")
public class UserAccountController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================
    // CURRENT ACCOUNT
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentAccount(
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> optionalUser =
                userRepository.findByUsername(authentication.getName());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User account not found"));
        }

        User user = optionalUser.get();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("enabled", user.isEnabled());

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // CHANGE CURRENT USER PASSWORD
    // =========================================================

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest request) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (request == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Request body is required"));
        }

        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        if (currentPassword == null || currentPassword.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Current password is required"));
        }

        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "New password is required"));
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "New password must be at least 6 characters"));
        }

        if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "New password and confirmation do not match"));
        }

        Optional<User> optionalUser =
                userRepository.findByUsername(authentication.getName());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User account not found"));
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(
                currentPassword,
                user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Current password is incorrect"));
        }

        if (passwordEncoder.matches(
                newPassword,
                user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "New password must be different from the current password"));
        }

        user.setPassword(
                passwordEncoder.encode(newPassword.trim())
        );

        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of("message", "Password changed successfully")
        );
    }

    // =========================================================
    // REQUEST DTO
    // =========================================================

    public static class ChangePasswordRequest {

        private String currentPassword;
        private String newPassword;
        private String confirmPassword;

        public ChangePasswordRequest() {
        }

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}
