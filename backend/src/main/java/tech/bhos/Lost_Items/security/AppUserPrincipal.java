package tech.bhos.Lost_Items.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tech.bhos.Lost_Items.model.AppUser;

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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
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
