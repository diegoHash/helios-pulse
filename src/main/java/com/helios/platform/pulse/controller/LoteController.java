package com.helios.platform.pulse.controller;

import lombok.RequiredArgsConstructor;
import com.helios.platform.pulse.dto.LoteDTO;
import com.helios.platform.pulse.service.LoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/helios/pulse/lotes")
@RequiredArgsConstructor
public class LoteController {

    private final LoteService loteService;

    @GetMapping
    public ResponseEntity<List<LoteDTO>> getAllLotes() {
        return ResponseEntity.ok(loteService.getAllLotes());
    }

    @PostMapping
    public ResponseEntity<LoteDTO> createLote(@RequestBody LoteDTO dto) {
        return ResponseEntity.ok(loteService.createLote(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoteDTO> updateLote(@PathVariable("id") Long id, @RequestBody LoteDTO dto) {
        return ResponseEntity.ok(loteService.updateLote(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLote(@PathVariable("id") Long id) {
        loteService.deleteLote(id);
        return ResponseEntity.ok().build();
    }
}
