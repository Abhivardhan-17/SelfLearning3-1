package com.socialdashboard.service;

import com.socialdashboard.dto.*;
import com.socialdashboard.model.AppUser;
import com.socialdashboard.repository.UserRepository;
import com.socialdashboard.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    // In-memory rate limiting cooldowns (email -> last request instant)
    private final Map<String, Instant> resendCooldowns = new ConcurrentHashMap<>();
    private final Map<String, Instant> resetCooldowns = new ConcurrentHashMap<>();

    private static final int COOLDOWN_SECONDS = 60;
    private static final int CODE_EXPIRATION_MINUTES = 5;

    public AuthService(UserRepository users,
                       PasswordEncoder encoder,
                       JwtService jwt,
                       EmailService emailService) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.emailService = emailService;
    }

    /**
     * User registration with 6-digit email verification code
     */
    public Map<String, Object> register(RegisterRequest request) {

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();

        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }

        if (confirmPassword == null || !password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Check if an existing verified user already has this email
        var existingByEmail = users.findByEmail(email);
        if (existingByEmail.isPresent()) {
            AppUser existing = existingByEmail.get();
            if (existing.isEmailVerified()) {
                throw new IllegalArgumentException("An account with this email already exists. Please log in.");
            }
        }

        // Check if username is taken by a different user
        var existingByUsername = users.findAll().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();

        if (existingByUsername.isPresent()) {
            AppUser existing = existingByUsername.get();
            if (!existing.getEmail().equalsIgnoreCase(email) || existing.isEmailVerified()) {
                throw new IllegalArgumentException("Username is already taken. Please choose another.");
            }
        }

        AppUser user = existingByEmail.orElseGet(AppUser::new);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        user.setRole("USER");
        user.setEmailVerified(false);

        // Generate secure 6-digit verification code and hash it
        String code = generate6DigitCode();
        user.setVerificationCodeHash(encoder.encode(code));
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));

        users.save(user);
        resendCooldowns.put(email, Instant.now());

        // Dispatch verification email via SMTP
        boolean emailSent = emailService.sendVerificationEmail(email, username, code);

        java.util.Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("success", true);
        resp.put("email", email);
        resp.put("emailVerified", false);
        resp.put("smtpDelivered", emailSent);

        if (emailSent) {
            resp.put("message", "Registration successful! A 6-digit verification code has been sent to " + email + ". Please check your inbox and spam folder.");
        } else {
            resp.put("message", "SMTP is not configured. For testing, your verification code is: " + code + " (To receive actual emails, configure Gmail App Password in application.properties).");
            resp.put("verificationCode", code);
        }

        return resp;
    }

    /**
     * Backward-compatible register method accepting AuthRequest
     */
    public Map<String, Object> register(AuthRequest request) {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername(request.getUsername());
        reg.setEmail(request.getEmail());
        reg.setPassword(request.getPassword());
        reg.setConfirmPassword(request.getPassword());
        return register(reg);
    }

    /**
     * Verify email with 6-digit code
     */
    public Map<String, Object> verifyEmail(VerifyEmailRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String code = request.getCode().trim();

        AppUser user = users.findByEmail(email).orElseThrow(() ->
                new IllegalArgumentException("No account found with this email address")
        );

        if (user.isEmailVerified()) {
            return Map.of(
                    "success", true,
                    "message", "Your email is already verified. You can log in now.",
                    "token", jwt.generate(user.getEmail()),
                    "user", userDto(user)
            );
        }

        if (user.getVerificationCodeExpiry() == null
                || LocalDateTime.now().isAfter(user.getVerificationCodeExpiry())) {
            throw new IllegalArgumentException("Verification code has expired. Please request a new code.");
        }

        if (user.getVerificationCodeHash() == null
                || !encoder.matches(code, user.getVerificationCodeHash())) {
            throw new IllegalArgumentException("Invalid verification code. Please check and try again.");
        }

        // Activate user and invalidate verification code
        user.setEmailVerified(true);
        user.setVerificationCodeHash(null);
        user.setVerificationCodeExpiry(null);
        users.save(user);

        log.info("Account verified successfully for: {}", email);

        return Map.of(
                "success", true,
                "message", "Email verified successfully! Welcome to KLMedia.",
                "token", jwt.generate(user.getEmail()),
                "user", userDto(user)
        );
    }

    /**
     * Resend email verification code with cooldown rate-limiting
     */
    public Map<String, Object> resendVerification(ResendVerificationRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        AppUser user = users.findByEmail(email).orElseThrow(() ->
                new IllegalArgumentException("No account found with this email address")
        );

        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("This account is already verified. Please log in.");
        }

        checkCooldown(resendCooldowns, email, "verification code");

        // Generate new secure 6-digit code
        String code = generate6DigitCode();
        user.setVerificationCodeHash(encoder.encode(code));
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));
        users.save(user);

        resendCooldowns.put(email, Instant.now());

        boolean emailSent = emailService.sendVerificationEmail(email, user.getUsername(), code);

        java.util.Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("success", true);
        resp.put("email", email);
        resp.put("smtpDelivered", emailSent);

        if (emailSent) {
            resp.put("message", "A new 6-digit verification code has been sent to " + email + ". Please check your inbox and spam folder.");
        } else {
            resp.put("message", "SMTP is not configured. For testing, your verification code is: " + code);
            resp.put("verificationCode", code);
        }

        return resp;
    }

    /**
     * User login with verified email requirement
     */
    public Map<String, Object> login(AuthRequest request) {

        AppUser user = validateCredentials(request);

        if (!"USER".equalsIgnoreCase(user.getRole())) {
            throw new IllegalArgumentException(
                    "Please use Admin Login for this account"
            );
        }

        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException(
                    "Your email is not verified. Please verify your email before logging in."
            );
        }

        return response(user);
    }

    /**
     * Admin login
     */
    public Map<String, Object> adminLogin(AuthRequest request) {

        AppUser user = validateCredentials(request);

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new IllegalArgumentException(
                    "Access denied. Admin account required"
            );
        }

        return response(user);
    }

    /**
     * Forgot password - Generates 6-digit code and emails user
     * Always returns generic message to avoid email enumeration
     */
    public Map<String, Object> forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        java.util.concurrent.atomic.AtomicBoolean emailSent = new java.util.concurrent.atomic.AtomicBoolean(false);
        java.util.concurrent.atomic.AtomicReference<String> generatedCode = new java.util.concurrent.atomic.AtomicReference<>("");

        users.findByEmail(email).ifPresent(user -> {
            try {
                checkCooldown(resetCooldowns, email, "password reset code");
            } catch (Exception e) {
                log.info("Rate limit hit for password reset on: {}", email);
                return;
            }

            String code = generate6DigitCode();
            user.setResetCodeHash(encoder.encode(code));
            user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));
            user.setPasswordResetVerified(false);
            users.save(user);

            resetCooldowns.put(email, Instant.now());
            generatedCode.set(code);

            boolean sent = emailService.sendPasswordResetEmail(email, user.getUsername(), code);
            emailSent.set(sent);
        });

        java.util.Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("success", true);
        if (emailSent.get()) {
            resp.put("message", "If that email address is registered with KLMedia, a 6-digit reset code has been sent.");
        } else if (!generatedCode.get().isEmpty()) {
            resp.put("message", "SMTP is not configured. For testing, your reset code is: " + generatedCode.get());
            resp.put("resetCode", generatedCode.get());
        } else {
            resp.put("message", "If that email address is registered with KLMedia, a 6-digit reset code has been sent.");
        }
        return resp;
    }

    /**
     * Verify password reset 6-digit code
     */
    public Map<String, Object> verifyResetCode(VerifyResetCodeRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String code = request.getCode().trim();

        AppUser user = users.findByEmail(email).orElseThrow(() ->
                new IllegalArgumentException("Invalid email or reset code")
        );

        if (user.getResetCodeExpiry() == null
                || LocalDateTime.now().isAfter(user.getResetCodeExpiry())) {
            throw new IllegalArgumentException("Password reset code has expired. Please request a new one.");
        }

        if (user.getResetCodeHash() == null
                || !encoder.matches(code, user.getResetCodeHash())) {
            throw new IllegalArgumentException("Invalid reset code. Please check and try again.");
        }

        user.setPasswordResetVerified(true);
        users.save(user);

        return Map.of(
                "success", true,
                "message", "Reset code verified successfully. Please enter your new password."
        );
    }

    /**
     * Reset password with new password and BCrypt hashing
     */
    public Map<String, Object> resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }

        if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        AppUser user = users.findByEmail(email).orElseThrow(() ->
                new IllegalArgumentException("Invalid request")
        );

        if (user.getResetCodeExpiry() == null
                || LocalDateTime.now().isAfter(user.getResetCodeExpiry())) {
            throw new IllegalArgumentException("Reset session has expired. Please request a new code.");
        }

        // If code provided directly, verify it, otherwise check passwordResetVerified flag
        if (request.getCode() != null && !request.getCode().isBlank()) {
            if (user.getResetCodeHash() == null || !encoder.matches(request.getCode().trim(), user.getResetCodeHash())) {
                throw new IllegalArgumentException("Invalid reset code");
            }
        } else if (!Boolean.TRUE.equals(user.getPasswordResetVerified())) {
            throw new IllegalArgumentException("Reset code has not been verified yet.");
        }

        // Hash new password using BCrypt and clear reset state
        user.setPassword(encoder.encode(newPassword));
        user.setResetCodeHash(null);
        user.setResetCodeExpiry(null);
        user.setPasswordResetVerified(false);
        users.save(user);

        log.info("Password reset successfully completed for: {}", email);

        return Map.of(
                "success", true,
                "message", "Your password has been reset successfully. You can now log in with your new password."
        );
    }

    /**
     * Get current authenticated user details
     */
    public Map<String, Object> getCurrentUser(String email) {
        AppUser user = users.findByEmail(email).orElseThrow(() ->
                new IllegalArgumentException("User not found")
        );
        return userDto(user);
    }

    private void checkCooldown(Map<String, Instant> tracker, String key, String actionLabel) {
        Instant lastSent = tracker.get(key);
        if (lastSent != null) {
            long secondsPassed = Duration.between(lastSent, Instant.now()).getSeconds();
            if (secondsPassed < COOLDOWN_SECONDS) {
                long waitTime = COOLDOWN_SECONDS - secondsPassed;
                throw new IllegalArgumentException("Please wait " + waitTime + " seconds before requesting another " + actionLabel + ".");
            }
        }
    }

    private String generate6DigitCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    private AppUser validateCredentials(AuthRequest request) {

        AppUser user = users.findByEmail(
                request.getEmail().trim().toLowerCase()
        ).orElseThrow(() ->
                new IllegalArgumentException("Invalid email or password")
        );

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return user;
    }

    private Map<String, Object> userDto(AppUser user) {
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "bio", user.getBio() == null ? "" : user.getBio(),
                "role", user.getRole(),
                "emailVerified", user.isEmailVerified()
        );
    }

    private Map<String, Object> response(AppUser user) {
        return Map.of(
                "token", jwt.generate(user.getEmail()),
                "user", userDto(user)
        );
    }
}
