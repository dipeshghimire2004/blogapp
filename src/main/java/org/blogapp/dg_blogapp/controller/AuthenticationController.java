package org.blogapp.dg_blogapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.blogapp.dg_blogapp.dto.LoginResponse;
import org.blogapp.dg_blogapp.dto.LoginRequest;
import org.blogapp.dg_blogapp.dto.RegisterRequest;
import org.blogapp.dg_blogapp.dto.UserResponseDTO;
import org.blogapp.dg_blogapp.model.Role;
import org.blogapp.dg_blogapp.service.AuthenticationService;
import org.blogapp.dg_blogapp.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

/**
 * REST controller for authentication endpoints.
 */
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authService;
    private JwtService jwtService;

    public AuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a new user", description = "Creates a new user with USER role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(201).body(authService.register(request, Role.USER));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }


    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Rotates access and refresh tokens")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        LoginResponse response = jwtService.rotateRefreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }
}

