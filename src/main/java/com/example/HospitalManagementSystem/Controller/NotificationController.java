package com.example.HospitalManagementSystem.Controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    // =========================================================
    // GET NOTIFICATIONS
    // =========================================================

    @GetMapping
    public List<Map<String, Object>> getNotifications(
            Authentication authentication) {

        List<Map<String, Object>> notifications =
                new ArrayList<>();

        String username = "User";

        if (authentication != null
                && authentication.getName() != null
                && !authentication.getName().isBlank()) {

            username = authentication.getName();
        }

        // =====================================================
        // 1. APPOINTMENT NOTIFICATION
        // =====================================================

        Map<String, Object> appointment =
                new LinkedHashMap<>();

        appointment.put(
                "id",
                "appointment-system-update"
        );

        appointment.put(
                "title",
                "Appointment Updates"
        );

        appointment.put(
                "description",
                "Your appointment information is available in the appointments section."
        );

        appointment.put(
                "category",
                "APPOINTMENT"
        );

        appointment.put(
                "icon",
                "fa-solid fa-calendar-check"
        );

        appointment.put(
                "tone",
                "blue"
        );

        appointment.put(
                "timestamp",
                LocalDateTime.now().toString()
        );

        appointment.put(
                "link",
                "/ui/appointments"
        );

        notifications.add(appointment);

        // =====================================================
        // 2. MEDICAL RECORD NOTIFICATION
        // =====================================================

        Map<String, Object> medical =
                new LinkedHashMap<>();

        medical.put(
                "id",
                "medical-record-system-update"
        );

        medical.put(
                "title",
                "Medical Records"
        );

        medical.put(
                "description",
                username
                        + ", your medical record information can be viewed from the medical records section."
        );

        medical.put(
                "category",
                "MEDICAL"
        );

        medical.put(
                "icon",
                "fa-solid fa-file-medical"
        );

        medical.put(
                "tone",
                "green"
        );

        medical.put(
                "timestamp",
                LocalDateTime.now().toString()
        );

        medical.put(
                "link",
                "/ui/medical-records"
        );

        notifications.add(medical);

        // =====================================================
        // 3. GENERAL SYSTEM NOTIFICATION
        // =====================================================

        Map<String, Object> system =
                new LinkedHashMap<>();

        system.put(
                "id",
                "system-update"
        );

        system.put(
                "title",
                "System Notification"
        );

        system.put(
                "description",
                "Your hospital management account is active and ready to use."
        );

        system.put(
                "category",
                "SYSTEM"
        );

        system.put(
                "icon",
                "fa-regular fa-bell"
        );

        system.put(
                "tone",
                "yellow"
        );

        system.put(
                "timestamp",
                LocalDateTime.now().toString()
        );

        // Keep notification page navigation inside
        // pages available to all logged-in roles.
        system.put(
                "link",
                "/dashboard"
        );

        notifications.add(system);

        return notifications;
    }
}