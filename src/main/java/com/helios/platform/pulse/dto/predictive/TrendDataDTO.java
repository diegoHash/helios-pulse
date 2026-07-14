package com.helios.platform.pulse.dto.predictive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataDTO {
    private String tendencia; // Crecimiento, Estabilidad, Disminucion
    private Double porcentajeVariacion;
}
