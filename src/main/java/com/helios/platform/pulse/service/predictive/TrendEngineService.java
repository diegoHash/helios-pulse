package com.helios.platform.pulse.service.predictive;

import lombok.RequiredArgsConstructor;
import com.helios.platform.pulse.dto.predictive.TrendDataDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TrendEngineService {

    private final ConsumptionEngineService consumptionEngine;

    public TrendDataDTO calculateTrend() {
        LocalDate today = LocalDate.now();

        // 30 days vs previous 30 days
        Integer current30 = consumptionEngine.getConsumptionForDays(today, 30);
        Integer previous30 = consumptionEngine.getConsumptionForDays(today.minusDays(30), 30);

        Double variation = 0.0;
        String trend = "Estabilidad";

        if (previous30 > 0) {
            variation = ((double) (current30 - previous30) / previous30) * 100.0;
        } else if (current30 > 0) {
            variation = 100.0; // from 0 to something is 100% growth for representation
        }

        if (variation > 5.0) {
            trend = "Crecimiento";
        } else if (variation < -5.0) {
            trend = "Disminucion";
        }

        return new TrendDataDTO(trend, Math.round(variation * 100.0) / 100.0);
    }
}
