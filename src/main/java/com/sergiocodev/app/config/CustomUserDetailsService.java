package com.sergiocodev.app.config;

import com.sergiocodev.app.model.User;
import com.sergiocodev.app.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));

        if (!user.isActive()) {
            throw new org.springframework.security.authentication.DisabledException("The user is inactive");
        }

        java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();

        // Add roles
        user.getRoles().forEach(role -> {
            authorities.add(
                    new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.getName()));
            // Add role permissions
            role.getPermissions().forEach(permission -> {
                authorities.add(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(permission.getName()));
            });
        });

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                authorities);
    }
}
