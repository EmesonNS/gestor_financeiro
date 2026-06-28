package com.zorysa.finance.auth.security;

import com.zorysa.finance.users.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userService.findActiveByEmail(username)
                .map(user -> new AuthUserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash(), user.getRole().name(), user.isActive()))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));
    }
}
