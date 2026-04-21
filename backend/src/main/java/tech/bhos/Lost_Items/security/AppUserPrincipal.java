package tech.bhos.Lost_Items.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tech.bhos.Lost_Items.model.AppUser;
import tech.bhos.Lost_Items.model.UserRole;

import java.util.Collection;
import java.util.List;

public class AppUserPrincipal implements UserDetails {
    private final AppUser user;

    public AppUserPrincipal(AppUser user) {
        this.user = user;
    }

    public Long userId() {
        return user.getUserId();
    }

    public String email() {
        return user.getEmail();
    }

    public UserRole role() {
        return user.effectiveRole();
    }

    public boolean isAdmin() {
        return role() == UserRole.ADMIN;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (isAdmin()) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_ADMIN")
            );
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
}
