package com.helios.platform.pulse.controller;

import com.helios.platform.pulse.dto.AuthRequestDTO;
import com.helios.platform.pulse.dto.AuthResponseDTO;
import com.helios.platform.pulse.dto.RegisterRequestDTO;
import com.helios.platform.pulse.entities.UserEntity;
import com.helios.platform.pulse.security.AuthService;
import com.helios.platform.pulse.service.NotificationService;
import com.helios.platform.pulse.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AuthService authService;

    @InjectMocks
    private UserController userController;

    @Test
    void testGetAll() {
        when(userService.getAll()).thenReturn(Collections.emptyList());
        List<UserEntity> result = userController.getAll();
        assertEquals(0, result.size());
    }

    @Test
    void testAuth_Success() {
        ReflectionTestUtils.setField(userController, "otc", new AtomicInteger(123456));

        boolean result = userController.auth(123456);

        assertTrue(result);
        verify(notificationService, times(1)).sendNotification("Authentication Successful!");
    }

    @Test
    void testAuth_Failure() {
        ReflectionTestUtils.setField(userController, "otc", new AtomicInteger(123456));

        boolean result = userController.auth(654321);

        assertFalse(result);
        verify(notificationService, times(1)).sendNotification("Authentication Failed!");
    }

    @Test
    void testAuthOtc() {
        userController.authOtc();
        verify(notificationService, times(1)).sendNotification(contains("YOUR AUTHENTICATION CODE IS:"));
    }

    @Test
    void testNewUser_Success() {
        RegisterRequestDTO req = new RegisterRequestDTO();
        req.setUsername("Test");
        when(authService.register(any())).thenReturn(AuthResponseDTO.builder().build());

        ResponseEntity<?> response = userController.newUser(req);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testNewUser_InvalidRequest() {
        ResponseEntity<?> response1 = userController.newUser(null);
        assertEquals(400, response1.getStatusCode().value());

        RegisterRequestDTO req2 = new RegisterRequestDTO();
        ResponseEntity<?> response2 = userController.newUser(req2);
        assertEquals(400, response2.getStatusCode().value());
    }

    @Test
    void testUpdateUser_Null() {
        assertNull(userController.updateUser(null));
        UserEntity u = new UserEntity();
        assertNull(userController.updateUser(u));
    }

    @Test
    void testUpdateUser_Success() {
        UserEntity u = new UserEntity();
        u.setUsername("Test");
        when(userService.updateUser(any())).thenReturn(u);

        UserEntity result = userController.updateUser(u);
        assertNotNull(result);
    }

    @Test
    void testDeleteUser_Invalid() {
        assertEquals(400, userController.deleteUser(null).getStatusCode().value());
        assertEquals(400, userController.deleteUser(0L).getStatusCode().value());
    }

    @Test
    void testDeleteUser_Success() {
        when(userService.deleteUser(1L)).thenReturn(ResponseEntity.ok("Ok"));
        assertEquals(200, userController.deleteUser(1L).getStatusCode().value());
    }

    @Test
    void testAuthenticateUser() {
        when(authService.authenticate(any())).thenReturn(AuthResponseDTO.builder().build());
        ResponseEntity<?> response = userController.authenticateUser(new AuthRequestDTO());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testGetCurrentProfile_Unauthorized() {
        ResponseEntity<?> response = userController.getCurrentProfile(null);
        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testGetCurrentProfile_Success() {
        Principal principal = () -> "testUser";
        when(userService.getProfile("testUser")).thenReturn(new com.helios.platform.pulse.dto.UserResponseDTO());

        ResponseEntity<?> response = userController.getCurrentProfile(principal);
        assertEquals(200, response.getStatusCode().value());
    }
}
