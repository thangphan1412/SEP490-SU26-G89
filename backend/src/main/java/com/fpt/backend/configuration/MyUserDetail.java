package com.fpt.backend.configuration;

import com.fpt.backend.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Setter
@Getter
public class MyUserDetail implements UserDetails {

    private final Users users;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (users == null || users.getUserRoles() == null) {
            return List.of();
        }
        return users.getUserRoles()
                .stream()
                .filter(java.util.Objects::nonNull)
                .map(userRole -> userRole.getRole())
                .filter(java.util.Objects::nonNull)
                .map(role -> role.getRoleName())
                .filter(roleName -> roleName != null && !roleName.isBlank())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public @Nullable String getPassword() {
        return users.getPassword();
    }

    @Override
    public String getUsername() {
        return users.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
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
}
