package com.example.HospitalManagementSystem.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    // =========================================================
    // AUTHENTICATION PAGES
    // =========================================================

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "reset-password";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard";
    }

    // =========================================================
    // FRONTEND PAGE ROUTES
    // =========================================================

    @GetMapping("/ui/patients")
    public String patientsPage() {
        return "patients/patients";
    }

    @GetMapping("/ui/doctors")
    public String doctorsPage() {
        return "doctors/doctors";
    }

    @GetMapping("/ui/appointments")
    public String appointmentsPage() {
        return "appointments/appointments";
    }

    @GetMapping("/ui/medical-records")
    public String medicalRecordsPage() {
        return "medical-records/medical-records";
    }

    @GetMapping("/ui/billing")
    public String billingPage() {
        return "billing/billing";
    }

    @GetMapping("/ui/medical-record-form")
    public String medicalRecordFormPage() {
        return "medical-records/medical-record-form";
    }

    @GetMapping("/ui/patient-form")
    public String patientFormPage() {
        return "patients/patient-form";
    }

    @GetMapping("/ui/patient-profile")
    public String patientProfilePage() {
        return "patients/patient-profile";
    }

    @GetMapping("/ui/patient-details")
    public String patientDetailsPage() {
        return "patients/patient-details";
    }

    @GetMapping("/ui/appointment-form")
    public String appointmentFormPage() {
        return "appointments/appointment-form";
    }

    // =========================================================
    // BILLING FORM
    // =========================================================

    @GetMapping("/ui/bill-form")
    public String billFormPage() {
        return "billing/bill-form";
    }

    // =========================================================
    // REPORTS
    // =========================================================

    @GetMapping("/ui/reports")
    public String reportsPage() {
        return "reports/reports";
    }

    // =========================================================
    // STAFF
    // =========================================================

    @GetMapping("/ui/staff")
    public String staffPage() {
        return "staff/staff";
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    @GetMapping("/ui/settings")
    public String settingsPage() {
        return "settings/settings";
    }

    // =========================================================
    // DOCTOR DETAILS
    // =========================================================

    @GetMapping("/ui/doctor-details")
    public String doctorDetailsPage() {
        return "doctors/doctor-details";
    }

    // =========================================================
    // NOTIFICATIONS
    // =========================================================

    @GetMapping("/ui/notifications")
    public String notificationsPage() {
        return "notifications/notifications";
    }

    // =========================================================
    // PATIENT APPOINTMENT BOOKING
    // =========================================================

    @GetMapping("/book-appointment")
    public String bookAppointmentPage() {
        return "book-appointment";
    }
}