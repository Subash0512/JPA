package com.example.HospitalManagementSystem.Repsitory;

import com.example.HospitalManagementSystem.Model.User;
import com.example.HospitalManagementSystem.Repsitory.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Used by CustomUserDetailsService during login
    Optional<User> findByUsername(String username);

    // Used during patient/doctor registration
    boolean existsByUsername(String username);
}