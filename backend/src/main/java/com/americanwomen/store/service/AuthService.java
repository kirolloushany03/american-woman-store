package com.americanwomen.store.service;

import com.americanwomen.store.dto.*;
import com.americanwomen.store.entity.User;
import com.americanwomen.store.repository.UserRepository;
import com.americanwomen.store.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final com.americanwomen.store.ocl.OclValidationService oclValidationService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      JwtTokenProvider tokenProvider, AuthenticationManager authenticationManager,
                      com.americanwomen.store.ocl.OclValidationService oclValidationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.oclValidationService = oclValidationService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // OCL validation for password before encoding
        oclValidationService.validatePassword(request.getPassword());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(User.Role.ROLE_USER);

        // OCL validation for User entity before saving
        oclValidationService.validateUser(user);

        user = userRepository.save(user);

        String token = tokenProvider.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, UserDto.from(user));
    }

    public AuthResponse login(AuthRequest request) {
        // Check if user exists first
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = tokenProvider.generateToken(user.getUsername(), user.getRole().name());
            return new AuthResponse(token, UserDto.from(user));
        } catch (BadCredentialsException e) {
            // Re-throw as BadCredentialsException to return 401
            throw new BadCredentialsException("Invalid username or password");
        } catch (org.springframework.security.core.AuthenticationException e) {
            // Any other Spring Security authentication exception should be 401
            throw new BadCredentialsException("Invalid username or password");
        } catch (Exception e) {
            // Log unexpected errors but still return 401 for security
            System.err.println("[AuthService] Unexpected error during login: " + e.getMessage());
            e.printStackTrace();
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    public UserDto getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return UserDto.from(user);
    }
    
    public com.americanwomen.store.dto.ProfileResponse getCurrentUserProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return com.americanwomen.store.dto.ProfileResponse.from(user);
    }

    @Transactional
    public UserDto updateProfile(UpdateProfileRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Email validation - must be unique if changed
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    throw new RuntimeException("Email already exists");
                }
            }
            user.setEmail(request.getEmail().trim());
        }
        
        // Update optional fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getCity() != null) {
            user.setCity(request.getCity().trim());
        }
        if (request.getAddress1() != null) {
            user.setAddress1(request.getAddress1().trim());
        }
        if (request.getAddress2() != null) {
            user.setAddress2(request.getAddress2().trim());
        }

        // OCL validation before saving
        oclValidationService.validateUser(user);

        user = userRepository.save(user);
        return UserDto.from(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}

