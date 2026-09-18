package com.example.HospitalManagementSystem.Service;

import com.example.HospitalManagementSystem.Model.Doctor;
import com.example.HospitalManagementSystem.Model.Patient;
import com.example.HospitalManagementSystem.Model.PasswordResetToken;
import com.example.HospitalManagementSystem.Model.User;
import com.example.HospitalManagementSystem.Repsitory.DoctorRepo;
import com.example.HospitalManagementSystem.Repsitory.PasswordResetTokenRepository;
import com.example.HospitalManagementSystem.Repsitory.PatientRepo;
import com.example.HospitalManagementSystem.Repsitory.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final int TOKEN_VALIDITY_MINUTES = 15;

    private final UserRepository userRepository;
    private final PatientRepo patientRepo;
    private final DoctorRepo doctorRepo;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String baseUrl;

    public PasswordResetService(
            UserRepository userRepository,
            PatientRepo patientRepo,
            DoctorRepo doctorRepo,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender,
            @Value("${app.base-url:http://localhost:8080}") String baseUrl) {

        this.userRepository = userRepository;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.baseUrl = baseUrl.replaceAll("/$", "");
    }

    // =========================================================
    // REQUEST PASSWORD RESET
    // =========================================================

    @Transactional
    public void requestPasswordReset(String email) {

        String normalizedEmail =
                email == null ? "" : email.trim();

        if (normalizedEmail.isBlank()) {
            return;
        }

        User user = findUserByEmail(normalizedEmail).orElse(null);

        /*
         * Do not reveal whether an account exists.
         */
        if (user == null || !user.isEnabled()) {
            return;
        }

        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken =
                new PasswordResetToken(
                        token,
                        user,
                        LocalDateTime.now().plusMinutes(
                                TOKEN_VALIDITY_MINUTES
                        )
                );

        tokenRepository.save(resetToken);

        sendResetEmail(
                normalizedEmail,
                token
        );
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================

    @Transactional
    public void resetPassword(
            String token,
            String newPassword,
            String confirmPassword) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Password reset token is required"
            );
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "New password is required"
            );
        }

        if (newPassword.length() < 6) {
            throw new IllegalArgumentException(
                    "New password must be at least 6 characters"
            );
        }

        if (confirmPassword == null ||
                !newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException(
                    "New password and confirmation do not match"
            );
        }

        PasswordResetToken resetToken =
                tokenRepository.findByToken(token.trim())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid or expired password reset link"
                                )
                        );

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {

            tokenRepository.delete(resetToken);

            throw new IllegalArgumentException(
                    "Invalid or expired password reset link"
            );
        }

        User user = resetToken.getUser();

        if (user == null || !user.isEnabled()) {

            tokenRepository.delete(resetToken);

            throw new IllegalArgumentException(
                    "This account cannot reset its password"
            );
        }

        if (passwordEncoder.matches(
                newPassword,
                user.getPassword())) {

            throw new IllegalArgumentException(
                    "New password must be different from the current password"
            );
        }

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);

        /*
         * Token can only be used once.
         */
        tokenRepository.delete(resetToken);
    }

    // =========================================================
    // FIND USER BY EMAIL
    // =========================================================

    private Optional<User> findUserByEmail(String email) {

        Optional<Patient> patient =
                patientRepo.findByEmail(email);

        if (patient.isPresent() &&
                patient.get().getUser() != null) {

            return Optional.of(
                    patient.get().getUser()
            );
        }

        Optional<Doctor> doctor =
                doctorRepo.findByEmail(email);

        if (doctor.isPresent() &&
                doctor.get().getUser() != null) {

            return Optional.of(
                    doctor.get().getUser()
            );
        }

        return Optional.empty();
    }

    // =========================================================
    // SEND RESET EMAIL
    // =========================================================

    private void sendResetEmail(
            String email,
            String token) {

        String resetLink =
                baseUrl +
                "/reset-password?token=" +
                token;

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(email);
        message.setSubject(
                "Hospital Management System - Password Reset"
        );

        message.setText(
                "Hello,\n\n" +
                "We received a request to reset your Hospital Management System password.\n\n" +
                "Use the link below to create a new password:\n" +
                resetLink + "\n\n" +
                "This link will expire in " +
                TOKEN_VALIDITY_MINUTES +
                " minutes and can only be used once.\n\n" +
                "If you did not request this reset, you can safely ignore this email.\n\n" +
                "Hospital Management System"
        );

        mailSender.send(message);
    }
}
