package com.example.HospitalManagementSystem.Service;

import com.example.HospitalManagementSystem.Model.MedicalRecord;
import com.example.HospitalManagementSystem.Model.Patient;
import com.example.HospitalManagementSystem.Repsitory.MedicalRecordRepo;
import com.example.HospitalManagementSystem.Repsitory.PatientRepo;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MedicalRecordService {

    private final MedicalRecordRepo medicalRecordRepo;
    private final PatientRepo patientRepo;
    private final NotificationService notificationService;

    public MedicalRecordService(MedicalRecordRepo medicalRecordRepo,
                                PatientRepo patientRepo,
                                NotificationService notificationService) {
        this.medicalRecordRepo = medicalRecordRepo;
        this.patientRepo = patientRepo;
        this.notificationService = notificationService;
    }

    public MedicalRecord addMedicalRecord(Long patientId, MedicalRecord medicalRecord) {
        Patient patient = patientRepo.findById(patientId).orElseThrow(
                () -> new RuntimeException("Patient not found"));

        medicalRecord.setPatient(patient);
        MedicalRecord savedRecord = medicalRecordRepo.save(medicalRecord);

        if (notificationService != null && patient.getEmail() != null) {
            try {
                String details = String.format("Dear %s, your medical record was updated:\nDiagnosis: %s\nTreatment: %s\nDate: %s",
                        patient.getName(),
                        savedRecord.getDiagnosis(),
                        savedRecord.getTreatment(),
                        savedRecord.getRecordDate().toString());

                notificationService.sendMedicalRecordNotification(patient.getEmail(), details);
            } catch (Exception e) {
                System.err.println("Failed to send medical record notification: " + e.getMessage());
            }
        }

        return savedRecord;
    }

    public List<MedicalRecord> getMedicalRecordsByPatient(Long patientId) {
        return medicalRecordRepo.findByPatientId(patientId);
    }

    public Optional<MedicalRecord> getMedicalRecordById(Long id) {
        return medicalRecordRepo.findById(id);
    }
}
