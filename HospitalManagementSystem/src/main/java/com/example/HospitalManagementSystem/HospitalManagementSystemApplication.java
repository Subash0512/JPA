package com.example.HospitalManagementSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HospitalManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalManagementSystemApplication.class, args);
        System.out.println("Hospital Management System Started Successfully!");
        System.out.println("API Base URL: http://localhost:8080");
        System.out.println("Available Endpoints:");
        System.out.println("- Patients: /patients");
        System.out.println("- Doctors: /doctors"); 
        System.out.println("- Appointments: /appointments");
        System.out.println("- Medical Records: /medicalrecords");
        System.out.println("- Billing: /billing");
    }
}