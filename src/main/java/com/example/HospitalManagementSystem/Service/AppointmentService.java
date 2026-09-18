package com.example.HospitalManagementSystem.Service;

import com.example.HospitalManagementSystem.DTO.AppointmentDTO;
import com.example.HospitalManagementSystem.DTO.DoctorDTO;
import com.example.HospitalManagementSystem.DTO.PatientDTO;
import com.example.HospitalManagementSystem.Exception.ResourceNotFoundException;
import com.example.HospitalManagementSystem.Model.Appointment;
import com.example.HospitalManagementSystem.Model.Doctor;
import com.example.HospitalManagementSystem.Model.Patient;
import com.example.HospitalManagementSystem.Repsitory.AppointmentRepo;
import com.example.HospitalManagementSystem.Repsitory.DoctorRepo;
import com.example.HospitalManagementSystem.Repsitory.PatientRepo;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepo appointmentRepo;

    private final PatientRepo patientRepo;

    private final DoctorRepo doctorRepo;

    private final NotificationService notificationService;

    @Autowired
    public AppointmentService(
            AppointmentRepo appointmentRepo,
            PatientRepo patientRepo,
            DoctorRepo doctorRepo,
            NotificationService notificationService) {

        this.appointmentRepo = appointmentRepo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.notificationService = notificationService;
    }

    // =========================================================
    // AUTHENTICATION
    // =========================================================

    private Authentication getAuthentication() {

        return SecurityContextHolder
                .getContext()
                .getAuthentication();
    }

    // =========================================================
    // CURRENT USERNAME
    // =========================================================

    private String getCurrentUsername() {

        return getAuthentication()
                .getName();
    }

    // =========================================================
    // CURRENT ROLE
    // =========================================================

    private String getCurrentRole() {

        return getAuthentication()
                .getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("");
    }

    // =========================================================
    // CREATE APPOINTMENT
    // =========================================================

    public Appointment createAppointment(
            Appointment appointment) {

        // =====================================================
        // VALID APPOINTMENT DATE/TIME
        // =====================================================

        if (appointment.getAppointmentTime() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Appointment date and time are required"
            );
        }

        if (!appointment.getAppointmentTime().isAfter(LocalDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Appointment must be scheduled for a future date and time"
            );
        }

        // =====================================================
        // VALID DOCTOR
        // =====================================================

        if (appointment.getDoctor() == null
                || appointment.getDoctor().getId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Doctor is required"
            );
        }

        Long doctorId =
                appointment
                        .getDoctor()
                        .getId();

        Doctor doctor =
                doctorRepo.findById(doctorId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Doctor not found with ID: "
                                                + doctorId
                                )
                        );

        String role = getCurrentRole();

        String username = getCurrentUsername();

        Patient patient;

        // =====================================================
        // PATIENT
        // =====================================================

        if (role.equals("ROLE_PATIENT")) {

            patient =
                    patientRepo
                            .findByUserUsername(username)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Patient account not found"
                                    )
                            );
        }

        // =====================================================
        // DOCTOR / ADMIN
        // =====================================================

        else if (
                role.equals("ROLE_DOCTOR")
                        ||
                role.equals("ROLE_ADMIN")
        ) {

            if (
                    appointment.getPatient() == null
                            ||
                    appointment.getPatient().getId() == null
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Patient is required"
                );
            }

            Long patientId =
                    appointment
                            .getPatient()
                            .getId();

            patient =
                    patientRepo.findById(patientId)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Patient not found with ID: "
                                                    + patientId
                                    )
                            );
        }

        // =====================================================
        // OTHER ROLE
        // =====================================================

        else {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to create appointments"
            );
        }

        appointment.setPatient(patient);

        appointment.setDoctor(doctor);

        // =====================================================
        // NEW APPOINTMENTS ALWAYS START AS SCHEDULED
        // =====================================================

        appointment.setStatus("Scheduled");

        // =====================================================
        // DOCTOR DOUBLE-BOOKING
        // =====================================================

        boolean doctorAlreadyBooked =
                appointmentRepo
                        .existsByDoctorAndAppointmentTimeAndStatusNot(
                                doctor,
                                appointment.getAppointmentTime(),
                                "Cancelled"
                        );

        if (doctorAlreadyBooked) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Doctor is already booked for this time"
            );
        }

        // =====================================================
        // PATIENT DOUBLE-BOOKING
        // =====================================================

        boolean patientAlreadyBooked =
                appointmentRepo
                        .existsByPatientAndAppointmentTimeAndStatusNot(
                                patient,
                                appointment.getAppointmentTime(),
                                "Cancelled"
                        );

        if (patientAlreadyBooked) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You already have an appointment at this time"
            );
        }

        // =====================================================
        // SAVE
        // =====================================================

        Appointment saved =
                appointmentRepo.save(appointment);

        // =====================================================
        // CONFIRMATION EMAIL
        // =====================================================

        if (
                notificationService != null
                        &&
                patient.getEmail() != null
                        &&
                !patient.getEmail().isEmpty()
        ) {

            String details =
                    String.format(
                            "Dear %s, your appointment is scheduled on %s with Dr. %s.",
                            patient.getName(),
                            saved.getAppointmentTime(),
                            doctor.getName()
                    );

            try {

                notificationService
                        .sendAppointmentConfirmation(
                                patient.getEmail(),
                                details
                        );

            } catch (Exception e) {

                System.err.println(
                        "Failed to send email notification: "
                                + e.getMessage()
                );
            }
        }

        return saved;
    }

    // =========================================================
    // UPDATE APPOINTMENT
    // =========================================================

    public Appointment saveAppointment(
            Appointment appointment) {

        Appointment existing =
                appointmentRepo.findById(
                        appointment.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Appointment not found with ID: "
                                        + appointment.getId()
                        )
                );

        // =====================================================
        // ACCESS CHECK
        // =====================================================

        checkAppointmentAccess(existing);

        String role = getCurrentRole();

        String oldStatus = existing.getStatus();

        String newStatus = appointment.getStatus();

        // =====================================================
        // VALID APPOINTMENT DATE/TIME
        // =====================================================

        if (appointment.getAppointmentTime() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Appointment date and time are required"
            );
        }

        // =====================================================
        // VALID STATUS
        // =====================================================

        validateStatus(newStatus);

        // =====================================================
        // STATUS TRANSITION
        // =====================================================

        validateStatusTransition(
                oldStatus,
                newStatus,
                role
        );

        // =====================================================
        // TERMINAL STATUS
        // =====================================================

        if (
                isTerminalStatus(oldStatus)
                        &&
                !oldStatus.equals(newStatus)
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This appointment can no longer be modified"
            );
        }

        // =====================================================
        // FUTURE DATE VALIDATION
        // =====================================================
        //
        // Scheduled / Confirmed appointments must remain
        // in the future.
        //
        // Completed / Cancelled appointments are allowed
        // to contain a past date/time.
        // =====================================================

        if (
                !isTerminalStatus(newStatus)
                        &&
                !appointment.getAppointmentTime()
                        .isAfter(LocalDateTime.now())
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Scheduled or confirmed appointments must have a future date and time"
            );
        }

        String oldTime =
                existing
                        .getAppointmentTime()
                        .toString();

        // =====================================================
        // PRESERVE OWNERSHIP
        // =====================================================

        appointment.setPatient(
                existing.getPatient()
        );

        appointment.setDoctor(
                existing.getDoctor()
        );

        // =====================================================
        // DOCTOR DOUBLE-BOOKING
        // =====================================================

        boolean doctorAlreadyBooked =
                appointmentRepo
                        .existsByDoctorAndAppointmentTimeAndStatusNotAndIdNot(
                                existing.getDoctor(),
                                appointment.getAppointmentTime(),
                                "Cancelled",
                                existing.getId()
                        );

        if (doctorAlreadyBooked) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Doctor is already booked for this time"
            );
        }

        // =====================================================
        // PATIENT DOUBLE-BOOKING
        // =====================================================

        boolean patientAlreadyBooked =
                appointmentRepo
                        .existsByPatientAndAppointmentTimeAndStatusNotAndIdNot(
                                existing.getPatient(),
                                appointment.getAppointmentTime(),
                                "Cancelled",
                                existing.getId()
                        );

        if (patientAlreadyBooked) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Patient already has an appointment at this time"
            );
        }

        // =====================================================
        // SAVE
        // =====================================================

        Appointment saved =
                appointmentRepo.save(appointment);

        // =====================================================
        // RESCHEDULE EMAIL
        // =====================================================

        if (
                notificationService != null
                        &&
                saved.getPatient().getEmail() != null
                        &&
                !oldTime.equals(
                        saved.getAppointmentTime().toString()
                )
        ) {

            try {

                notificationService
                        .sendRescheduleNotification(
                                saved.getPatient().getEmail(),
                                saved.getPatient().getName(),
                                saved.getDoctor().getName(),
                                oldTime,
                                saved.getAppointmentTime()
                                        .toString()
                        );

            } catch (Exception e) {

                System.err.println(
                        "Failed to send reschedule email: "
                                + e.getMessage()
                );
            }
        }

        return saved;
    }

    // =========================================================
    // VALIDATE STATUS
    // =========================================================

    private void validateStatus(
            String status) {

        if (
                status == null
                        ||
                !(
                        status.equals("Scheduled")
                                ||
                        status.equals("Confirmed")
                                ||
                        status.equals("Completed")
                                ||
                        status.equals("Cancelled")
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid appointment status"
            );
        }
    }

    // =========================================================
    // VALIDATE STATUS TRANSITION
    // =========================================================

    private void validateStatusTransition(
            String oldStatus,
            String newStatus,
            String role) {

        // Same status is allowed.
        if (oldStatus.equals(newStatus)) {

            return;
        }

        // =====================================================
        // PATIENT
        // =====================================================

        if (role.equals("ROLE_PATIENT")) {

            if (
                    newStatus.equals("Cancelled")
                            &&
                    (
                            oldStatus.equals("Scheduled")
                                    ||
                            oldStatus.equals("Confirmed")
                    )
            ) {

                return;
            }

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Patients can only cancel their own scheduled or confirmed appointments"
            );
        }

        // =====================================================
        // DOCTOR
        // =====================================================

        if (role.equals("ROLE_DOCTOR")) {

            if (
                    oldStatus.equals("Scheduled")
                            &&
                    (
                            newStatus.equals("Confirmed")
                                    ||
                            newStatus.equals("Cancelled")
                    )
            ) {

                return;
            }

            if (
                    oldStatus.equals("Confirmed")
                            &&
                    (
                            newStatus.equals("Completed")
                                    ||
                            newStatus.equals("Cancelled")
                    )
            ) {

                return;
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid appointment status transition for doctor"
            );
        }

        // =====================================================
        // ADMIN
        // =====================================================

        if (role.equals("ROLE_ADMIN")) {

            if (
                    oldStatus.equals("Scheduled")
                            &&
                    (
                            newStatus.equals("Confirmed")
                                    ||
                            newStatus.equals("Cancelled")
                    )
            ) {

                return;
            }

            if (
                    oldStatus.equals("Confirmed")
                            &&
                    (
                            newStatus.equals("Completed")
                                    ||
                            newStatus.equals("Cancelled")
                    )
            ) {

                return;
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid appointment status transition"
            );
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to change appointment status"
        );
    }

    // =========================================================
    // TERMINAL STATUS
    // =========================================================

    private boolean isTerminalStatus(
            String status) {

        return "Completed".equals(status)
                ||
                "Cancelled".equals(status);
    }

    // =========================================================
    // GET ALL APPOINTMENTS
    // =========================================================

    public List<AppointmentDTO> getAllAppointments() {

        String role = getCurrentRole();

        String username = getCurrentUsername();

        List<Appointment> appointments;

        // =====================================================
        // ADMIN
        // =====================================================

        if (role.equals("ROLE_ADMIN")) {

            appointments =
                    appointmentRepo.findAll();
        }

        // =====================================================
        // DOCTOR
        // =====================================================

        else if (role.equals("ROLE_DOCTOR")) {

            Doctor doctor =
                    doctorRepo
                            .findByUserUsername(username)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Doctor account not found"
                                    )
                            );

            appointments =
                    appointmentRepo.findByDoctor(doctor);
        }

        // =====================================================
        // PATIENT
        // =====================================================

        else if (role.equals("ROLE_PATIENT")) {

            Patient patient =
                    patientRepo
                            .findByUserUsername(username)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Patient account not found"
                                    )
                            );

            appointments =
                    appointmentRepo.findByPatient(patient);
        }

        else {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to view appointments"
            );
        }

        return appointments
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET APPOINTMENT BY ID
    // =========================================================

    public Optional<AppointmentDTO>
    getAppointmentById(Long id) {

        Optional<Appointment> appointmentOpt =
                appointmentRepo.findById(id);

        if (appointmentOpt.isEmpty()) {

            return Optional.empty();
        }

        Appointment appointment =
                appointmentOpt.get();

        checkAppointmentAccess(appointment);

        return Optional.of(
                convertToDTO(appointment)
        );
    }

    // =========================================================
    // DELETE APPOINTMENT
    // =========================================================

    public void deleteAppointment(
            Long id) {

        Appointment existing =
                appointmentRepo.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Appointment not found with ID: "
                                                + id
                                )
                        );

        checkAppointmentAccess(existing);

        String cancelTime =
                existing
                        .getAppointmentTime()
                        .toString();

        Patient patient =
                existing.getPatient();

        Doctor doctor =
                existing.getDoctor();

        appointmentRepo.deleteById(id);

        if (
                notificationService != null
                        &&
                patient.getEmail() != null
        ) {

            try {

                notificationService
                        .sendCancellationNotification(
                                patient.getEmail(),
                                patient.getName(),
                                doctor.getName(),
                                cancelTime
                        );

            } catch (Exception e) {

                System.err.println(
                        "Failed to send cancellation email: "
                                + e.getMessage()
                );
            }
        }
    }

    // =========================================================
    // ACCESS CHECK
    // =========================================================

    private void checkAppointmentAccess(
            Appointment appointment) {

        String role = getCurrentRole();

        String username = getCurrentUsername();

        // =====================================================
        // ADMIN
        // =====================================================

        if (role.equals("ROLE_ADMIN")) {

            return;
        }

        // =====================================================
        // DOCTOR
        // =====================================================

        if (role.equals("ROLE_DOCTOR")) {

            Doctor doctor =
                    doctorRepo
                            .findByUserUsername(username)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Doctor account not found"
                                    )
                            );

            if (
                    !appointment
                            .getDoctor()
                            .getId()
                            .equals(
                                    doctor.getId()
                            )
            ) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not authorized to access this appointment"
                );
            }

            return;
        }

        // =====================================================
        // PATIENT
        // =====================================================

        if (role.equals("ROLE_PATIENT")) {

            Patient patient =
                    patientRepo
                            .findByUserUsername(username)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Patient account not found"
                                    )
                            );

            if (
                    !appointment
                            .getPatient()
                            .getId()
                            .equals(
                                    patient.getId()
                            )
            ) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not authorized to access this appointment"
                );
            }

            return;
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to access appointments"
        );
    }

    // =========================================================
    // CONVERT TO DTO
    // =========================================================

    public AppointmentDTO convertToDTO(
            Appointment appointment) {

        Patient patient =
                appointment.getPatient();

        Doctor doctor =
                appointment.getDoctor();

        PatientDTO patientDTO =
                new PatientDTO(
                        patient.getId(),
                        patient.getName(),
                        patient.getAge(),
                        patient.getGender(),
                        patient.getPhone(),
                        patient.getEmail(),
                        patient.getAddress()
                );

        DoctorDTO doctorDTO =
                new DoctorDTO(
                        doctor.getId(),
                        doctor.getName(),
                        doctor.getSpecialization(),
                        doctor.getEmail(),
                        doctor.getPhone()
                );

        return new AppointmentDTO(
                appointment.getId(),
                patientDTO,
                doctorDTO,
                appointment.getAppointmentTime(),
                appointment.getStatus()
        );
    }
 // =========================================================
 // GET CURRENT PATIENT APPOINTMENTS
 // =========================================================

 public List<AppointmentDTO> getMyAppointments() {

     String role = getCurrentRole();

     if (!role.equals("ROLE_PATIENT")) {

         throw new ResponseStatusException(
                 HttpStatus.FORBIDDEN,
                 "Only patients can use this endpoint"
         );
     }

     String username =
             getCurrentUsername();

     Patient patient =
             patientRepo
                     .findByUserUsername(username)
                     .orElseThrow(() ->
                             new ResourceNotFoundException(
                                     "Patient account not found"
                             )
                     );

     return appointmentRepo
             .findByPatient(patient)
             .stream()
             .map(this::convertToDTO)
             .collect(Collectors.toList());
 }
}