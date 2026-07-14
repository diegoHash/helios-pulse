package com.helios.platform.pulse.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class TransactionBatchDTO {
    private Long id;

    @NotBlank(message = "Property cannot be blank")
    private String property;

    // Frontend expects: tx.deliveryDate
    private String deliveryDate;

    // Frontend expects: tx.responsableNombre
    private String responsableNombre;

    @NotBlank(message = "Workshift cannot be blank")
    private String workshift;

    @NotBlank(message = "Type cannot be blank")
    private String type;

    // Frontend expects: tx.representaIngreso
    @NotNull(message = "representaIngreso cannot be null")
    private Boolean representaIngreso;

    @Size(max = 1000, message = "Observation too long")
    private String observacion;

    private List<PaymentBatchDTO> payments;

    // Frontend expects: tx.batches
    @JsonProperty("batches")
    @JsonAlias({"batches", "braceletBatches", "bracelet_batches"})
    private List<BraceletBatchDTO> batches;
}
