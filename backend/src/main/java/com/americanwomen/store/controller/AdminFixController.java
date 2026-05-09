package com.americanwomen.store.controller;

import com.americanwomen.store.entity.User;
import com.americanwomen.store.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")

@RestController
@RequestMapping("/api/fix")
public class AdminFixController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminFixController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/check-admin")
    public String checkAdmin() {
        User admin = userRepository.findByUsername("admin")
            .orElse(null);
        if (admin != null) {
            boolean passwordMatches = passwordEncoder.matches("admin123", admin.getPassword());
            return String.format("Admin user found. Username: %s, Role: %s, Password matches: %s", 
                admin.getUsername(), admin.getRole(), passwordMatches);
        }
        return "Admin user not found";
    }

    @PostMapping("/admin-password")
    public String fixAdminPassword() {
        User admin = userRepository.findByUsername("admin")
            .orElse(null);
        if (admin != null) {
            String newHash = passwordEncoder.encode("admin123");
            admin.setPassword(newHash);
            userRepository.save(admin);
            boolean verified = passwordEncoder.matches("admin123", newHash);
            return String.format("Admin password hash updated successfully. Verified: %s", verified);
        }
        return "Admin user not found";
    }

    @GetMapping("/reset-admin")
    public String resetAdmin() {
        // Delete existing admin if exists
        userRepository.findByUsername("admin").ifPresent(userRepository::delete);
        
        // Create new admin with correct password
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@americanwomen.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setRole(User.Role.ROLE_ADMIN);
        userRepository.save(admin);
        
        boolean verified = passwordEncoder.matches("admin123", admin.getPassword());
        return String.format("Admin user reset successfully. Username: admin, Password: admin123, Verified: %s", verified);
    }

    @GetMapping("/fix-login")
    public String fixLogin() {
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            // Create admin if doesn't exist
            admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@americanwomen.com");
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setRole(User.Role.ROLE_ADMIN);
        }
        
        // Fix password
        String newHash = passwordEncoder.encode("admin123");
        admin.setPassword(newHash);
        userRepository.save(admin);
        
        // Verify it works
        boolean verified = passwordEncoder.matches("admin123", admin.getPassword());
        return String.format("{\"status\":\"success\",\"message\":\"Admin password fixed\",\"username\":\"admin\",\"password\":\"admin123\",\"verified\":%s}", verified);
    }

    @GetMapping("/auto-fix")
    public String autoFix() {
        // Same logic as AdminInitializer but can be called manually
        User admin = userRepository.findByUsername("admin").orElse(null);
        
        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@americanwomen.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setRole(User.Role.ROLE_ADMIN);
            userRepository.save(admin);
            boolean verified = passwordEncoder.matches("admin123", admin.getPassword());
            return String.format("{\"status\":\"created\",\"message\":\"Admin user created\",\"username\":\"admin\",\"password\":\"admin123\",\"role\":\"%s\",\"verified\":%s}", 
                admin.getRole(), verified);
        } else {
            boolean currentPasswordWorks = passwordEncoder.matches("admin123", admin.getPassword());
            if (!currentPasswordWorks) {
                String newHash = passwordEncoder.encode("admin123");
                admin.setPassword(newHash);
                userRepository.save(admin);
                boolean verified = passwordEncoder.matches("admin123", admin.getPassword());
                return String.format("{\"status\":\"fixed\",\"message\":\"Admin password fixed\",\"username\":\"admin\",\"password\":\"admin123\",\"role\":\"%s\",\"verified\":%s}", 
                    admin.getRole(), verified);
            } else {
                return String.format("{\"status\":\"ok\",\"message\":\"Admin user is ready\",\"username\":\"admin\",\"password\":\"admin123\",\"role\":\"%s\",\"verified\":true}", 
                    admin.getRole());
            }
        }
    }

    @PostMapping("/create-admin")
    public String createAdmin() {
        if (userRepository.existsByUsername("admin")) {
            return "Admin user already exists";
        }
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@americanwomen.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setRole(User.Role.ROLE_ADMIN);
        userRepository.save(admin);
        return "Admin user created successfully. Username: admin, Password: admin123";
    }
}



