package com.helios.platform.pulse.dto.predictive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumptionDataDTO {
    private Integer promedio7;
    private Integer promedio15;
    private Integer promedio30;
    private Integer promedio90;
    private Integer promedio180;
    private Integer promedioAnual;
    private Double consumoPonderado;
}
