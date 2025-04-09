package org.blogapp.dg_blogapp.service;

import com.amazonaws.services.elasticache.model.UserAlreadyExistsException;
import org.blogapp.dg_blogapp.dto.loginResponse;
import org.blogapp.dg_blogapp.dto.LoginRequest;
import org.blogapp.dg_blogapp.dto.RegisterRequest;
import org.blogapp.dg_blogapp.dto.UserResponseDTO;
import org.blogapp.dg_blogapp.mapper.UserMapper;
import org.blogapp.dg_blogapp.model.Role;
import org.blogapp.dg_blogapp.model.User;
import org.blogapp.dg_blogapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;




/**
 * Service for handling user authentication and registration.
 */
@Service
public class AuthenticationService {
    private static Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                 JwtService jwtService, AuthenticationManager authenticationManager, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
    }

    /**
     * Registers a new user and returns a JWT token.
     */
    @Transactional
    public UserResponseDTO register(RegisterRequest request, Role role) {
        logger.info("Registering new user: {}", request.getUsername());
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            logger.error("Username {} already exists", request.getUsername());
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }
        // Create and save user
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = userMapper.toEntity(request, encodedPassword, role);
        User savedUser = userRepository.save(user);
        logger.info("User {} registered successfully", request.getUsername());
        return userMapper.toDto(savedUser);
    }



    /**
     * Authenticates a user and returns a JWT token, caching user details.
     */
    @Cacheable(value = "userCache", key = "#request.username")
    public loginResponse authenticate(LoginRequest request) {
        logger.info("Authenticating user: {}", request.getUsername());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            logger.error("Authentication failed for user {}: {}", request.getUsername(), e.getMessage());
            throw new UsernameNotFoundException("Invalid username or password");
        }
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    logger.error("User {} not found", request.getUsername());
                    return new UsernameNotFoundException("User not found: " + request.getUsername());
                });
        String jwtToken = jwtService.generateToken(user);
        logger.info("User {} authenticated successfully", request.getUsername());
        return new loginResponse(jwtToken);
    }
}
