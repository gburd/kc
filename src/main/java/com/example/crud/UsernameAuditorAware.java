package com.example.crud;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;

public class UsernameAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        User user = (User)authentication.getPrincipal();
        String username = null;
        if (user != null) {
            username = user.getUsername();
        }
        return (username == null) ? Optional.empty() : Optional.of(username);
    }
}
