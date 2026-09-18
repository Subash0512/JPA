package com.example.HospitalManagementSystem.Service;

import com.example.HospitalManagementSystem.Exception.ResourceNotFoundException;
import com.example.HospitalManagementSystem.Model.Billing;
import com.example.HospitalManagementSystem.Model.Patient;
import com.example.HospitalManagementSystem.Repsitory.BillingRepo;
import com.example.HospitalManagementSystem.Repsitory.PatientRepo;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BillingService {

    private final BillingRepo billingRepo;
    private final PatientRepo patientRepo;
    private final NotificationService notificationService;


    public BillingService(
            BillingRepo billingRepo,
            PatientRepo patientRepo,
            NotificationService notificationService) {

        this.billingRepo = billingRepo;
        this.patientRepo = patientRepo;
        this.notificationService = notificationService;
    }


    // =========================================================
    // CURRENT AUTHENTICATION
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

        if (authentication == null) {
            return "";
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
                        authority.getAuthority())
                .orElse("");
    }


    // =========================================================
    // GENERATE BILL
    // =========================================================

    public Billing generateBill(
            Long patientId,
            Billing billing) {

        String role =
                getCurrentRole();


        // =====================================================
        // ONLY ADMIN CAN GENERATE BILL
        // =====================================================

        if (!role.equals("ROLE_ADMIN")) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to generate bills"
            );
        }


        Patient patient =
                patientRepo.findById(patientId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Patient not found with ID: "
                                                + patientId
                                )
                        );


        billing.setPatient(patient);


        Billing savedBill =
                billingRepo.save(billing);


        // =====================================================
        // SEND BILLING EMAIL
        // =====================================================

        if (notificationService != null
                && patient.getEmail() != null
                && !patient.getEmail().isEmpty()) {

            try {

                String details =
                        String.format(
                                "Dear %s,\nYour bill has been generated.\nAmount: %.2f\nStatus: %s\nDate: %s",
                                patient.getName(),
                                savedBill.getAmount(),
                                savedBill.getStatus(),
                                savedBill.getBillingDate()
                        );


                notificationService
                        .sendBillingNotification(
                                patient.getEmail(),
                                details
                        );

            } catch (Exception e) {

                System.err.println(
                        "Failed to send billing notification: "
                                + e.getMessage()
                );
            }
        }


        return savedBill;
    }


    // =========================================================
    // GET ALL BILLING RECORDS
    //
    // ADMIN ONLY
    //
    // GET /billing
    // =========================================================

    public List<Billing> getAllBilling() {

        String role =
                getCurrentRole();


        if (!role.equals("ROLE_ADMIN")) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to access all billing records"
            );
        }


        return billingRepo.findAll();
    }


    // =========================================================
    // GET BILLING BY PATIENT
    // =========================================================

    public List<Billing> getBillingByPatient(
            Long patientId) {

        String role =
                getCurrentRole();


        // =====================================================
        // ADMIN → ANY PATIENT
        // =====================================================

        if (role.equals("ROLE_ADMIN")) {

            return billingRepo
                    .findByPatientId(patientId);
        }


        // =====================================================
        // PATIENT → ONLY OWN RECORDS
        // =====================================================

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


            if (
                    !loggedInPatient
                            .getId()
                            .equals(patientId)
            ) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not authorized to access another patient's billing records"
                );
            }


            return billingRepo
                    .findByPatientId(patientId);
        }


        // =====================================================
        // OTHER ROLES
        // =====================================================

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to access billing records"
        );
    }


    // =========================================================
    // GET MY BILLING RECORDS
    //
    // PATIENT ONLY
    //
    // GET /billing/me
    // =========================================================

    public List<Billing> getMyBilling() {

        String role =
                getCurrentRole();

        if (!role.equals("ROLE_PATIENT")) {

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

        return billingRepo.findByPatientId(
                patient.getId()
        );
    }


    // =========================================================
    // GET BILL BY ID
    // =========================================================

    public Optional<Billing> getBillById(
            Long id) {

        Optional<Billing> billOpt =
                billingRepo.findById(id);


        if (billOpt.isEmpty()) {

            return Optional.empty();
        }


        Billing bill =
                billOpt.get();


        Patient patient =
                bill.getPatient();


        String role =
                getCurrentRole();


        // =====================================================
        // ADMIN
        // =====================================================

        if (role.equals("ROLE_ADMIN")) {

            return Optional.of(bill);
        }


        // =====================================================
        // PATIENT
        // =====================================================

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


            if (
                    !loggedInPatient
                            .getId()
                            .equals(
                                    patient.getId()
                            )
            ) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not authorized to access this billing record"
                );
            }


            return Optional.of(bill);
        }


        // =====================================================
        // OTHER ROLES
        // =====================================================

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to access billing records"
        );
    }


    // =========================================================
    // UPDATE PAYMENT STATUS
    // =========================================================

    public Billing updatePaymentStatus(
            Long billingId,
            String status) {

        String role =
                getCurrentRole();


        // =====================================================
        // ONLY ADMIN
        // =====================================================

        if (!role.equals("ROLE_ADMIN")) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to update payment status"
            );
        }


        Billing bill =
                billingRepo.findById(billingId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Billing record not found"
                                )
                        );


        // =====================================================
        // VALID PAYMENT STATUS
        // =====================================================

        if (status == null ||
                (!status.equalsIgnoreCase("Paid")
                        && !status.equalsIgnoreCase("Pending"))) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be either Paid or Pending"
            );
        }


        // Store a consistent value regardless of input casing.
        bill.setStatus(
                status.equalsIgnoreCase("Paid")
                        ? "Paid"
                        : "Pending"
        );


        Billing updatedBill =
                billingRepo.save(bill);


        Patient patient =
                bill.getPatient();


        // =====================================================
        // SEND STATUS UPDATE EMAIL
        // =====================================================

        if (notificationService != null
                && patient.getEmail() != null
                && !patient.getEmail().isEmpty()) {

            try {

                String details =
                        String.format(
                                "Dear %s,\nYour bill payment status has been updated to: %s",
                                patient.getName(),
                                updatedBill.getStatus()
                        );


                notificationService
                        .sendBillingNotification(
                                patient.getEmail(),
                                details
                        );

            } catch (Exception e) {

                System.err.println(
                        "Failed to send billing status update email: "
                                + e.getMessage()
                );
            }
        }


        return updatedBill;
    }
}