
package com.socialdashboard.controller;

import com.socialdashboard.model.AppUser;
import com.socialdashboard.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AdminController(
            UserRepository users,
            PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    // View all users
    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {

        return users.findAll()
                .stream()
                .map(user -> Map.<String, Object>of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                ))
                .toList();
    }

    // Add a new user
    @PostMapping("/users")
    public Map<String, Object> addUser(
            @RequestBody Map<String, String> request) {

        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");

        if (username == null || username.isBlank()
                || email == null || email.isBlank()
                || password == null || password.length() < 6) {
            throw new IllegalArgumentException(
                    "Enter username, valid email and password of at least 6 characters"
            );
        }

        username = username.trim();
        email = email.trim().toLowerCase();

        if (users.existsByEmail(email)
                || users.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Email or username already exists"
            );
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        user.setRole("USER");

        users.save(user);

        return Map.of(
                "success", true,
                "message", "User added successfully"
        );
    }

    // Delete a user
    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(
            @PathVariable Long id) {

        AppUser user = users.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new IllegalArgumentException(
                    "Admin accounts cannot be deleted here"
            );
        }

        users.delete(user);

        return Map.of(
                "success", true,
                "message", "User deleted successfully"
        );
    }
}
