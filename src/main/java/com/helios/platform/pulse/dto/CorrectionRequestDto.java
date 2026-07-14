package com.helios.platform.pulse.dto;

import lombok.Data;

@Data
public class CorrectionRequestDto {
    private Long transactionId;
    private String fieldToModify;
    private String message;
}
