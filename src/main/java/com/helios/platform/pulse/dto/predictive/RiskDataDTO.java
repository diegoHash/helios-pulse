package com.helios.platform.pulse.dto.predictive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskDataDTO {
    private Integer diasRestantes;
    private String fechaEstimadaAgotamiento;
    private Integer scoreRiesgo; // 1 to 10
}
