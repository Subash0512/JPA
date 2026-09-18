package com.example.HospitalManagementSystem.Controller;

import com.example.HospitalManagementSystem.Service.PasswordResetService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(
            PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    // =========================================================
    // FORGOT PASSWORD
    // =========================================================

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        String email =
                request == null
                        ? null
                        : request.getEmail();

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Email is required"
                    )
            );
        }

        try {

            passwordResetService.requestPasswordReset(email);

        } catch (Exception exception) {

            /*
             * Do not reveal whether the account exists.
             * The UI receives the same generic message.
             */
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "If an account exists for that email, a password reset link has been sent."
                )
        );
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        if (request == null) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Request body is required"
                    )
            );
        }

        try {

            passwordResetService.resetPassword(
                    request.getToken(),
                    request.getNewPassword(),
                    request.getConfirmPassword()
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Password reset successfully"
                    )
            );

        } catch (IllegalArgumentException exception) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            exception.getMessage()
                    )
            );

        } catch (Exception exception) {

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "message",
                            "Unable to reset password right now"
                    )
            );
        }
    }

    // =========================================================
    // REQUEST DTOs
    // =========================================================

    public static class ForgotPasswordRequest {

        private String email;

        public ForgotPasswordRequest() {
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class ResetPasswordRequest {

        private String token;
        private String newPassword;
        private String confirmPassword;

        public ResetPasswordRequest() {
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}
