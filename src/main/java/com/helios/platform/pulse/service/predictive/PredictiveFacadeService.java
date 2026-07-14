package com.helios.platform.pulse.service.predictive;

import lombok.RequiredArgsConstructor;
import com.helios.platform.pulse.dto.predictive.ConsumptionDataDTO;
import com.helios.platform.pulse.dto.predictive.PredictiveDashboardDTO;
import com.helios.platform.pulse.dto.predictive.RiskDataDTO;
import com.helios.platform.pulse.dto.predictive.TrendDataDTO;
import com.helios.platform.pulse.repositories.ILoteRepo;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PredictiveFacadeService {

    private final ConsumptionEngineService consumptionEngine;
    private final TrendEngineService trendEngine;
    private final RiskEngineService riskEngine;
    private final ILoteRepo loteRepo;

    public PredictiveDashboardDTO getDashboardData() {
        PredictiveDashboardDTO dashboard = new PredictiveDashboardDTO();

        // 1. Inventario Actual
        Integer inventarioActual = loteRepo.sumRemaining();
        dashboard.setInventarioActual(inventarioActual == null ? 0 : inventarioActual);

        // 2. Consumo Histórico y Ponderado
        ConsumptionDataDTO consumo = consumptionEngine.calculateHistoricalConsumption();
        dashboard.setConsumo(consumo);

        // 3. Tendencias
        TrendDataDTO tendencia = trendEngine.calculateTrend();
        dashboard.setTendencia(tendencia);

        // 4. Riesgo
        RiskDataDTO riesgo = riskEngine.calculateRisk(consumo.getConsumoPonderado());
        dashboard.setRiesgo(riesgo);

        // Mock placeholders for Phase 2 implementation
        dashboard.setEventoProximo("Día de la Independencia (Mock)");
        dashboard.setDiasParaEvento(15);
        dashboard.setDemandaEstimada(450.0);
        dashboard.setVisitantesHistoricos(400);
        dashboard.setFactorClimatico("Moderado");
        dashboard.setAiInterpretation("Generando análisis con IA... (Fase 4)");

        return dashboard;
    }
}
