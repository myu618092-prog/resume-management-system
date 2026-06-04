package com.internship.tracker.service;

import com.internship.tracker.dto.AuthDtos.AuthResponse;
import com.internship.tracker.dto.AuthDtos.LoginRequest;
import com.internship.tracker.dto.AuthDtos.RegisterRequest;
import com.internship.tracker.dto.UserResponse;
import com.internship.tracker.entity.User;
import com.internship.tracker.repository.UserRepository;
import com.internship.tracker.security.CurrentUser;
import com.internship.tracker.security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setSchool(request.school());
        user.setMajor(request.major());
        user.setSkills(request.skills());
        user = userRepository.save(user);

        CurrentUser principal = new CurrentUser(user);
        return new AuthResponse(jwtService.generateToken(principal, user.getId()), UserResponse.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        CurrentUser principal = new CurrentUser(user);
        return new AuthResponse(jwtService.generateToken(principal, user.getId()), UserResponse.from(user));
    }
}
