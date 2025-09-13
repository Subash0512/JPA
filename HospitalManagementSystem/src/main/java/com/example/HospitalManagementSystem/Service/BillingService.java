package com.example.HospitalManagementSystem.Service;

import com.example.HospitalManagementSystem.Model.Billing;
import com.example.HospitalManagementSystem.Model.Patient;
import com.example.HospitalManagementSystem.Repsitory.BillingRepo;
import com.example.HospitalManagementSystem.Repsitory.PatientRepo;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BillingService {

    private final BillingRepo billingRepo;
    private final PatientRepo patientRepo;
    private final NotificationService notificationService;

    public BillingService(BillingRepo billingRepo,
                          PatientRepo patientRepo,
                          NotificationService notificationService) {
        this.billingRepo = billingRepo;
        this.patientRepo = patientRepo;
        this.notificationService = notificationService;
    }

    public Billing generateBill(Long patientId, Billing billing) {
        Patient patient = patientRepo.findById(patientId).orElseThrow(
                () -> new RuntimeException("Patient not found"));

        billing.setPatient(patient);
        Billing savedBill = billingRepo.save(billing);

        if (notificationService != null && patient.getEmail() != null) {
            try {
                String details = String.format("Dear %s,\nYour bill has been generated.\nAmount: %.2f\nStatus: %s\nDate: %s",
                        patient.getName(),
                        savedBill.getAmount(),
                        savedBill.getStatus(),
                        savedBill.getBillingDate().toString());

                notificationService.sendBillingNotification(patient.getEmail(), details);
            } catch (Exception e) {
                System.err.println("Failed to send billing notification: " + e.getMessage());
            }
        }

        return savedBill;
    }

    public List<Billing> getBillingByPatient(Long patientId) {
        return billingRepo.findByPatientId(patientId);
    }

    public Optional<Billing> getBillById(Long id) {
        return billingRepo.findById(id);
    }

    public Billing updatePaymentStatus(Long billingId, String status) {
        Billing bill = billingRepo.findById(billingId).orElseThrow(
                () -> new RuntimeException("Billing record not found"));

        bill.setStatus(status);
        Billing updatedBill = billingRepo.save(bill);

        Patient patient = bill.getPatient();
        if (notificationService != null && patient.getEmail() != null) {
            try {
                String details = String.format("Dear %s,\nYour bill payment status has been updated to: %s",
                        patient.getName(), updatedBill.getStatus());

                notificationService.sendBillingNotification(patient.getEmail(), details);
            } catch (Exception e) {
                System.err.println("Failed to send billing status update email: " + e.getMessage());
            }
        }

        return updatedBill;
    }
}
