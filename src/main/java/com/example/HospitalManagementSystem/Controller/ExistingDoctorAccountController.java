package com.example.HospitalManagementSystem.Controller;

import com.example.HospitalManagementSystem.Model.Doctor;
import com.example.HospitalManagementSystem.Service.ExistingDoctorAccountService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/doctor-accounts")
public class ExistingDoctorAccountController {

    private final ExistingDoctorAccountService accountService;

    public ExistingDoctorAccountController(
            ExistingDoctorAccountService accountService) {

        this.accountService = accountService;
    }

    // =========================================================
    // CREATE LOGIN FOR EXISTING DOCTOR
    // =========================================================

    @PostMapping("/{doctorId}")
    public ResponseEntity<?> createDoctorLogin(
            @PathVariable Long doctorId,
            @RequestBody DoctorAccountRequest request) {

        Doctor doctor =
                accountService.createDoctorLogin(
                        doctorId,
                        request.getUsername(),
                        request.getPassword()
                );

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "message",
                "Doctor login account created successfully"
        );

        response.put("doctorId", doctor.getId());
        response.put("doctorName", doctor.getName());
        response.put("username", request.getUsername());
        response.put("role", "ROLE_DOCTOR");

        return ResponseEntity.ok(response);
    }



    // =========================================================
    // GET DOCTOR LOGIN STATUS
    // =========================================================

    @GetMapping("/{doctorId}/status")
    public ResponseEntity<?> getDoctorLoginStatus(
            @PathVariable Long doctorId) {

        Map<String, Object> response =
                accountService.getDoctorLoginStatus(doctorId);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // ENABLE / DISABLE DOCTOR LOGIN
    // =========================================================

    @PutMapping("/{doctorId}/status")
    public ResponseEntity<?> updateDoctorLoginStatus(
            @PathVariable Long doctorId,
            @RequestBody Map<String, Boolean> request) {

        boolean enabled = Boolean.TRUE.equals(request.get("enabled"));

        return ResponseEntity.ok(
                accountService.updateDoctorLoginStatus(doctorId, enabled));
    }

    // =========================================================
    // RESET DOCTOR PASSWORD
    // =========================================================

    @PutMapping("/{doctorId}/password")
    public ResponseEntity<?> resetDoctorPassword(
            @PathVariable Long doctorId,
            @RequestBody Map<String, String> request) {

        return ResponseEntity.ok(
                accountService.resetDoctorPassword(
                        doctorId, request.get("password")));
    }

    // =========================================================
    // REQUEST BODY
    // =========================================================

    public static class DoctorAccountRequest {

        private String username;
        private String password;

        public DoctorAccountRequest() {
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