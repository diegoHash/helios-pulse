package com.helios.platform.pulse.service;

import lombok.RequiredArgsConstructor;
import com.helios.platform.pulse.entities.IdempotencyKey;
import com.helios.platform.pulse.repositories.IdempotencyKeyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyKeyRepository repository;

    public boolean isAlreadyProcessed(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        return repository.existsById(key);
    }

    public void markAsProcessed(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        if (!repository.existsById(key)) {
            IdempotencyKey idempotencyKey = new IdempotencyKey(key, LocalDateTime.now());
            repository.save(idempotencyKey);
        }
    }
}
