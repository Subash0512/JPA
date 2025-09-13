package com.example.HospitalManagementSystem.Controller;

import com.example.HospitalManagementSystem.Model.MedicalRecord;
import com.example.HospitalManagementSystem.Service.MedicalRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medicalrecords")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @PostMapping("/patient/{patientId}")
    public ResponseEntity<MedicalRecord> addMedicalRecord(
            @PathVariable Long patientId, @RequestBody MedicalRecord medicalRecord) {
        MedicalRecord savedRecord = medicalRecordService.addMedicalRecord(patientId, medicalRecord);
        return ResponseEntity.ok(savedRecord);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecord>> getMedicalRecordsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicalRecordService.getMedicalRecordsByPatient(patientId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecord> getMedicalRecordById(@PathVariable Long id) {
        return medicalRecordService.getMedicalRecordById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}