package com.claimguard.service;

import com.claimguard.dto.request.CreateUserRequest;
import com.claimguard.dto.request.LoginRequest;
import com.claimguard.dto.response.AuthResponse;
import com.claimguard.dto.response.UserProfileResponse;
import com.claimguard.entity.User;
import com.claimguard.exception.DuplicateResourceException;
import com.claimguard.exception.ResourceNotFoundException;
import com.claimguard.repository.UserRepository;
import com.claimguard.security.CustomUserDetails;
import com.claimguard.security.JwtService;
import com.claimguard.security.SecurityUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditService auditService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        auditService.logAction("User", userDetails.getId(), "USER_LOGIN", "User logged in successfully: " + userDetails.getUsername());

        return new AuthResponse(
                token,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getFullName(),
                userDetails.getRole()
        );
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        return mapToUserProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserProfileResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserProfileResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken.");
        }

        User user = new User(
                request.getUsername().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName().trim(),
                request.getRole()
        );

        User savedUser = userRepository.save(user);

        auditService.logAction(
                "User",
                savedUser.getId(),
                "CREATE_USER",
                String.format("Created user '%s' with role %s", savedUser.getUsername(), savedUser.getRole())
        );

        return mapToUserProfileResponse(savedUser);
    }

    @Transactional
    public UserProfileResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setEnabled(!user.isEnabled());
        User updated = userRepository.save(user);

        auditService.logAction(
                "User",
                updated.getId(),
                "TOGGLE_USER_STATUS",
                "User enabled state toggled to: " + updated.isEnabled()
        );

        return mapToUserProfileResponse(updated);
    }

    private UserProfileResponse mapToUserProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}
