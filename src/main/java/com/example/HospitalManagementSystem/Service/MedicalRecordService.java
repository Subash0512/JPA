package com.example.HospitalManagementSystem.Service;

import com.example.HospitalManagementSystem.DTO.MedicalRecordDTO;
import com.example.HospitalManagementSystem.Exception.ResourceNotFoundException;
import com.example.HospitalManagementSystem.Model.Doctor;
import com.example.HospitalManagementSystem.Model.MedicalRecord;
import com.example.HospitalManagementSystem.Model.Patient;
import com.example.HospitalManagementSystem.Repsitory.AppointmentRepo;
import com.example.HospitalManagementSystem.Repsitory.DoctorRepo;
import com.example.HospitalManagementSystem.Repsitory.MedicalRecordRepo;
import com.example.HospitalManagementSystem.Repsitory.PatientRepo;

import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MedicalRecordService {

    private final MedicalRecordRepo medicalRecordRepo;
    private final PatientRepo patientRepo;
    private final DoctorRepo doctorRepo;
    private final AppointmentRepo appointmentRepo;
    private final NotificationService notificationService;

    public MedicalRecordService(
            MedicalRecordRepo medicalRecordRepo,
            PatientRepo patientRepo,
            DoctorRepo doctorRepo,
            AppointmentRepo appointmentRepo,
            NotificationService notificationService) {

        this.medicalRecordRepo = medicalRecordRepo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.appointmentRepo = appointmentRepo;
        this.notificationService = notificationService;
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication();
    }

    private String getCurrentUsername() {
        return getAuthentication().getName();
    }

    private String getCurrentRole() {
        return getAuthentication()
                .getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("");
    }
    public MedicalRecordDTO updateMedicalRecord(Long id, MedicalRecord updatedRecord) {

        if (id == null) {
            throw new IllegalArgumentException("Medical record ID is required");
        }

        if (updatedRecord == null) {
            throw new IllegalArgumentException("Medical record data is required");
        }

        MedicalRecord existingRecord = medicalRecordRepo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Medical record not found with id: " + id));

        Patient patient = existingRecord.getPatient();

        if (patient == null) {
            throw new ResourceNotFoundException(
                    "Patient not found for medical record id: " + id);
        }

        String role = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");

        if ("ROLE_ADMIN".equals(role)) {

            // Admin can edit any medical record

        } else if ("ROLE_DOCTOR".equals(role)) {

            // Doctor can edit only authorized patient's record
            checkDoctorPatientAccess(patient);

        } else if ("ROLE_PATIENT".equals(role)) {

            throw new AccessDeniedException(
                    "Patients are not allowed to edit medical records");

        } else {

            throw new AccessDeniedException(
                    "You are not authorized to edit medical records");
        }

        // Keep the original patient unchanged
        existingRecord.setDiagnosis(updatedRecord.getDiagnosis());
        existingRecord.setTreatment(updatedRecord.getTreatment());
        existingRecord.setRecordDate(updatedRecord.getRecordDate());

        MedicalRecord savedRecord = medicalRecordRepo.save(existingRecord);

        return toDTO(savedRecord);
    }
    // =========================================================
    // ADD MEDICAL RECORD
    // =========================================================

    public MedicalRecordDTO addMedicalRecord(
            Long patientId,
            MedicalRecord medicalRecord) {

        Patient patient =
                patientRepo.findById(patientId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Patient not found"
                                )
                        );

        String role = getCurrentRole();

        if (role.equals("ROLE_ADMIN")) {
            // Admin can add for any patient.
        }
        else if (role.equals("ROLE_DOCTOR")) {
            checkDoctorPatientAccess(patient);
        }
        else {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to create medical records"
            );
        }

        medicalRecord.setPatient(patient);

        MedicalRecord savedRecord =
                medicalRecordRepo.save(medicalRecord);

        if (notificationService != null
                && patient.getEmail() != null
                && !patient.getEmail().isEmpty()) {

            try {
                String details =
                        String.format(
                                "Dear %s, your medical record was updated:\nDiagnosis: %s\nTreatment: %s\nDate: %s",
                                patient.getName(),
                                savedRecord.getDiagnosis(),
                                savedRecord.getTreatment(),
                                savedRecord.getRecordDate()
                        );

                notificationService.sendMedicalRecordNotification(
                        patient.getEmail(),
                        details
                );

            } catch (Exception e) {
                System.err.println(
                        "Failed to send medical record notification: "
                                + e.getMessage()
                );
            }
        }

        return toDTO(savedRecord);
    }

    // =========================================================
    // GET MEDICAL RECORDS FOR CURRENT ROLE
    // =========================================================

    public List<MedicalRecordDTO> getMedicalRecords() {

        String role = getCurrentRole();

        // ADMIN → all records.
        if (role.equals("ROLE_ADMIN")) {
            return medicalRecordRepo
                    .findAll()
                    .stream()
                    .map(this::toDTO)
                    .toList();
        }

        // DOCTOR → records belonging to patients
        // the doctor is authorized to access.
        if (role.equals("ROLE_DOCTOR")) {

            Doctor doctor =
                    doctorRepo
                            .findByUserUsername(
                                    getCurrentUsername()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Doctor account not found"
                                    )
                            );

            var authorizedPatientIds =
                    appointmentRepo
                            .findByDoctor(doctor)
                            .stream()
                            .map(appointment ->
                                    appointment.getPatient().getId()
                            )
                            .collect(java.util.stream.Collectors.toSet());

            return medicalRecordRepo
                    .findAll()
                    .stream()
                    .filter(record ->
                            record.getPatient() != null
                            && authorizedPatientIds.contains(
                                    record.getPatient().getId()
                            )
                    )
                    .map(this::toDTO)
                    .toList();
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to view medical records"
        );
    }


    // =========================================================
    // GET MEDICAL RECORDS BY PATIENT
    // =========================================================

    public List<MedicalRecordDTO> getMedicalRecordsByPatient(
            Long patientId) {

        Patient patient =
                patientRepo.findById(patientId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Patient not found"
                                )
                        );

        String role = getCurrentRole();

        // ADMIN → can view all records for the selected patient.
        if (role.equals("ROLE_ADMIN")) {
            return medicalRecordRepo
                    .findByPatientId(patientId)
                    .stream()
                    .map(this::toDTO)
                    .toList();
        }

        // PATIENT → only own records.
        if (role.equals("ROLE_PATIENT")) {

            Patient loggedInPatient =
                    patientRepo
                            .findByUserUsername(
                                    getCurrentUsername()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Patient account not found"
                                    )
                            );

            if (!loggedInPatient.getId().equals(patientId)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not authorized to access another patient's medical records"
                );
            }

            return medicalRecordRepo
                    .findByPatientId(patientId)
                    .stream()
                    .map(this::toDTO)
                    .toList();
        }

        // DOCTOR → only authorized patients.
        if (role.equals("ROLE_DOCTOR")) {

            checkDoctorPatientAccess(patient);

            return medicalRecordRepo
                    .findByPatientId(patientId)
                    .stream()
                    .map(this::toDTO)
                    .toList();
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to access medical records"
        );
    }

    // =========================================================
    // GET CURRENT PATIENT MEDICAL RECORDS
    // =========================================================

    public List<MedicalRecordDTO> getMyMedicalRecords() {

        if (!getCurrentRole().equals("ROLE_PATIENT")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only patients can use this endpoint"
            );
        }

        Patient patient =
                patientRepo
                        .findByUserUsername(
                                getCurrentUsername()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Patient account not found"
                                )
                        );

        return medicalRecordRepo
                .findByPatientId(patient.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // =========================================================
    // GET ALL MEDICAL RECORDS
    // =========================================================

    public List<MedicalRecordDTO> getAllMedicalRecords() {

        if (!getCurrentRole().equals("ROLE_ADMIN")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only administrators can view all medical records"
            );
        }

        return medicalRecordRepo
                .findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // =========================================================
    // GET MEDICAL RECORD BY ID
    // =========================================================

    public Optional<MedicalRecordDTO> getMedicalRecordById(
            Long id) {

        Optional<MedicalRecord> recordOpt =
                medicalRecordRepo.findById(id);

        if (recordOpt.isEmpty()) {
            return Optional.empty();
        }

        MedicalRecord record =
                recordOpt.get();

        Patient patient =
                record.getPatient();

        String role = getCurrentRole();

        if (role.equals("ROLE_ADMIN")) {
            return Optional.of(toDTO(record));
        }

        if (role.equals("ROLE_PATIENT")) {

            Patient loggedInPatient =
                    patientRepo
                            .findByUserUsername(
                                    getCurrentUsername()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Patient account not found"
                                    )
                            );

            if (!loggedInPatient
                    .getId()
                    .equals(patient.getId())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not authorized to access this medical record"
                );
            }

            return Optional.of(toDTO(record));
        }

        if (role.equals("ROLE_DOCTOR")) {

            checkDoctorPatientAccess(patient);

            return Optional.of(toDTO(record));
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to access medical records"
        );
    }

    // =========================================================
    // CONVERT ENTITY → DTO
    // =========================================================

    private MedicalRecordDTO toDTO(
            MedicalRecord record) {

        Patient patient =
                record.getPatient();

        return new MedicalRecordDTO(
                record.getId(),
                patient != null ? patient.getId() : null,
                patient != null ? patient.getName() : null,
                record.getDiagnosis(),
                record.getTreatment(),
                record.getRecordDate()
        );
    }

    // =========================================================
    // CHECK DOCTOR → PATIENT ACCESS
    // =========================================================

    private void checkDoctorPatientAccess(
            Patient patient) {

        String username =
                getCurrentUsername();

        Doctor doctor =
                doctorRepo
                        .findByUserUsername(username)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Doctor account not found"
                                )
                        );

        boolean authorized =
                appointmentRepo
                        .findByDoctor(doctor)
                        .stream()
                        .anyMatch(appointment ->
                                appointment.getPatient()
                                        .getId()
                                        .equals(patient.getId())
                        );

        if (!authorized) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to access this patient's medical records"
            );
        }
    }
}
