package com.helios.platform.pulse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BraceletBatchDTO {
    private Long id;

    @NotNull(message = "serialInicial cannot be null")
    @Min(value = 0, message = "serialInicial must be positive")
    private Integer serialInicial;

    @NotNull(message = "serialFinal cannot be null")
    @Min(value = 0, message = "serialFinal must be positive")
    private Integer serialFinal;

    @NotNull(message = "cantidad cannot be null")
    @Min(value = 1, message = "cantidad must be at least 1")
    private Integer cantidad;

    @Min(value = 0, message = "price must be positive")
    private Double price;
}
