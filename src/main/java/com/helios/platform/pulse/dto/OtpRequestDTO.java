package com.helios.platform.pulse.dto;

import lombok.Data;

@Data
public class OtpRequestDTO {
    private String username;
    private String method; // "EMAIL" o "TELEGRAM"
}
