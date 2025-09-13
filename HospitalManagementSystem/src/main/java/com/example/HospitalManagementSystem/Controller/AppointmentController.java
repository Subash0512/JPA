package com.example.HospitalManagementSystem.Controller;
import com.example.HospitalManagementSystem.DTO.AppointmentDTO;
import com.example.HospitalManagementSystem.Model.Appointment;
import com.example.HospitalManagementSystem.Service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AppointmentDTO> createAppointment(@RequestBody Appointment appointment) {
        try {
            Appointment saved = appointmentService.createAppointment(appointment);
            return ResponseEntity.ok(appointmentService.convertToDTO(saved));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentDTO> updateAppointment(@PathVariable Long id, @RequestBody Appointment updatedAppointment) {
        return appointmentService.getAppointmentById(id)
                .map(existing -> {
                    updatedAppointment.setId(id);
                    Appointment saved = appointmentService.saveAppointment(updatedAppointment);
                    return ResponseEntity.ok(appointmentService.convertToDTO(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}