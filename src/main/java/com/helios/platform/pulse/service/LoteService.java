package com.helios.platform.pulse.service;

import lombok.RequiredArgsConstructor;
import com.helios.platform.pulse.dto.LoteDTO;
import com.helios.platform.pulse.entities.LoteEntity;
import com.helios.platform.pulse.repositories.ILoteRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoteService {

    private final ILoteRepo loteRepo;

    public List<LoteDTO> getAllLotes() {
        return loteRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public LoteDTO createLote(LoteDTO dto) {
        LoteEntity entity = new LoteEntity();
        entity.setDeliveryDate(dto.getDeliveryDate());
        entity.setFirstSerial(dto.getFirstSerial());
        entity.setQuantity(dto.getQuantity());
        entity.setLastSerial(dto.getFirstSerial() + dto.getQuantity() - 1);
        entity.setType(dto.getType());
        entity.setRemaining(dto.getQuantity());

        return toDTO(loteRepo.save(entity));
    }

    public LoteDTO updateLote(Long id, LoteDTO dto) {
        LoteEntity entity = loteRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Lote not found"));

        entity.setDeliveryDate(dto.getDeliveryDate());
        entity.setFirstSerial(dto.getFirstSerial());
        entity.setQuantity(dto.getQuantity());
        entity.setLastSerial(dto.getFirstSerial() + dto.getQuantity() - 1);
        entity.setType(dto.getType());
        entity.setRemaining(dto.getRemaining());

        return toDTO(loteRepo.save(entity));
    }

    public void deleteLote(Long id) {
        loteRepo.deleteById(id);
    }

    private LoteDTO toDTO(LoteEntity entity) {
        LoteDTO dto = new LoteDTO();
        dto.setId(entity.getId());
        dto.setDeliveryDate(entity.getDeliveryDate());
        dto.setFirstSerial(entity.getFirstSerial());
        dto.setQuantity(entity.getQuantity());
        dto.setLastSerial(entity.getLastSerial());
        dto.setType(entity.getType());
        dto.setRemaining(entity.getRemaining());
        return dto;
    }
}
