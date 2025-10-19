package com.LDE.monFax_backend.controllers;


import com.LDE.monFax_backend.dto.LoginDTO;
import com.LDE.monFax_backend.dto.RegisterDTO;
import com.LDE.monFax_backend.models.User;
import com.LDE.monFax_backend.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO dto) {
        return ResponseEntity.ok(Map.of("token", authService.register(dto)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        String token = authService.login(dto);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        User user = authService.getCurrentUser();
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "numero", user.getNumero(),
                "dateNaissance", user.getDateNaissance(),
                "filiere", user.getFiliere(),
                "role", user.getRole()
        ));
    }


}