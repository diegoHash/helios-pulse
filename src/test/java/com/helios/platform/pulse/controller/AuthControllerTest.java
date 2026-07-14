package com.helios.platform.pulse.controller;

import com.helios.platform.pulse.dto.AuthRequestDTO;
import com.helios.platform.pulse.dto.AuthResponseDTO;
import com.helios.platform.pulse.dto.RegisterRequestDTO;
import com.helios.platform.pulse.security.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void testRegister() {
        RegisterRequestDTO req = new RegisterRequestDTO();
        AuthResponseDTO resp = AuthResponseDTO.builder().token("token").username("user").build();
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(resp);

        ResponseEntity<AuthResponseDTO> response = authController.register(req);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("token", java.util.Objects.requireNonNull(response.getBody()).getToken());
    }

    @Test
    void testAuthenticate() {
        AuthRequestDTO req = new AuthRequestDTO();
        AuthResponseDTO resp = AuthResponseDTO.builder().token("token").username("user").build();
        when(authService.authenticate(any(AuthRequestDTO.class))).thenReturn(resp);

        ResponseEntity<AuthResponseDTO> response = authController.authenticate(req);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("token", java.util.Objects.requireNonNull(response.getBody()).getToken());
    }

}
