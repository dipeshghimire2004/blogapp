package org.blogapp.dg_blogapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blogapp.dg_blogapp.dto.AuthResult;
import org.blogapp.dg_blogapp.dto.JwtResponse;
import org.blogapp.dg_blogapp.dto.LoginRequest;
import org.blogapp.dg_blogapp.dto.RegisterRequest;
import org.blogapp.dg_blogapp.dto.TokenRefreshResult;
import org.blogapp.dg_blogapp.model.Role;
import org.blogapp.dg_blogapp.service.AuthenticationService;
import org.blogapp.dg_blogapp.service.JwtService;
import org.blogapp.dg_blogapp.utils.CookieUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name="Authentication", description="User authentication and registration endpoints")
public class AuthenticationController
{

    private final AuthenticationService authService;
    private final CookieUtil cookieUtil;
    private final JwtService jwtService;




    @Operation(summary = "Register a new user", description = "Creates a new user with USER role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "409", description = "Username already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResult> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        AuthResult result = authService.register(request, Role.USER);

        cookieUtil.setAccessTokenCookie(response, result.getAccessToken());
        cookieUtil.setRefreshTokenCookie(response, result.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response)
    {
        JwtResponse result = authService.login(request);
   cookieUtil.setAccessTokenCookie(response, result.getAccessToken());
        cookieUtil.setRefreshTokenCookie(response, result.getRefreshToken());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("accessToken", result.getAccessToken());
        responseBody.put("refreshToken", result.getRefreshToken());
        responseBody.put("message", "Login successful");
        log.info("{}",responseBody);
        return ResponseEntity.ok(responseBody);
    }


    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Rotates access and refresh tokens")
    public ResponseEntity<Map<String, String>> refreshToken(HttpServletRequest request, HttpServletResponse response)
    {
        String refreshToken = cookieUtil.getRefreshToken(request)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        TokenRefreshResult result = authService.refreshToken(refreshToken);

        cookieUtil.setAccessTokenCookie(response, result.getAccessToken());
        cookieUtil.setRefreshTokenCookie(response, result.getRefreshToken());

        return ResponseEntity.ok(Map.of("message", result.getMessage()));
    }

    @Operation(summary = "Logout user")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response)
    {
        // Handle HTTP concern: Clear cookies
        cookieUtil.clearAuthCookie(response);

        // Handle HTTP concern: Build response
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/authme")
    public ResponseEntity<Object> profileMe() {
        Map<String,Object> map = new HashMap<>();
        map.put("user_id",jwtService.getCurrentUserIdFromJwtToken());
//        map.put("roles", SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString());
        return ResponseEntity.ok(map);
    }
}

