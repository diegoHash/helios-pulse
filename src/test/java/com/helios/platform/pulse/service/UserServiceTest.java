package com.helios.platform.pulse.service;

import com.helios.platform.pulse.entities.Role;
import com.helios.platform.pulse.entities.UserEntity;
import com.helios.platform.pulse.repositories.IUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private IUser userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testGetAll() {
        UserEntity user = new UserEntity();
        user.setUsername("TestUser");
        when(userRepository.findByRoleNot(Role.ROOT)).thenReturn(Collections.singletonList(user));

        List<UserEntity> result = userService.getAll();
        assertEquals(1, result.size());
        assertEquals("TestUser", result.get(0).getUsername());
    }

    @Test
    void testNewUser() {
        UserEntity user = new UserEntity();
        user.setUsername("NewUser");
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        UserEntity result = userService.newUser(user);
        assertEquals("NewUser", result.getUsername());
    }

    @Test
    void testUpdateUser_Success() {
        UserEntity oldUser = new UserEntity();
        oldUser.setId(1L);
        oldUser.setUsername("OldName");

        UserEntity newUser = new UserEntity();
        newUser.setId(1L);
        newUser.setUsername("NewName");

        when(userRepository.findById(1L)).thenReturn(Optional.of(oldUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        UserEntity result = userService.updateUser(newUser);
        assertEquals("NewName", result.getUsername());
    }

    @Test
    void testUpdateUser_NotFound() {
        UserEntity newUser = new UserEntity();
        newUser.setId(1L);
        newUser.setUsername("NewName");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserEntity result = userService.updateUser(newUser);
        assertNull(result);
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById(1L);

        ResponseEntity<String> response = userService.deleteUser(1L);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Ok", response.getBody());

        verify(userRepository, times(1)).deleteById(1L);
    }
}
