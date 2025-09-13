package com.example.HospitalManagementSystem.Service;
import com.example.HospitalManagementSystem.DTO.AppointmentDTO;
import com.example.HospitalManagementSystem.DTO.DoctorDTO;
import com.example.HospitalManagementSystem.DTO.PatientDTO;
import com.example.HospitalManagementSystem.Model.Appointment;
import com.example.HospitalManagementSystem.Model.Doctor;
import com.example.HospitalManagementSystem.Model.Patient;
import com.example.HospitalManagementSystem.Repsitory.AppointmentRepo;
import com.example.HospitalManagementSystem.Repsitory.DoctorRepo;
import com.example.HospitalManagementSystem.Repsitory.PatientRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepo appointmentRepo;
    private final PatientRepo patientRepo;
    private final DoctorRepo doctorRepo;
    private final NotificationService notificationService;

    @Autowired
    public AppointmentService(
            AppointmentRepo appointmentRepo,
            PatientRepo patientRepo,
            DoctorRepo doctorRepo,
            NotificationService notificationService) {
        this.appointmentRepo = appointmentRepo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.notificationService = notificationService;
    }

    public Appointment createAppointment(Appointment appointment) {
        if (appointment.getPatient() == null || appointment.getPatient().getId() == null) {
            throw new RuntimeException("Patient ID is required");
        }
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            throw new RuntimeException("Doctor ID is required");
        }

        Long patientId = appointment.getPatient().getId();
        Long doctorId = appointment.getDoctor().getId();

        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);

        Appointment saved = appointmentRepo.save(appointment);

        if (notificationService != null && patient.getEmail() != null && !patient.getEmail().isEmpty()) {
            String details = String.format("Dear %s, your appointment is confirmed on %s with Dr. %s.",
                    patient.getName(), saved.getAppointmentTime(), doctor.getName());
            try {
                notificationService.sendAppointmentConfirmation(patient.getEmail(), details);
            } catch (Exception e) {
                System.err.println("Failed to send email notification: " + e.getMessage());
            }
        }
        return saved;
    }

    public Appointment saveAppointment(Appointment appointment) {
        Optional<Appointment> existingOpt = appointmentRepo.findById(appointment.getId());
        if (existingOpt.isPresent()) {
            Appointment existing = existingOpt.get();
            String oldTime = existing.getAppointmentTime().toString();
            Appointment saved = appointmentRepo.save(appointment);

            if (notificationService != null && saved.getPatient().getEmail() != null) {
                try {
                    notificationService.sendRescheduleNotification(
                            saved.getPatient().getEmail(),
                            saved.getPatient().getName(),
                            saved.getDoctor().getName(),
                            oldTime,
                            saved.getAppointmentTime().toString()
                    );
                } catch (Exception e) {
                    System.err.println("Failed to send reschedule email: " + e.getMessage());
                }
            }
            return saved;
        } else {
            // If not existing, just save without notification
            return appointmentRepo.save(appointment);
        }
    }

    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepo.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<AppointmentDTO> getAppointmentById(Long id) {
        return appointmentRepo.findById(id)
                .map(this::convertToDTO);
    }

    public void deleteAppointment(Long id) {
        Optional<Appointment> existingOpt = appointmentRepo.findById(id);
        if (existingOpt.isPresent()) {
            Appointment existing = existingOpt.get();
            String cancelTime = existing.getAppointmentTime().toString();
            Patient patient = existing.getPatient();
            Doctor doctor = existing.getDoctor();

            appointmentRepo.deleteById(id);

            if (notificationService != null && patient.getEmail() != null) {
                try {
                    notificationService.sendCancellationNotification(
                            patient.getEmail(),
                            patient.getName(),
                            doctor.getName(),
                            cancelTime
                    );
                } catch (Exception e) {
                    System.err.println("Failed to send cancellation email: " + e.getMessage());
                }
            }
        } else {
            // Optionally handle not found case
            throw new RuntimeException("Appointment not found with ID: " + id);
        }
    }

    public AppointmentDTO convertToDTO(Appointment appointment) {
        Patient patient = appointment.getPatient();
        Doctor doctor = appointment.getDoctor();

        PatientDTO patientDTO = new PatientDTO(
                patient.getId(),
                patient.getName(),
                patient.getAge(),
                patient.getGender(),
                patient.getPhone(),
                patient.getEmail(),
                patient.getAddress()
        );

        DoctorDTO doctorDTO = new DoctorDTO(
                doctor.getId(),
                doctor.getName(),
                doctor.getSpecialization(),
                doctor.getEmail(),
                doctor.getPhone()
        );

        return new AppointmentDTO(
                appointment.getId(),
                patientDTO,
                doctorDTO,
                appointment.getAppointmentTime(),
                appointment.getStatus()
        );
    }
}
