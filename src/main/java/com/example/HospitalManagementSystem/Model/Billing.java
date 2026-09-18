package com.example.HospitalManagementSystem.Model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private Double amount;

    @NotNull(message = "Payment status is required")
    @Pattern(
            regexp = "Paid|Pending",
            message = "Status must be either Paid or Pending"
    )
    private String status;

    @NotNull(message = "Billing date is required")
    @PastOrPresent(message = "Billing date cannot be in the future")
    private LocalDate billingDate;

    // Default Constructor
    public Billing() {
    }

    // Parameterized Constructor
    public Billing(Long id,
                   Patient patient,
                   Double amount,
                   String status,
                   LocalDate billingDate) {

        this.id = id;
        this.patient = patient;
        this.amount = amount;
        this.status = status;
        this.billingDate = billingDate;
    }

    public Billing(Patient patient,
                   Double amount,
                   String status,
                   LocalDate billingDate) {

        this.patient = patient;
        this.amount = amount;
        this.status = status;
        this.billingDate = billingDate;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(LocalDate billingDate) {
        this.billingDate = billingDate;
    }
}