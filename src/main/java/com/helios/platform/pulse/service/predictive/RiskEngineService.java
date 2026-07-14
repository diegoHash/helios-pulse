package com.helios.platform.pulse.service.predictive;

import lombok.RequiredArgsConstructor;
import com.helios.platform.pulse.dto.predictive.RiskDataDTO;
import com.helios.platform.pulse.repositories.ILoteRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class RiskEngineService {

    private final ILoteRepo loteRepo;

    public RiskDataDTO calculateRisk(Double consumoPonderado) {
        Integer inventarioActual = loteRepo.sumRemaining();
        if (inventarioActual == null) {
            inventarioActual = 0;
        }

        int diasRestantes = 0;
        if (consumoPonderado > 0) {
            diasRestantes = (int) Math.floor(inventarioActual / consumoPonderado);
        } else {
            diasRestantes = 999; // Represents practically infinite days if consumption is 0
        }

        LocalDate fechaAgotamiento = LocalDate.now().plusDays(diasRestantes);
        String fechaEstStr = fechaAgotamiento.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        int scoreRiesgo = 1;
        if (diasRestantes <= 2) {
            scoreRiesgo = 10;
        } else if (diasRestantes <= 5) {
            scoreRiesgo = 8;
        } else if (diasRestantes <= 10) {
            scoreRiesgo = 5;
        } else if (diasRestantes <= 15) {
            scoreRiesgo = 3;
        } else {
            scoreRiesgo = 1;
        }

        return new RiskDataDTO(diasRestantes, fechaEstStr, scoreRiesgo);
    }
}
