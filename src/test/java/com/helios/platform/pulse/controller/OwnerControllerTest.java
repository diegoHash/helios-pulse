package com.helios.platform.pulse.controller;

import com.helios.platform.pulse.entities.PropietarioModel;
import com.helios.platform.pulse.service.BraceletServiceV2;
import com.helios.platform.pulse.service.PulseOwnerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnerControllerTest {

    @Mock
    private PulseOwnerService ownerService;

    @Mock
    private BraceletServiceV2 braceletServiceV2;

    @InjectMocks
    private OwnerController ownerController;

    @Test
    void testGetOwnersInfo() {
        when(ownerService.getOwners()).thenReturn(Collections.emptyList());
        List<PropietarioModel> owners = ownerController.getOwnersInfo();
        assertEquals(0, owners.size());
    }

    @Test
    void testResetQuotas() {
        when(ownerService.reset()).thenReturn(ResponseEntity.ok("Success"));
        ResponseEntity<String> response = ownerController.resetQuotas();
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Success", response.getBody());
    }

    @Test
    void testGetCurrentSerial() {
        when(braceletServiceV2.getMaxSerial()).thenReturn(100);
        Integer serial = ownerController.getCurrentSerial();
        assertEquals(100, serial);
    }

    @Test
    void testGetRemainingBracelets() {
        when(braceletServiceV2.getRemaining()).thenReturn(50);
        Integer remaining = ownerController.getRemainingBracelets();
        assertEquals(50, remaining);
    }
}
