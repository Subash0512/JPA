package com.example.HospitalManagementSystem.Repsitory;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.HospitalManagementSystem.Model.MedicalRecord;
import java.util.List;

public interface MedicalRecordRepo extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatientId(Long patientId);
}
