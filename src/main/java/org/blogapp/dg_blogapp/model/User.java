package org.blogapp.dg_blogapp.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.blogapp.dg_blogapp.model.Role;
/**
 * Represents a user entity with authentication details and roles.
 */
@Entity
@Table(name = "users")
@Data
@Where(clause="deleted=false")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password cannot be blank")
    private String password;

    @Column(name="email", nullable=false, unique=true)
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    private String email;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role; // Now references standalone Role enum


    @OneToMany(mappedBy="user", cascade= CascadeType.ALL, orphanRemoval=true)
    private List<BlogPost> posts;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Column(name="deleted", nullable=false)
    private boolean deleted=false;
}

