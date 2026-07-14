package com.helios.platform.pulse.service;

import com.helios.platform.pulse.dto.LoteDTO;
import com.helios.platform.pulse.entities.LoteEntity;
import com.helios.platform.pulse.repositories.ILoteRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoteServiceTest {

    @Mock
    private ILoteRepo loteRepo;

    @InjectMocks
    private LoteService loteService;

    @Test
    void testGetAllLotes() {
        LoteEntity entity = new LoteEntity();
        entity.setId(1L);
        entity.setDeliveryDate("2026-01-01");
        entity.setFirstSerial(100);
        entity.setQuantity(50);
        entity.setLastSerial(149);
        entity.setType("PROPIETARIO");
        entity.setRemaining(50);

        when(loteRepo.findAll()).thenReturn(Collections.singletonList(entity));

        List<LoteDTO> dtos = loteService.getAllLotes();
        assertEquals(1, dtos.size());
        assertEquals(1L, dtos.get(0).getId());
        assertEquals(100, dtos.get(0).getFirstSerial());
    }

    @Test
    void testCreateLote() {
        LoteDTO dto = new LoteDTO();
        dto.setDeliveryDate("2026-01-01");
        dto.setFirstSerial(100);
        dto.setQuantity(50);
        dto.setType("PROPIETARIO");

        LoteEntity savedEntity = new LoteEntity();
        savedEntity.setId(1L);
        savedEntity.setDeliveryDate(dto.getDeliveryDate());
        savedEntity.setFirstSerial(dto.getFirstSerial());
        savedEntity.setQuantity(dto.getQuantity());
        savedEntity.setLastSerial(149);
        savedEntity.setType(dto.getType());
        savedEntity.setRemaining(50);

        when(loteRepo.save(any(LoteEntity.class))).thenReturn(savedEntity);

        LoteDTO result = loteService.createLote(dto);
        assertEquals(1L, result.getId());
        assertEquals(149, result.getLastSerial());
        assertEquals(50, result.getRemaining());
    }

    @Test
    void testUpdateLote_Success() {
        LoteEntity existingEntity = new LoteEntity();
        existingEntity.setId(1L);

        LoteDTO dto = new LoteDTO();
        dto.setDeliveryDate("2026-01-01");
        dto.setFirstSerial(200);
        dto.setQuantity(10);
        dto.setType("INVITADO");
        dto.setRemaining(5);

        when(loteRepo.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(loteRepo.save(any(LoteEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        LoteDTO result = loteService.updateLote(1L, dto);
        assertEquals(200, result.getFirstSerial());
        assertEquals(209, result.getLastSerial());
        assertEquals(5, result.getRemaining());
    }

    @Test
    void testUpdateLote_NotFound() {
        when(loteRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> loteService.updateLote(1L, new LoteDTO()));
    }

    @Test
    void testDeleteLote() {
        doNothing().when(loteRepo).deleteById(1L);
        loteService.deleteLote(1L);
        verify(loteRepo, times(1)).deleteById(1L);
    }
}
