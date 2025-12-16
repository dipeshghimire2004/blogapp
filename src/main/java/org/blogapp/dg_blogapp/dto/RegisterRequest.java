package org.blogapp.dg_blogapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.blogapp.dg_blogapp.model.Role;

// RegisterRequest.java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {


    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Builder.Default
    private Role role = Role.USER; //Default role
}