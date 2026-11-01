
package com.socialdashboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String bio;

    private String avatarUrl;

    @Column(nullable = false, columnDefinition = "varchar(20) default 'USER'")
    private String role = "USER";

    @Column(nullable = false)
    private boolean emailVerified = false;

    private String verificationCodeHash;

    private LocalDateTime verificationCodeExpiry;

    private String resetCodeHash;

    private LocalDateTime resetCodeExpiry;

    private Boolean passwordResetVerified = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getVerificationCodeHash() {
        return verificationCodeHash;
    }

    public void setVerificationCodeHash(String verificationCodeHash) {
        this.verificationCodeHash = verificationCodeHash;
    }

    public LocalDateTime getVerificationCodeExpiry() {
        return verificationCodeExpiry;
    }

    public void setVerificationCodeExpiry(LocalDateTime verificationCodeExpiry) {
        this.verificationCodeExpiry = verificationCodeExpiry;
    }

    public String getResetCodeHash() {
        return resetCodeHash;
    }

    public void setResetCodeHash(String resetCodeHash) {
        this.resetCodeHash = resetCodeHash;
    }

    public LocalDateTime getResetCodeExpiry() {
        return resetCodeExpiry;
    }

    public void setResetCodeExpiry(LocalDateTime resetCodeExpiry) {
        this.resetCodeExpiry = resetCodeExpiry;
    }

    public Boolean getPasswordResetVerified() {
        return passwordResetVerified;
    }

    public void setPasswordResetVerified(Boolean passwordResetVerified) {
        this.passwordResetVerified = passwordResetVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
