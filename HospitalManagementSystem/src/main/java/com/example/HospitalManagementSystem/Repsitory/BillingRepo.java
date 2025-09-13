package com.example.HospitalManagementSystem.Repsitory;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.HospitalManagementSystem.Model.Billing;
import java.util.List;

public interface BillingRepo extends JpaRepository<Billing, Long> {
    List<Billing> findByPatientId(Long patientId);
}
