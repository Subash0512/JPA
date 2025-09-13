package com.example.HospitalManagementSystem.DTO;

import java.time.LocalDateTime;

public class AppointmentDTO {
    private Long id;
    private PatientDTO patient;
    private DoctorDTO doctor;
    private LocalDateTime appointmentTime;
    private String status;

    public AppointmentDTO()
    {
    	
    }

    // Getters and Setters 
    
    public AppointmentDTO(Long id, PatientDTO patient, DoctorDTO doctor, LocalDateTime appointmentTime, String status) {
        this.id = id; 
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

	public PatientDTO getPatient() {
		return patient;
	}

	public void setPatient(PatientDTO patient) {
		this.patient = patient;
	}

	public DoctorDTO getDoctor() {
		return doctor;
	}

	public void setDoctor(DoctorDTO doctor) {
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
