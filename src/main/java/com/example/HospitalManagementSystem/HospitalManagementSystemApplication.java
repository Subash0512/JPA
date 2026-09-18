package com.example.HospitalManagementSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HospitalManagementSystemApplication {

    private static final Logger logger =
            LoggerFactory.getLogger(HospitalManagementSystemApplication.class);

    public static void main(String[] args) {

        SpringApplication.run(HospitalManagementSystemApplication.class, args);

        logger.info("Hospital Management System Started Successfully");
    }
}