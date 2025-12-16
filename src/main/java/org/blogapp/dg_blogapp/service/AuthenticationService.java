package org.blogapp.dg_blogapp.service;

import com.amazonaws.services.elasticache.model.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blogapp.dg_blogapp.controller.AuthResult;
import org.blogapp.dg_blogapp.dto.JwtResponse;
import org.blogapp.dg_blogapp.dto.LoginRequest;
import org.blogapp.dg_blogapp.dto.LoginResponse;
import org.blogapp.dg_blogapp.dto.RegisterRequest;
import org.blogapp.dg_blogapp.dto.TokenRefreshResult;
import org.blogapp.dg_blogapp.dto.UserResponseDTO;
import org.blogapp.dg_blogapp.mapper.UserMapper;
import org.blogapp.dg_blogapp.model.Role;
import org.blogapp.dg_blogapp.model.User;
import org.blogapp.dg_blogapp.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;




/**
 * Service for handling user authentication and registration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

//    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
//                                 JwtService jwtService, AuthenticationManager authenticationManager, UserMapper userMapper) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//        this.jwtService = jwtService;
//        this.authenticationManager = authenticationManager;
//        this.userMapper = userMapper;
//    }

    /**
     * Registers a new user and returns a JWT token.
     */
    @Transactional
    public AuthResult register(RegisterRequest request, Role role) {
        log.info("Registering new user: {}", request.getEmail());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.error("Email {} already exists", request.getEmail());
            throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
        }
        // Create and save user
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = userMapper.toEntity(request, encodedPassword, role);
        User savedUser = userRepository.save(user);
        log.info("User {} registered successfully", request.getEmail());

        //generate token
        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);
        log.info("Refresh token {} registered successfully", refreshToken);

        //return everything the controller needs
        return AuthResult.builder()
                .user(userMapper.toDto(savedUser))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .message("Registration successful")
                .build();
    }





    public JwtResponse login(LoginRequest loginRequest) {
        log.info("Login request: {}", loginRequest);
        // Find user by email
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String encodedPassword = passwordEncoder.encode(loginRequest.getPassword());
        log.info("Login password: {}", encodedPassword);
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.error("Invalid email or password");
            throw new RuntimeException("Invalid email or password");
        }
        return jwtService.getJwtResponse(user);
    }

    public TokenRefreshResult refreshToken(String refreshToken) {
        log.info("Refreshing token");
        LoginResponse response= jwtService.rotateRefreshToken(refreshToken);

        log.info("Token refreshed successfully");

        return TokenRefreshResult.builder()
                .accessToken(response.getAccessToken())
                .refreshToken(response.getRefreshToken())
                .message("Token refreshed successfully")
                .build();
    }
    /**
     * Authenticates a user and returns a JWT token, caching user details.
     */
//    @Cacheable(value = "userCache", key = "#request.username")
//    public LoginResponse authenticate(LoginRequest request) {
//        logger.info("Authenticating user: {}", request.getUsername());
//        try {
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            request.getUsername(),
//                            request.getPassword()
//                    )
//            );
//        } catch (Exception e) {
//            logger.error("Authentication failed for user {}: {}", request.getUsername(), e.getMessage());
//            throw new UsernameNotFoundException("Invalid username or password");
//        }
//        User user = userRepository.findByUsername(request.getUsername())
//                .orElseThrow(() -> {
//                    logger.error("User {} not found", request.getUsername());
//                    return new UsernameNotFoundException("User not found: " + request.getUsername());
//                });
//        String accessToken = jwtService.generateAccessToken(user);
//        String refreshToken = jwtService.generateRefreshToken(user);
//        logger.info("User {} authenticated successfully", request.getUsername());
//        return new LoginResponse(accessToken, refreshToken);
//    }
}
