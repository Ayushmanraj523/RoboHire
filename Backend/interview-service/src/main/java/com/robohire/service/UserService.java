package com.robohire.service;

import com.robohire.dto.LoginRequest;
import com.robohire.dto.RegisterRequest;
import com.robohire.dto.UserResponse;
import com.robohire.exception.ApiException;
import com.robohire.model.User;
import com.robohire.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for user authentication and management.
 * Handles business logic for user registration and login.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Register a new user in the system.
     * @param request User registration details
     * @return UserResponse with created user information
     * @throws ApiException if email already exists
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new ApiException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword()) // TODO: Implement BCrypt password encoding
                .build();
        
        user = userRepository.save(user);
        log.info("User registered successfully with ID: {}", user.getId());
        
        return mapToUserResponse(user);
    }

    /**
     * Authenticate user login credentials.
     * @param request Login credentials
     * @return UserResponse with authenticated user information
     * @throws ApiException if credentials are invalid
     */
    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found - {}", request.getEmail());
                    return new ApiException("Invalid email or password");
                });

        // TODO: Implement BCrypt password verification
        if (!user.getPassword().equals(request.getPassword())) {
            log.warn("Login failed: Invalid password for user - {}", request.getEmail());
            throw new ApiException("Invalid email or password");
        }

        log.info("User logged in successfully: {}", user.getEmail());
        return mapToUserResponse(user);
    }

    /**
     * Map User entity to UserResponse DTO.
     */
    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
