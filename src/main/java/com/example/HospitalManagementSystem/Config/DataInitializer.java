package com.example.HospitalManagementSystem.Config;

import com.example.HospitalManagementSystem.Model.User;
import com.example.HospitalManagementSystem.Repsitory.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // Create ADMIN only if it does not already exist
            if (!userRepository.existsByUsername("admin")) {

                User admin = new User();

                admin.setUsername("admin");
                admin.setPassword(
                        passwordEncoder.encode("adminpass"));
                admin.setRole("ADMIN");
                admin.setEnabled(true);

                userRepository.save(admin);

                System.out.println(
                        "Default ADMIN user created successfully.");
            }
        };
    }
}