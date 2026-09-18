package com.example.HospitalManagementSystem.Model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@JsonIgnoreProperties({
        "hibernateLazyInitializer",
        "handler"
})
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================================================
    // PATIENT
    // =========================================================
    /*
     * Patient is assigned by the backend for a
     * logged-in patient.
     */

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "patient_id",
            nullable = false
    )
    private Patient patient;


    // =========================================================
    // DOCTOR
    // =========================================================
    /*
     * Doctor is required for every appointment.
     */

    @NotNull(
            message = "Doctor is required"
    )
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "doctor_id",
            nullable = false
    )
    private Doctor doctor;


    // =========================================================
    // APPOINTMENT DATE & TIME
    // =========================================================
    /*
     * The value must be present.
     *
     * We intentionally do NOT use @Future here.
     *
     * Reason:
     * Completed and cancelled appointments can legitimately
     * have appointment times in the past.
     *
     * The future-date rule is handled in the service layer
     * when creating/rescheduling active appointments.
     */

    @NotNull(
            message = "Appointment date and time are required"
    )
    private LocalDateTime appointmentTime;


    // =========================================================
    // STATUS
    // =========================================================
    /*
     * Allowed statuses:
     *
     * Scheduled
     * Confirmed
     * Completed
     * Cancelled
     */

    @NotNull(
            message = "Appointment status is required"
    )
    @Pattern(
            regexp = "Scheduled|Confirmed|Completed|Cancelled",
            message =
                    "Status must be Scheduled, Confirmed, Completed or Cancelled"
    )
    private String status;


    // =========================================================
    // DEFAULT CONSTRUCTOR
    // =========================================================

    public Appointment() {
    }


    // =========================================================
    // PARAMETERIZED CONSTRUCTOR
    // =========================================================

    public Appointment(
            Long id,
            Patient patient,
            Doctor doctor,
            LocalDateTime appointmentTime,
            String status) {

        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }


    public Appointment(
            Patient patient,
            Doctor doctor,
            LocalDateTime appointmentTime,
            String status) {

        this.patient = patient;
        this.doctor = doctor;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }


    // =========================================================
    // GETTER / SETTER - ID
    // =========================================================

    public Long getId() {

        return id;
    }


    public void setId(Long id) {

        this.id = id;
    }


    // =========================================================
    // GETTER / SETTER - PATIENT
    // =========================================================

    public Patient getPatient() {

        return patient;
    }


    public void setPatient(Patient patient) {

        this.patient = patient;
    }


    // =========================================================
    // GETTER / SETTER - DOCTOR
    // =========================================================

    public Doctor getDoctor() {

        return doctor;
    }


    public void setDoctor(Doctor doctor) {

        this.doctor = doctor;
    }


    // =========================================================
    // GETTER / SETTER - APPOINTMENT TIME
    // =========================================================

    public LocalDateTime getAppointmentTime() {

        return appointmentTime;
    }


    public void setAppointmentTime(
            LocalDateTime appointmentTime) {

        this.appointmentTime = appointmentTime;
    }


    // =========================================================
    // GETTER / SETTER - STATUS
    // =========================================================

    public String getStatus() {

        return status;
    }


    public void setStatus(String status) {

        this.status = status;
    }
}