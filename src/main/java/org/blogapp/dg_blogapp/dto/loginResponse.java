package org.blogapp.dg_blogapp.dto;
import lombok.*;

/**
 * Data Transfer Object for authentication responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class loginResponse {
    String token;
}
