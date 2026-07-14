package com.helios.platform.pulse.dto;

import lombok.Data;

@Data
public class PaymentMethodSummaryDTO {
    private String paymentMethod;
    private Double totalAmount;
    private Double totalAmountUsd;
}
