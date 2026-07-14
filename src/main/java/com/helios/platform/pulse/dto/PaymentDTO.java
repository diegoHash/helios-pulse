package com.helios.platform.pulse.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaymentDTO {
    private Long id;

    @JsonProperty("payment_method")
    @JsonAlias({"paymentMethod", "payment_method"})
    private String paymentMethod;

    private Double amount;

    @JsonProperty("exchange_rate")
    @JsonAlias({"exchangeRate", "exchange_rate"})
    private Double exchangeRate;

    @JsonProperty("referencia_pago_bs")
    @JsonAlias({"referenciaPagoBs", "referencia_pago_bs"})
    private String referenciaPagoBs;

    @JsonProperty("serial_billete_pago_usd")
    @JsonAlias({"serialBilletePagoUsd", "serial_billete_pago_usd"})
    private String serialBilletePagoUsd;
}
