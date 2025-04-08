package org.blogapp.dg_blogapp.mapper;

import org.blogapp.dg_blogapp.dto.RegisterRequest;
import org.blogapp.dg_blogapp.dto.UserResponseDTO;
import org.blogapp.dg_blogapp.model.User;
import org.springframework.stereotype.Component;


/**
 * Mapper class to convert between User entity and DTOs.
 */
@Component
public class UserMapper {
    /**
     * Converts an AuthenticationRequest DTO to a User entity.
     */
    public User toEntity(RegisterRequest request, String encodedPassword){
        return User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .email(request.getEmail())
                .role(request.getRole())
                .build();
    }

    /**
     * Converts a User entity to a UserResponseDTO.
     */
    public UserResponseDTO toDto(User user) {
        if (user == null) {
            return null;
        }
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
