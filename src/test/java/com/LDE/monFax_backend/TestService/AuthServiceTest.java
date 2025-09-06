package com.LDE.monFax_backend.TestService;


import com.LDE.monFax_backend.dto.LoginDTO;
import com.LDE.monFax_backend.dto.RegisterDTO;
import com.LDE.monFax_backend.enumerations.UserType;
import com.LDE.monFax_backend.models.User;
import com.LDE.monFax_backend.repositories.UserRepository;
import com.LDE.monFax_backend.services.AuthService;
import com.LDE.monFax_backend.services.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterDTO registerDTO;
    private LoginDTO loginDTO;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        registerDTO = new RegisterDTO();
        registerDTO.setUsername("testuser");
        registerDTO.setEmail("test@example.com");
        registerDTO.setPassword("password123");
        registerDTO.setNumero("699999999");
        registerDTO.setDateNaissance(LocalDate.of(2000, 1, 1));
        registerDTO.setFiliere("Informatique");

        loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(UserType.STUDENT);
    }

    @Test
    void testRegister_Success() {
        when(userRepo.findByEmail(registerDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerDTO.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");

        String token = authService.register(registerDTO);

        assertEquals("jwtToken", token);

        // Vérifie que l'utilisateur est bien sauvegardé avec les bonnes infos
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals(UserType.STUDENT, savedUser.getRole());
    }

    @Test
    void testRegister_EmailAlreadyUsed() {
        when(userRepo.findByEmail(registerDTO.getEmail())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> authService.register(registerDTO));

        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        when(userRepo.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        String token = authService.login(loginDTO);

        assertEquals("jwtToken", token);
        assertNotNull(user.getLastLogin());
    }

    @Test
    void testLogin_UserNotFound() {
        when(userRepo.findByEmail(loginDTO.getEmail())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(loginDTO));
    }

    @Test
    void testLogin_WrongPassword() {
        when(userRepo.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(loginDTO));
    }
}
