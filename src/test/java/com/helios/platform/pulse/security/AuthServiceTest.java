package com.helios.platform.pulse.security;

import com.helios.platform.pulse.dto.AuthRequestDTO;
import com.helios.platform.pulse.dto.AuthResponseDTO;
import com.helios.platform.pulse.dto.RegisterRequestDTO;
import com.helios.platform.pulse.entities.Role;
import com.helios.platform.pulse.entities.UserEntity;
import com.helios.platform.pulse.repositories.IUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private IUser repository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testRegister_UserAlreadyExists() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("existingUser");

        when(repository.findByUsername("existingUser")).thenReturn(Optional.of(new UserEntity()));

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    @Test
    void testRegister_RootCreation() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("rootUser");
        request.setPassword("pass");

        when(repository.findByUsername("rootUser")).thenReturn(Optional.empty());
        when(repository.countByRole(Role.ROOT)).thenReturn(0L); // No root exists
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(jwtService.generateToken(any(UserEntity.class))).thenReturn("fakeToken");

        AuthResponseDTO response = authService.register(request);
        assertEquals("fakeToken", response.getToken());
        assertEquals("rootUser", response.getUsername());
        assertEquals("ROOT", response.getRole());

        verify(repository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testRegister_NonRoot_Forbidden() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("cajeroUser");

        when(repository.findByUsername("cajeroUser")).thenReturn(Optional.empty());
        when(repository.countByRole(Role.ROOT)).thenReturn(1L); // Root exists

        // Context is empty => forbidden
        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    @Test
    void testRegister_CajeroCreation_Success() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("cajeroUser");
        request.setPassword("pass");
        request.setRole("CAJERO");

        when(repository.findByUsername("cajeroUser")).thenReturn(Optional.empty());
        when(repository.countByRole(Role.ROOT)).thenReturn(1L);

        // Mock auth context as ROOT
        Authentication auth = mock(Authentication.class);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ROOT"))).when(auth).getAuthorities();
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(jwtService.generateToken(any(UserEntity.class))).thenReturn("fakeToken");

        AuthResponseDTO response = authService.register(request);
        assertEquals("CAJERO", response.getRole());
    }

    @Test
    void testRegister_ObservadorCreation_Success() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("obsUser");
        request.setPassword("pass");
        request.setRole("OBSERVADOR");

        when(repository.findByUsername("obsUser")).thenReturn(Optional.empty());
        when(repository.countByRole(Role.ROOT)).thenReturn(1L);

        // Mock auth context as ROOT
        Authentication auth = mock(Authentication.class);
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ROOT"))).when(auth).getAuthorities();
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(jwtService.generateToken(any(UserEntity.class))).thenReturn("fakeToken");

        AuthResponseDTO response = authService.register(request);
        assertEquals("OBSERVADOR", response.getRole());
    }

    @Test
    void testAuthenticate_Success() {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("testUser");
        request.setPassword("pass");

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("testUser");
        user.setRole(Role.CAJERO);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(repository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("fakeToken");

        AuthResponseDTO response = authService.authenticate(request);
        assertEquals("fakeToken", response.getToken());
        assertEquals("testUser", response.getUsername());
        assertEquals("CAJERO", response.getRole());
    }

}
