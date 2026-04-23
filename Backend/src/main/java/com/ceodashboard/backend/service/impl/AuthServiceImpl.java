package com.ceodashboard.backend.service.impl;

import com.ceodashboard.backend.dto.request.LoginRequest;
import com.ceodashboard.backend.entity.User;
import com.ceodashboard.backend.repository.UserRepository;
import com.ceodashboard.backend.security.JwtUtil;
import com.ceodashboard.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

@Override
public String register(User user) {

    if (user.getUsername() == null || user.getUsername().isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
    }

    if (user.getPassword() == null || user.getPassword().isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
    }

    if (user.getRole() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required");
    }

    if (userRepository.findByUsername(user.getUsername()).isPresent()) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
    }

    user.setPassword(passwordEncoder.encode(user.getPassword()));

    try {
        userRepository.save(user);
    } catch (DataIntegrityViolationException ex) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user data", ex);
    }

    return "USER REGISTERED SUCCESSFULLY";
}

    @Override
    public String login(LoginRequest request) {

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }
}