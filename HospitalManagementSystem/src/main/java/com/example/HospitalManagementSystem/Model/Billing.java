package com.example.HospitalManagementSystem.Model;

import jakarta.persistence.*;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    
    private Patient patient;
    private Double amount;
    private String status; // "Paid or Pending"
    private LocalDate billingDate;

    public Billing()
    {
    	
    }

    public Billing(Long id, Patient patient, Double amount, String status, LocalDate billingDate) {
		super();
		this.id = id;
		this.patient = patient;
		this.amount = amount;
		this.status = status;
		this.billingDate = billingDate;
	}

	public Billing(Patient patient, Double amount, String status, LocalDate billingDate) {
    	super();
        this.patient = patient;
        this.amount = amount;
        this.status = status;
        this.billingDate = billingDate;
    }

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
