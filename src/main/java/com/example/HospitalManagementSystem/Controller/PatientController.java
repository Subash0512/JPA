package com.example.HospitalManagementSystem.Controller;

import jakarta.validation.Valid;

import com.example.HospitalManagementSystem.Model.Patient;
import com.example.HospitalManagementSystem.Service.PatientService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
@CrossOrigin(origins = "*")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // =========================================================
    // GET CURRENT PATIENT
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<Patient> getMyProfile() {
        return ResponseEntity.ok(patientService.getMyProfile());
    }

    // =========================================================
    // UPDATE CURRENT PATIENT PROFILE
    // =========================================================

    @PutMapping("/me")
    public ResponseEntity<Patient> updateMyProfile(
            @Valid @RequestBody Patient updatedPatient) {

        return ResponseEntity.ok(
                patientService.updateMyProfile(updatedPatient)
        );
    }

    // =========================================================
    // GET ALL PATIENTS
    // =========================================================

    @GetMapping
    public List<Patient> getAllPatients() {

        return patientService.getAllPatients();
    }


    // =========================================================
    // GET PATIENT BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(
            @PathVariable Long id) {

        return patientService
                .getPatientById(id)
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity.notFound().build()
                );
    }


    // =========================================================
    // CREATE PATIENT
    // =========================================================
    @PostMapping
    public ResponseEntity<?> createPatient(
            @RequestBody PatientAccountRequest request) {

        Patient savedPatient =
                patientService.savePatient(
                        request.getPatient(),
                        request.getUsername(),
                        request.getPassword()
                );

        Map<String, Object> response = new HashMap<>();

        response.put(
                "message",
                "Patient and login account created successfully"
        );

        response.put(
                "patientId",
                savedPatient.getId()
        );

        response.put(
                "patientName",
                savedPatient.getName()
        );

        response.put(
                "username",
                request.getUsername()
        );

        response.put(
                "role",
                "ROLE_PATIENT"
        );

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // UPDATE PATIENT
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody Patient updatedPatient) {

        Patient savedPatient =
                patientService.updatePatient(
                        id,
                        updatedPatient
                );

        return ResponseEntity.ok(savedPatient);
    }


    // =========================================================
    // DELETE PATIENT
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(
            @PathVariable Long id) {

        patientService.deletePatient(id);

        return ResponseEntity.noContent().build();
    }
 // =========================================================
 // PATIENT + LOGIN REQUEST
 // =========================================================

 public static class PatientAccountRequest {

     private Patient patient;
     private String username;
     private String password;

     public PatientAccountRequest() {
     }

     public Patient getPatient() {
         return patient;
     }

     public void setPatient(Patient patient) {
         this.patient = patient;
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