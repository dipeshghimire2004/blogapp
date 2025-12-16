package org.blogapp.dg_blogapp.dto;
import lombok.*;

/**
 * Data Transfer Object for authentication responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
}
