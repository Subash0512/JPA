package com.example.HospitalManagementSystem.DTO;

import java.time.LocalDate;

public class MedicalRecordDTO {

    private Long id;
    private Long patientId;
    private String patientName;
    private String diagnosis;
    private String treatment;
    private LocalDate recordDate;

    public MedicalRecordDTO() {}

    public MedicalRecordDTO(Long id, Long patientId, String diagnosis, String treatment, LocalDate recordDate) {
        this(id, patientId, null, diagnosis, treatment, recordDate);
    }

    public MedicalRecordDTO(Long id, Long patientId, String patientName, String diagnosis, String treatment, LocalDate recordDate) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.recordDate = recordDate;
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

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getDiagnosis() {
		return diagnosis;
	}

	public void setDiagnosis(String diagnosis) {
		this.diagnosis = diagnosis;
	}

	public String getTreatment() {
		return treatment;
	}

	public void setTreatment(String treatment) {
		this.treatment = treatment;
	}

	public LocalDate getRecordDate() {
		return recordDate;
	}

	public void setRecordDate(LocalDate recordDate) {
		this.recordDate = recordDate;
	}

    
}

