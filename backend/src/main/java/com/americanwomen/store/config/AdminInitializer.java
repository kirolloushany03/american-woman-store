package com.americanwomen.store.config;

import com.americanwomen.store.entity.User;
import com.americanwomen.store.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            User admin = userRepository.findByUsername("admin").orElse(null);
            
            if (admin == null) {
                // Create admin if doesn't exist
                log.info("[AdminInitializer] Admin user not found. Creating admin user...");
                admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@americanwomen.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setRole(User.Role.ROLE_ADMIN);
                userRepository.save(admin);
                
                // Verify it works
                boolean verified = passwordEncoder.matches("admin123", admin.getPassword());
                log.info("[AdminInitializer] ✅ Admin user created. Username: admin, Password: admin123, Role: {}, Verified: {}", 
                    admin.getRole(), verified);
            } else {
                // Log admin status
                log.info("[AdminInitializer] Admin user exists. Username: {}, Role: {}, ID: {}", 
                    admin.getUsername(), admin.getRole(), admin.getId());
                
                // Check if password is BCrypt encoded (starts with $2a$, $2b$, or $2y$)
                String currentPassword = admin.getPassword();
                boolean isBCrypt = currentPassword != null && 
                    (currentPassword.startsWith("$2a$") || 
                     currentPassword.startsWith("$2b$") || 
                     currentPassword.startsWith("$2y$"));
                
                // Verify current password works
                boolean currentPasswordWorks = isBCrypt && passwordEncoder.matches("admin123", currentPassword);
                
                if (!isBCrypt || !currentPasswordWorks) {
                    // Fix password if it's not BCrypt or doesn't work
                    if (!isBCrypt) {
                        log.warn("[AdminInitializer] Admin password is not BCrypt encoded. Re-encoding...");
                    } else {
                        log.info("[AdminInitializer] Current password doesn't match. Fixing admin password...");
                    }
                    String newHash = passwordEncoder.encode("admin123");
                    admin.setPassword(newHash);
                    userRepository.save(admin);
                    
                    // Verify it works
                    boolean verified = passwordEncoder.matches("admin123", admin.getPassword());
                    if (verified) {
                        log.info("[AdminInitializer] ✅ Admin password fixed and verified. Username: admin, Password: admin123");
                    } else {
                        log.error("[AdminInitializer] ❌ Admin password verification failed after fix!");
                    }
                } else {
                    log.info("[AdminInitializer] ✅ Admin password is correct. Username: admin, Password: admin123");
                }
            }
        } catch (Exception e) {
            log.error("[AdminInitializer] ❌ Error initializing admin user", e);
        }
    }
}
