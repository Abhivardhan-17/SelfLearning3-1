package com.socialdashboard.controller;

import com.socialdashboard.dto.*;
import com.socialdashboard.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    // User registration with verification email
    @PostMapping("/register")
    public Map<String, Object> register(
            @Valid @RequestBody RegisterRequest request) {
        return auth.register(request);
    }

    // Verify email with 6-digit code
    @PostMapping("/verify-email")
    public Map<String, Object> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        return auth.verifyEmail(request);
    }

    // Resend email verification code
    @PostMapping("/resend-verification")
    public Map<String, Object> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        return auth.resendVerification(request);
    }

    // Normal user login
    @PostMapping("/login")
    public Map<String, Object> login(
            @Valid @RequestBody AuthRequest request) {
        return auth.login(request);
    }

    // Admin login
    @PostMapping("/admin-login")
    public Map<String, Object> adminLogin(
            @Valid @RequestBody AuthRequest request) {
        return auth.adminLogin(request);
    }

    // Forgot password request - sends 6-digit code
    @PostMapping("/forgot-password")
    public Map<String, Object> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        return auth.forgotPassword(request);
    }

    // Verify 6-digit password reset code
    @PostMapping("/verify-reset-code")
    public Map<String, Object> verifyResetCode(
            @Valid @RequestBody VerifyResetCodeRequest request) {
        return auth.verifyResetCode(request);
    }

    // Reset password with new password
    @PostMapping("/reset-password")
    public Map<String, Object> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        return auth.resetPassword(request);
    }

    // Get current authenticated user profile
    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("User is not authenticated");
        }
        return auth.getCurrentUser(authentication.getName());
    }
}
