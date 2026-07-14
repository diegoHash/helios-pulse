package com.helios.platform.pulse.dto;

import lombok.Data;
import java.util.List;

@Data
public class CashCloseDTO {
    private String date;
    private String type;
    private Integer totalBracelets;
    private Double totalAmountUsd;
    private List<PaymentMethodSummaryDTO> paymentsSummary;
}
