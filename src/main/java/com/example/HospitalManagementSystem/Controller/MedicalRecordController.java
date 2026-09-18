package com.example.HospitalManagementSystem.Controller;

import jakarta.validation.Valid;

import com.example.HospitalManagementSystem.DTO.MedicalRecordDTO;
import com.example.HospitalManagementSystem.Model.MedicalRecord;
import com.example.HospitalManagementSystem.Service.MedicalRecordService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medicalrecords")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(
            MedicalRecordService medicalRecordService) {

        this.medicalRecordService =
                medicalRecordService;
    }

    // =========================================================
    // ADD MEDICAL RECORD
    // =========================================================

    @PostMapping("/patient/{patientId}")
    public ResponseEntity<MedicalRecordDTO> addMedicalRecord(
            @PathVariable Long patientId,
            @Valid @RequestBody MedicalRecord medicalRecord) {

        MedicalRecordDTO savedRecord =
                medicalRecordService.addMedicalRecord(
                        patientId,
                        medicalRecord
                );

        return ResponseEntity.ok(savedRecord);
    }

    // =========================================================
    // GET ALL MEDICAL RECORDS
    // ADMIN / DOCTOR
    // =========================================================

    @GetMapping
    public ResponseEntity<List<MedicalRecordDTO>>
    getAllMedicalRecords() {

        return ResponseEntity.ok(
                medicalRecordService.getMedicalRecords()
        );
    }

    // =========================================================
    // GET MEDICAL RECORDS BY PATIENT
    // =========================================================

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecordDTO>>
    getMedicalRecordsByPatient(
            @PathVariable Long patientId) {

        return ResponseEntity.ok(
                medicalRecordService
                        .getMedicalRecordsByPatient(
                                patientId
                        )
        );
    }

    // =========================================================
    // GET CURRENT PATIENT MEDICAL RECORDS
    // PATIENT ONLY
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<List<MedicalRecordDTO>>
    getMyMedicalRecords() {

        return ResponseEntity.ok(
                medicalRecordService
                        .getMyMedicalRecords()
        );
    }

    // =========================================================
    // GET MEDICAL RECORD BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecordDTO>
    getMedicalRecordById(
            @PathVariable Long id) {

        return medicalRecordService
                .getMedicalRecordById(id)
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }
    @PutMapping("/{id}")
    public ResponseEntity<MedicalRecordDTO> updateMedicalRecord(
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecord updatedRecord) {

        return ResponseEntity.ok(
                medicalRecordService.updateMedicalRecord(
                        id,
                        updatedRecord
                )
        );
    }
}
