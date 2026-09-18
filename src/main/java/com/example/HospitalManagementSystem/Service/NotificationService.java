package com.example.HospitalManagementSystem.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger =
            LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;

    
    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAppointmentConfirmation(String patientEmail, String appointmentDetails) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(patientEmail);
            message.setSubject("Appointment Confirmation - Hospital Management System");
            message.setText(appointmentDetails);
            message.setFrom("noreply@hospital.com");
            
            mailSender.send(message);
            logger.info("Appointment confirmation email sent to {}", patientEmail);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + patientEmail + ": " + e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

    public void sendBillingNotification(String patientEmail, String billingDetails) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(patientEmail);
            message.setSubject("Billing Information - Hospital Management System");
            message.setText(billingDetails);
            message.setFrom("noreply@hospital.com");
            
            mailSender.send(message);
            logger.info("Billing notification email sent to {}", patientEmail);
        } catch (Exception e) {
        	logger.error("Failed to send billing email to {}", patientEmail, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    public void sendMedicalRecordNotification(String patientEmail, String recordDetails) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();  
            message.setTo(patientEmail);
            message.setSubject("Medical Record Update - Hospital Management System");
            message.setText(recordDetails);
            message.setFrom("noreply@hospital.com");
            
            mailSender.send(message);
            logger.info("Medical record notification email sent to {}", patientEmail);
        } catch (Exception e) {
        	logger.error("Failed to send medical record email to {}", patientEmail, e);
            throw new RuntimeException("Email sending failed", e);
        }
        
    }
    public void sendRescheduleNotification(String recipientEmail, String patientName, String doctorName, String oldTime, String newTime) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setSubject("Appointment Rescheduled");
        message.setText("Dear " + patientName + ",\n\nYour appointment with Dr. " + doctorName + 
            " originally scheduled for " + oldTime + " has been rescheduled to " + newTime + 
            ".\n\nIf you have questions, contact the hospital.\n\nThank you!");
        mailSender.send(message);
    }

    public void sendCancellationNotification(String recipientEmail, String patientName, String doctorName, String cancelTime) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setSubject("Appointment Cancelled");
        message.setText("Dear " + patientName + ",\n\nYour appointment with Dr. " + doctorName +
            " scheduled for " + cancelTime +
            " has been cancelled.\n\nFor assistance or to reschedule, contact the hospital.\n\nThank you!");
        mailSender.send(message);
    }

}