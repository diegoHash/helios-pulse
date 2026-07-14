package com.helios.platform.pulse.controller;

import com.helios.platform.pulse.dto.LoteDTO;
import com.helios.platform.pulse.service.LoteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoteControllerTest {

    @Mock
    private LoteService loteService;

    @InjectMocks
    private LoteController loteController;

    @Test
    void testGetAllLotes() {
        when(loteService.getAllLotes()).thenReturn(Collections.emptyList());

        ResponseEntity<List<LoteDTO>> response = loteController.getAllLotes();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(0, java.util.Objects.requireNonNull(response.getBody()).size());
    }

    @Test
    void testCreateLote() {
        LoteDTO dto = new LoteDTO();
        when(loteService.createLote(any(LoteDTO.class))).thenReturn(dto);

        ResponseEntity<LoteDTO> response = loteController.createLote(new LoteDTO());

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());
    }

    @Test
    void testUpdateLote() {
        LoteDTO dto = new LoteDTO();
        when(loteService.updateLote(eq(1L), any(LoteDTO.class))).thenReturn(dto);

        ResponseEntity<LoteDTO> response = loteController.updateLote(1L, new LoteDTO());

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());
    }

    @Test
    void testDeleteLote() {
        doNothing().when(loteService).deleteLote(1L);

        ResponseEntity<Void> response = loteController.deleteLote(1L);

        assertEquals(200, response.getStatusCode().value());
        verify(loteService, times(1)).deleteLote(1L);
    }
}
