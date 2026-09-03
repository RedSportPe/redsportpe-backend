package com.redsport.backend.identity.infrastructure.authorization.sfs.services;

import com.redsport.backend.identity.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Bridges our User aggregate to Spring Security's UserDetails.
 * Spring Security uses this to know who a user is and what role they have.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // Spring Security expects roles prefixed with "ROLE_"
        var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name().toUpperCase());

        return new User(
                user.getEmail(),
                user.getPasswordHash(),
                List.of(authority)
        );
    }
}