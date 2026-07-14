package com.helios.platform.pulse.service;

import com.helios.platform.pulse.entities.IdempotencyKey;
import com.helios.platform.pulse.repositories.IdempotencyKeyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private IdempotencyKeyRepository repository;

    @InjectMocks
    private IdempotencyService idempotencyService;

    @Test
    void testIsAlreadyProcessed_NullOrBlank() {
        assertFalse(idempotencyService.isAlreadyProcessed(null));
        assertFalse(idempotencyService.isAlreadyProcessed(""));
        assertFalse(idempotencyService.isAlreadyProcessed("   "));
        verify(repository, never()).existsById(anyString());
    }

    @Test
    void testIsAlreadyProcessed_Exists() {
        when(repository.existsById("key-123")).thenReturn(true);
        assertTrue(idempotencyService.isAlreadyProcessed("key-123"));
    }

    @Test
    void testIsAlreadyProcessed_NotExists() {
        when(repository.existsById("key-123")).thenReturn(false);
        assertFalse(idempotencyService.isAlreadyProcessed("key-123"));
    }

    @Test
    void testMarkAsProcessed_NullOrBlank() {
        idempotencyService.markAsProcessed(null);
        idempotencyService.markAsProcessed("   ");
        verify(repository, never()).existsById(anyString());
        verify(repository, never()).save(any(IdempotencyKey.class));
    }

    @Test
    void testMarkAsProcessed_AlreadyExists() {
        when(repository.existsById("key-123")).thenReturn(true);
        idempotencyService.markAsProcessed("key-123");
        verify(repository, never()).save(any(IdempotencyKey.class));
    }

    @Test
    void testMarkAsProcessed_NewKey() {
        when(repository.existsById("key-123")).thenReturn(false);
        idempotencyService.markAsProcessed("key-123");
        verify(repository, times(1)).save(any(IdempotencyKey.class));
    }
}
