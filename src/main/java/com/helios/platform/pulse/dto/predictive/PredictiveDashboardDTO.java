package com.helios.platform.pulse.dto.predictive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictiveDashboardDTO {
    private Integer inventarioActual;

    private ConsumptionDataDTO consumo;
    private TrendDataDTO tendencia;
    private RiskDataDTO riesgo;

    private String eventoProximo;
    private Integer diasParaEvento;
    private Double demandaEstimada;
    private Integer visitantesHistoricos;

    private String factorClimatico;

    private String aiInterpretation;
}
