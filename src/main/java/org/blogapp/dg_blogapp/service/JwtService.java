package org.blogapp.dg_blogapp.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.blogapp.dg_blogapp.dto.JwtResponse;
import org.blogapp.dg_blogapp.dto.LoginResponse;
import org.blogapp.dg_blogapp.model.RefreshToken;
import org.blogapp.dg_blogapp.model.User;
import org.blogapp.dg_blogapp.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;


    // Generate Access Token
    public String generateAccessToken(UserDetails user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", user.getAuthorities())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpiration)))
                .signWith(getSigningKey()) // Algorithm inferred automatically
                .compact();
    }

    // Generate Refresh Token (stored in DB)
    public String generateRefreshToken(UserDetails userDetails) {
        String token = UUID.randomUUID().toString();
        User user= (User) userDetails;
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    //extract all calims
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())    //It parses (reads) that token using your secret key (getSigningKey()).
                .build()
                .parseSignedClaims(token)   //It checks if the token’s signature is valid (meaning: not modified or fake).
                .getPayload();          //Then it returns all the information (claims) inside the token.
    }

//    What it does:
//
//    It uses the first method to get all claims from the token.
//
//    But instead of giving you everything, it lets you choose one specific piece of information.
//
//    It takes a function (claimsResolver) — which says what part of the claims you want.
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractClaims(token);
        return claimsResolver.apply(claims);
    }



    // Extract username from token
    public String extractUsername(String token) {
       return extractClaim(token, Claims::getSubject);
    }

    //extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        try{
            return extractExpiration(token).before(new Date());
        }catch(Exception e){
            log.warn("Token expirration check failed", e.getMessage());
            return true;
        }
    }


    // Validate access token
    public boolean isAccessTokenValid(String token, UserDetails user) {
        try {
           final String username = extractUsername(token);
           return username.equals(user.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }


    //validate refresh token from database
    public boolean isRefreshTokenValid(String token){
        return refreshTokenRepository.findByToken(token)
                .map(rt ->rt.getExpiryDate().isAfter(Instant.now()))
                .orElse(false);
    }

    // Rotate refresh token (generate new pair)
    public LoginResponse rotateRefreshToken(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Incorrect refresh token"));

        if (storedToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new RuntimeException("Refresh token expired");
        }

        User user = storedToken.getUser();

        // Delete old refresh token
        refreshTokenRepository.delete(storedToken);

        String newAccessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user);

        log.info("Token pair rotated for user :{} ", user.getUsername());

        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    // Convert Base64 secret into HMAC key
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUserIdFromJwtToken() {
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
    }

    public String getRandomUUID(){
        return UUID.randomUUID().toString();
    }

    public JwtResponse getJwtResponse(UserDetails userDetails){
        String deviceId = getRandomUUID();
        String accessToken = generateAccessToken(userDetails);
        String refreshToken = generateRefreshToken(userDetails);
        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


}
