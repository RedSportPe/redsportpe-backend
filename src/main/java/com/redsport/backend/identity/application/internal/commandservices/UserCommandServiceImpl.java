package com.redsport.backend.identity.application.internal.commandservices;
import com.redsport.backend.identity.domain.model.aggregates.User;
import com.redsport.backend.identity.domain.model.commands.CreateUserCommand;
import com.redsport.backend.identity.domain.model.commands.SignInCommand;
import com.redsport.backend.identity.domain.services.UserCommandService;
import com.redsport.backend.identity.infrastructure.persistence.jpa.repositories.UserRepository;
import com.redsport.backend.identity.infrastructure.tokens.jwt.services.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UserCommandServiceImpl(UserRepository userRepository,
                                  PasswordEncoder passwordEncoder,
                                  TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Override
    public Optional<ImmutablePair> handle(SignInCommand command) {
        var user = userRepository.findByEmail(command.email());
        if (user.isEmpty()) {
            throw new RuntimeException("Credenciales inválidas");
        }

        var found = user.get();
        if (!passwordEncoder.matches(command.password(), found.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = tokenService.generateToken(found.getEmail(), found.getRole().name());
        return Optional.of(new ImmutablePair(found, token));
    }

    @Override
    public User handle(CreateUserCommand command) {
        // Invariant: no two users with the same email
        if (userRepository.existsByEmail(command.email())) {
            throw new RuntimeException("Ya existe un usuario con ese correo");
        }
        String hashedPassword = passwordEncoder.encode(command.password());
        User user = new User(command.name(), command.email(), hashedPassword, command.role());
        return userRepository.save(user);
    }

}