package com.helios.platform.pulse.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaymentBatchDTO {
    private Long id;
    private String paymentMethod;
    private Double amount;

    // Frontend expects: p.tasaBcv
    @JsonProperty("tasaBcv")
    @JsonAlias({"tasaBcv", "exchangeRate", "exchange_rate"})
    private Double tasaBcv;

    private String referenciaPagoBs;
    private String serialBilletePagoUsd;
}
