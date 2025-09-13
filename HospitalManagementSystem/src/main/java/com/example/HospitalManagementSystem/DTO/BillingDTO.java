package com.example.HospitalManagementSystem.DTO;

import java.time.LocalDate;

public class BillingDTO {

    private Long id;
    private Long patientId;
    private Double amount;
    private String status;
    private LocalDate billingDate;

    public BillingDTO() 
    {
    	
    }

    public BillingDTO(Long id, Long patientId, Double amount, String status, LocalDate billingDate) {
        this.id = id;
        this.patientId = patientId;
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

	public Long getPatientId() {
		return patientId;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
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
