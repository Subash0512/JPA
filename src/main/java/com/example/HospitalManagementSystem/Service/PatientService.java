package com.example.HospitalManagementSystem.Service;

import com.example.HospitalManagementSystem.Exception.ResourceNotFoundException;
import com.example.HospitalManagementSystem.Model.Appointment;
import com.example.HospitalManagementSystem.Model.Doctor;
import com.example.HospitalManagementSystem.Model.Patient;
import com.example.HospitalManagementSystem.Repsitory.AppointmentRepo;
import com.example.HospitalManagementSystem.Repsitory.DoctorRepo;
import com.example.HospitalManagementSystem.Repsitory.PatientRepo;
import com.example.HospitalManagementSystem.Model.User;
import com.example.HospitalManagementSystem.Repsitory.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@Transactional
public class PatientService {


	private final PatientRepo patientRepo;
	private final DoctorRepo doctorRepo;
	private final AppointmentRepo appointmentRepo;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public PatientService(
	        PatientRepo patientRepo,
	        DoctorRepo doctorRepo,
	        AppointmentRepo appointmentRepo,
	        UserRepository userRepository,
	        PasswordEncoder passwordEncoder) {

	    this.patientRepo = patientRepo;
	    this.doctorRepo = doctorRepo;
	    this.appointmentRepo = appointmentRepo;
	    this.userRepository = userRepository;
	    this.passwordEncoder = passwordEncoder;
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

        Authentication authentication =
                getAuthentication();


        if (
                authentication == null
                ||
                authentication.getName() == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated"
            );

        }


        return authentication.getName();

    }


    // =========================================================
    // CURRENT ROLE
    // =========================================================

    private String getCurrentRole() {

        Authentication authentication =
                getAuthentication();


        if (authentication == null) {

            return "";

        }


        return authentication
                .getAuthorities()
                .stream()
                .findFirst()
                .map(authority ->
                        authority.getAuthority()
                )
                .orElse("");

    }


    // =========================================================
    // CURRENT PATIENT PROFILE
    // =========================================================

    public Patient getMyProfile() {
        if (!getCurrentRole().equals("ROLE_PATIENT")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only patient accounts can use this endpoint");
        }

        return patientRepo.findByUserUsername(getCurrentUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Patient account not found"));
    }

    // =========================================================
    // UPDATE CURRENT PATIENT PROFILE
    // =========================================================

    public Patient updateMyProfile(Patient updatedPatient) {

        if (!getCurrentRole().equals("ROLE_PATIENT")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only patient accounts can update their own profile"
            );
        }

        Patient existing = patientRepo
                .findByUserUsername(getCurrentUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient account not found"
                ));

        if (updatedPatient == null
                || updatedPatient.getName() == null
                || updatedPatient.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Patient name is required");
        }

        if (updatedPatient.getAge() == null || updatedPatient.getAge() <= 0) {
            throw new IllegalArgumentException("Valid patient age is required");
        }

        existing.setName(updatedPatient.getName().trim());
        existing.setAge(updatedPatient.getAge());
        existing.setGender(updatedPatient.getGender());
        existing.setPhone(updatedPatient.getPhone());
        existing.setEmail(updatedPatient.getEmail());
        existing.setAddress(updatedPatient.getAddress());

        return patientRepo.save(existing);
    }

 // =========================================================
 // CREATE PATIENT + LOGIN ACCOUNT
 // =========================================================
 //
 // Only ADMIN can create a patient.
 //
 // Creates:
 // 1. Patient record
 // 2. User login account
 // 3. Links User <-> Patient
 //
 // =========================================================

 public Patient savePatient(
         Patient patient,
         String username,
         String password) {

     if (!getCurrentRole().equals("ROLE_ADMIN")) {
         throw new ResponseStatusException(
                 HttpStatus.FORBIDDEN,
                 "Only admin can create patients"
         );
     }

     if (patient == null) {
         throw new IllegalArgumentException(
                 "Patient details are required"
         );
     }

     if (patient.getName() == null
             || patient.getName().trim().isEmpty()) {

         throw new IllegalArgumentException(
                 "Patient name is required"
         );
     }

     if (patient.getAge() == null
             || patient.getAge() <= 0) {

         throw new IllegalArgumentException(
                 "Valid patient age is required"
         );
     }

     if (username == null
             || username.trim().isEmpty()) {

         throw new IllegalArgumentException(
                 "Patient username is required"
         );
     }

     if (password == null
             || password.length() < 6) {

         throw new IllegalArgumentException(
                 "Patient password must be at least 6 characters"
         );
     }

     username = username.trim();

     // Check username
     if (userRepository.existsByUsername(username)) {
         throw new IllegalArgumentException(
                 "Username already exists"
         );
     }

     // Check patient email
     if (patient.getEmail() != null
             && patientRepo.findByEmail(patient.getEmail()).isPresent()) {

         throw new IllegalArgumentException(
                 "Patient email already exists"
         );
     }

     // Check patient phone
     if (patient.getPhone() != null
             && patientRepo.findByPhone(patient.getPhone()).isPresent()) {

         throw new IllegalArgumentException(
                 "Patient phone number already exists"
         );
     }

     // =====================================================
     // CREATE USER ACCOUNT
     // =====================================================

     User user = new User();

     user.setUsername(username);
     user.setPassword(
             passwordEncoder.encode(password)
     );
     user.setRole("PATIENT");
     user.setEnabled(true);

     User savedUser = userRepository.save(user);

     // =====================================================
     // LINK USER TO PATIENT
     // =====================================================

     patient.setUser(savedUser);

     // =====================================================
     // SAVE PATIENT
     // =====================================================

     return patientRepo.save(patient);
 }

    // =========================================================
    // GET PATIENTS
    // =========================================================
    //
    // ADMIN  -> all patients
    // DOCTOR -> patients who have appointments with this doctor
    // PATIENT -> only the logged-in patient
    //
    // =========================================================

    public List<Patient> getAllPatients() {

        String role =
                getCurrentRole();


        // =====================================================
        // ADMIN
        // =====================================================

        if (
                role.equals("ROLE_ADMIN")
        ) {

            return patientRepo.findAll();

        }


        // =====================================================
        // PATIENT
        // =====================================================

        if (
                role.equals("ROLE_PATIENT")
        ) {

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


            List<Patient> result =
                    new ArrayList<>();


            result.add(
                    patient
            );


            return result;

        }


        // =====================================================
        // DOCTOR
        // =====================================================

        if (
                role.equals("ROLE_DOCTOR")
        ) {

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


            /*
             * LinkedHashMap removes duplicate patients
             * while preserving their original order.
             */

            Map<Long, Patient> uniquePatients =
                    new LinkedHashMap<>();


            List<Appointment> appointments =
                    appointmentRepo
                            .findByDoctor(
                                    doctor
                            );


            for (
                    Appointment appointment
                    : appointments
            ) {

                if (
                        appointment != null
                        &&
                        appointment.getPatient() != null
                        &&
                        appointment.getPatient().getId() != null
                ) {

                    Patient patient =
                            appointment.getPatient();


                    uniquePatients.put(
                            patient.getId(),
                            patient
                    );

                }

            }


            return new ArrayList<>(
                    uniquePatients.values()
            );

        }


        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to view patients"
        );

    }


    // =========================================================
    // GET PATIENT BY ID
    // =========================================================

    public Optional<Patient> getPatientById(
            Long id) {

        if (id == null) {

            return Optional.empty();

        }


        String role =
                getCurrentRole();


        // =====================================================
        // ADMIN -> ANY PATIENT
        // =====================================================

        if (
                role.equals("ROLE_ADMIN")
        ) {

            return patientRepo.findById(
                    id
            );

        }


        // =====================================================
        // PATIENT -> ONLY OWN RECORD
        // =====================================================

        if (
                role.equals("ROLE_PATIENT")
        ) {

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


            if (
                    !loggedInPatient
                            .getId()
                            .equals(id)
            ) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not authorized to view another patient's details"
                );

            }


            return Optional.of(
                    loggedInPatient
            );

        }


        // =====================================================
        // DOCTOR -> ONLY ASSIGNED PATIENT
        // =====================================================

        if (
                role.equals("ROLE_DOCTOR")
        ) {

            Patient patient =
                    patientRepo
                            .findById(id)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Patient not found"
                                    )
                            );


            checkDoctorPatientAccess(
                    patient
            );


            return Optional.of(
                    patient
            );

        }


        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to view patients"
        );

    }


    // =========================================================
    // UPDATE PATIENT
    // =========================================================
    //
    // Only ADMIN can update patient information.
    //
    // Doctors can VIEW assigned patients,
    // but they do not modify the patient master record here.
    //
    // =========================================================

    public Patient updatePatient(
            Long id,
            Patient updatedPatient) {


        if (
                !getCurrentRole().equals("ROLE_ADMIN")
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only admin can update patients"
            );

        }


        Patient existing =
                patientRepo
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Patient not found with ID: "
                                                + id
                                )
                        );


        if (
                updatedPatient.getName() == null
                ||
                updatedPatient.getName()
                        .trim()
                        .isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Patient name is required"
            );

        }


        if (
                updatedPatient.getAge() == null
                ||
                updatedPatient.getAge() <= 0
        ) {

            throw new IllegalArgumentException(
                    "Valid patient age is required"
            );

        }


        existing.setName(
                updatedPatient.getName()
        );

        existing.setAge(
                updatedPatient.getAge()
        );

        existing.setGender(
                updatedPatient.getGender()
        );

        existing.setPhone(
                updatedPatient.getPhone()
        );

        existing.setEmail(
                updatedPatient.getEmail()
        );

        existing.setAddress(
                updatedPatient.getAddress()
        );


        return patientRepo.save(
                existing
        );

    }


    // =========================================================
    // DELETE PATIENT
    // =========================================================

    public void deletePatient(
            Long id) {


        if (
                !getCurrentRole().equals("ROLE_ADMIN")
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only admin can delete patients"
            );

        }


        if (
                !patientRepo.existsById(id)
        ) {

            throw new ResourceNotFoundException(
                    "Patient not found with ID: "
                            + id
            );

        }


        patientRepo.deleteById(
                id
        );

    }


    // =========================================================
    // FIND BY EMAIL
    // =========================================================

    public Optional<Patient> findByEmail(
            String email) {

        return patientRepo.findByEmail(
                email
        );

    }


    // =========================================================
    // FIND BY PHONE
    // =========================================================

    public Optional<Patient> findByPhone(
            String phone) {

        return patientRepo.findByPhone(
                phone
        );

    }


    // =========================================================
    // DOCTOR -> PATIENT ACCESS
    // =========================================================
    //
    // Doctor can access a patient when there is an appointment
    // between that doctor and patient.
    //
    // =========================================================

    private void checkDoctorPatientAccess(
            Patient patient) {


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


        boolean authorized =
                appointmentRepo
                        .findByDoctor(
                                doctor
                        )
                        .stream()
                        .anyMatch(
                                appointment ->
                                        appointment != null
                                        &&
                                        appointment.getPatient() != null
                                        &&
                                        appointment
                                                .getPatient()
                                                .getId()
                                                .equals(
                                                        patient.getId()
                                                )
                        );


        if (!authorized) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to access this patient"
            );

        }

    }

}