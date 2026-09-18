package com.example.HospitalManagementSystem.Controller;

import jakarta.validation.Valid;

import com.example.HospitalManagementSystem.Model.Billing;
import com.example.HospitalManagementSystem.Service.BillingService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/billing")
@CrossOrigin(origins = "*")
public class BillingController {

    private final BillingService billingService;


    public BillingController(
            BillingService billingService) {

        this.billingService =
                billingService;
    }


    // =========================================================
    // GENERATE BILL
    // =========================================================

    @PostMapping("/patient/{patientId}")
    public ResponseEntity<Billing> generateBill(

            @PathVariable Long patientId,

            @Valid
            @RequestBody
            Billing billing) {

        Billing savedBill =
                billingService.generateBill(
                        patientId,
                        billing
                );


        return ResponseEntity.ok(
                savedBill
        );
    }


    // =========================================================
    // GET ALL BILLING RECORDS
    //
    // ADMIN ONLY
    //
    // GET /billing
    // =========================================================

    @GetMapping
    public ResponseEntity<List<Billing>>
    getAllBilling() {

        return ResponseEntity.ok(
                billingService
                        .getAllBilling()
        );
    }


    // =========================================================
    // GET BILLING BY PATIENT
    // =========================================================

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Billing>>
    getBillingByPatient(
            @PathVariable Long patientId) {

        return ResponseEntity.ok(
                billingService
                        .getBillingByPatient(
                                patientId
                        )
        );
    }


    // =========================================================
    // GET MY BILLING RECORDS
    //
    // PATIENT ONLY
    //
    // GET /billing/me
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<List<Billing>>
    getMyBilling() {

        return ResponseEntity.ok(
                billingService.getMyBilling()
        );
    }


    // =========================================================
    // GET BILL BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<Billing>
    getBillById(
            @PathVariable Long id) {

        return billingService
                .getBillById(id)
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }


    // =========================================================
    // UPDATE PAYMENT STATUS
    // =========================================================

    @PutMapping("/{id}/status")
    public ResponseEntity<Billing>
    updatePaymentStatus(

            @PathVariable Long id,

            @RequestParam String status) {

        Billing updatedBill =
                billingService
                        .updatePaymentStatus(
                                id,
                                status
                        );


        return ResponseEntity.ok(
                updatedBill
        );
    }
}