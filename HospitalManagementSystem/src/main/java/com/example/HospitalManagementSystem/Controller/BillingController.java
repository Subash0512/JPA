package com.example.HospitalManagementSystem.Controller;

import com.example.HospitalManagementSystem.Model.Billing;
import com.example.HospitalManagementSystem.Service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/patient/{patientId}")
    public ResponseEntity<Billing> generateBill(
            @PathVariable Long patientId, @RequestBody Billing billing) {
        Billing savedBill = billingService.generateBill(patientId, billing);
        return ResponseEntity.ok(savedBill);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Billing>> getBillingByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(billingService.getBillingByPatient(patientId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Billing> getBillById(@PathVariable Long id) {
        return billingService.getBillById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Billing> updatePaymentStatus(@PathVariable Long id, @RequestParam String status) {
        Billing updatedBill = billingService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(updatedBill);
    }
}