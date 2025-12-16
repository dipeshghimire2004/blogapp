package org.blogapp.dg_blogapp.controller;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.blogapp.dg_blogapp.dto.UserResponseDTO;

@Getter
@Setter
@Builder
public class AuthResult {
    private UserResponseDTO user;
    private String accessToken;
    private String refreshToken;
    private String message;
}
