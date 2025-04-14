package org.blogapp.dg_blogapp.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.blogapp.dg_blogapp.dto.LoginResponse;
import org.blogapp.dg_blogapp.model.RefreshToken;
import org.blogapp.dg_blogapp.model.User;
import org.blogapp.dg_blogapp.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final String secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtService(@Value("${jwt.secret}") String secretKey, @Value("${jwt.access-token-expiration}")long accessTokenExpiration, @Value("${jwt.refresh-token-expiration}")long refreshTokenExpiration)  {
        this.secretKey = secretKey;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;

    }

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    /**
     * Generates a JWT token for the given user.
     */
    public String generateAccessToken(UserDetails user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("roles", user.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }


    public String extractUsername(String token){
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();  //get the subject (username)
    }

    public boolean isAccessTokenValid(String token, UserDetails user) {
        try{
            Claims claims= Jwts.parser()
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                return claims.getSubject().equals(user.getUsername()) && claims.getExpiration().before(new Date());

        }
        catch(Exception e){
            return false;
        }
    }

    public LoginResponse rotateRefreshToken(String refreshToken){
        RefreshToken storedToken=refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Incorrect refresh token"));

        if(storedToken.getExpiryDate().isBefore(Instant.now())){
            refreshTokenRepository.delete(storedToken);
            throw new RuntimeException("Refresh token expired");
        }

        User user = storedToken.getUser();
        refreshTokenRepository.delete(storedToken);

//        Generate new token
        String newAccessToken= generateAccessToken(user);
        String newRefreshToken= generateRefreshToken(user);

        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    /**
     * Converts the base64 secret key into a SigningKey.
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
