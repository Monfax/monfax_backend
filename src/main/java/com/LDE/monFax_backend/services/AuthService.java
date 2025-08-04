package com.LDE.monFax_backend.services;

import com.LDE.monFax_backend.dto.LoginDTO;
import com.LDE.monFax_backend.dto.RegisterDTO;
import com.LDE.monFax_backend.enumerations.UserType;
import com.LDE.monFax_backend.models.User;
import com.LDE.monFax_backend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String register(RegisterDTO dto) {
        if (userRepo.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserType.STUDENT);
        userRepo.save(user);

        return jwtService.generateToken(user);
    }

    public String login(LoginDTO dto) {
        Optional<User> userOpt = userRepo.findByEmail(dto.getEmail());

        if (userOpt.isEmpty()) {
            throw new BadCredentialsException("Utilisateur introuvable");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Mot de passe incorrect");
        }
        user.setLastLogin(LocalDateTime.now());
        return jwtService.generateToken(user);
    }


    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    }
}