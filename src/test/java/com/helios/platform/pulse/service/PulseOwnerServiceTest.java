package com.helios.platform.pulse.service;

import com.helios.platform.pulse.entities.PropietarioModel;
import com.helios.platform.pulse.repositories.IPulseOwnerRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PulseOwnerServiceTest {

    @Mock
    private IPulseOwnerRepository ownerRepo;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PulseOwnerService ownerService;

    @Test
    void testReset() {
        ResponseEntity<String> response = ownerService.reset();
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Success", response.getBody());
        verify(ownerRepo, times(1)).resetAll();
    }

    @Test
    void testGetOwners() {
        PropietarioModel owner = new PropietarioModel();
        when(ownerRepo.findAll()).thenReturn(Collections.singletonList(owner));

        List<PropietarioModel> owners = ownerService.getOwners();
        assertEquals(1, owners.size());
        assertEquals(owner, owners.get(0));
    }

    @Test
    void testGetPropertiesID() {
        HashMap<String, Long> map = new HashMap<>();
        map.put("V100", 1L);
        when(ownerRepo.findProperties()).thenReturn(map);

        HashMap<String, Long> result = ownerService.getPropertiesID();
        assertEquals(1, result.size());
        assertEquals(1L, result.get("V100"));
    }

    @Test
    void testUpdate_NotFound() {
        when(ownerRepo.deductCupo(1L, 5)).thenReturn(0);

        ResponseEntity<String> response = ownerService.update(1L, 5);
        assertEquals(404, response.getStatusCode().value());
        verify(notificationService, times(1)).sendNotification(anyString());
    }

    @Test
    void testUpdate_Success() {
        when(ownerRepo.deductCupo(1L, 5)).thenReturn(1);

        ResponseEntity<String> response = ownerService.update(1L, 5);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("UPDATE SATISFIED SUCCESSFULLY!", response.getBody());
        verify(notificationService, times(1)).sendNotification(anyString());
    }
}
