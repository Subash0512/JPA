package com.example.HospitalManagementSystem.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id",nullable=true)
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id",nullable=true)
    private Doctor doctor;

    private LocalDateTime appointmentTime;
    private String status;

    // Constructors
    public Appointment() 
    {
    	
    }
    public Appointment(Long id, Patient patient, Doctor doctor, LocalDateTime appointmentTime, String status) 
    {
        this.id = id; 
        this.patient = patient; 
        this.doctor = doctor;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }
    public Appointment(Patient patient, Doctor doctor, LocalDateTime appointmentTime, String status) {
        this.patient = patient; 
        this.doctor = doctor; 
        this.appointmentTime = appointmentTime; 
        this.status = status;
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
	public Doctor getDoctor() {
		return doctor;
	}
	public void setDoctor(Doctor doctor) {
		this.doctor = doctor;
	}
	public LocalDateTime getAppointmentTime() {
		return appointmentTime;
	}
	public void setAppointmentTime(LocalDateTime appointmentTime) {
		this.appointmentTime = appointmentTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

    
}
