package com.drivertracker.service;

import com.drivertracker.model.AuthRequest;
import com.drivertracker.model.AuthResponse;
import com.drivertracker.model.User;
import com.drivertracker.security.CustomUserDetailsService;
import com.drivertracker.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public AuthService(AuthenticationManager authenticationManager,
                       CustomUserDetailsService userDetailsService,
                       JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse login(AuthRequest request) {
        try {
            // 1. Let Spring verify username + password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid username or password");
        }

        // 2. Load user to get their role
        User user = userDetailsService.findUser(request.getUsername());

        // 3. Generate JWT token
        String token = jwtUtil.generateToken(user.getUserId(), user.getRole().name());

        // 4. Return token + user info
        return new AuthResponse(token, user.getUserId(), user.getRole().name());
    }
}