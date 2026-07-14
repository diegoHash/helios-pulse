package com.helios.platform.pulse.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class TransactionDTO {
    private Long id;

    private String property;

    @JsonProperty("delivery_date")
    @JsonAlias({"deliveryDate", "delivery_date"})
    private String deliveryDate;

    @JsonProperty("responsable_nombre")
    @JsonAlias({"responsableNombre", "responsable_nombre"})
    private String responsableNombre;

    private String workshift;
    private String type;

    @JsonProperty("representa_ingreso")
    @JsonAlias({"representaIngreso", "representa_ingreso"})
    private Boolean representaIngreso;

    private String observacion;

    private List<PaymentDTO> payments;
    private List<BraceletDTO> bracelets;
}
