package org.blogapp.dg_blogapp.mapper;

import org.blogapp.dg_blogapp.dto.RegisterRequest;
import org.blogapp.dg_blogapp.dto.UserResponseDTO;
import org.blogapp.dg_blogapp.model.Role;
import org.blogapp.dg_blogapp.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper class to convert between User entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Converts RegisterRequest, encodedPassword, and role into a User entity.
     * @param registerRequest The registration form data
     * @param encodedPassword The already encoded password
     * @param role The role to assign
     * @return A new User entity
     */
    @Mapping(target = "password", source = "encodedPassword")
    @Mapping(target = "role", source = "role")
    User toEntity(RegisterRequest registerRequest, String encodedPassword, Role role);

    /**
     * Converts a User entity into a UserResponseDTO.
     * @param user the user entity
     * @return UserResponseDTO with safe client-facing fields
     */
    UserResponseDTO toDto(User user);
}






//    public User toEntity(RegisterRequest request, String encodedPassword){
//        return User.builder()
//                .username(request.getUsername())
//                .password(encodedPassword)
//                .email(request.getEmail())
//                .role(request.getRole())
//                .build();
//    }



//    public UserResponseDTO toDto(User user) {
//        if (user == null) {
//            return null;
//        }
//        return UserResponseDTO.builder()
//                .id(user.getId())
//                .username(user.getUsername())
//                .email(user.getEmail())
//                .role(user.getRole())
//                .build();
//    }