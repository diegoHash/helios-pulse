package com.helios.platform.pulse.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class BraceletDTO {
    private Long id;

    private List<PaymentDTO> payments;

    @JsonProperty("serial_number")
    @JsonAlias({"serialNumber", "serial_number"})
    private Integer serialNumber;

    private String property;
    private String type;

    @JsonProperty("delivery_date")
    @JsonAlias({"deliveryDate", "delivery_date"})
    private String deliveryDate;

    @JsonProperty("responsable_nombre")
    @JsonAlias({"responsableNombre", "responsable_nombre"})
    private String responsableNombre;

    private String workshift;

    private String paymentMethod;
    private Double price;

    @JsonProperty("exchange_rate")
    @JsonAlias({"exchangeRate", "exchange_rate"})
    private Double exchangeRate;

    @JsonProperty("monto_pagado")
    @JsonAlias({"montoPagado", "monto_pagado"})
    private Double montoPagado;

    @JsonProperty("referencia_pago_bs")
    @JsonAlias({"referenciaPagoBs", "referencia_pago_bs"})
    private String referenciaPagoBs;

    @JsonProperty("serial_billete_pago_usd")
    @JsonAlias({"serialBilletePagoUsd", "serial_billete_pago_usd"})
    private String serialBilletePagoUsd;

    private String observacion;

    @JsonProperty("representa_ingreso")
    @JsonAlias({"representaIngreso", "representa_ingreso"})
    private Boolean representaIngreso;

    private Boolean damaged;
}
