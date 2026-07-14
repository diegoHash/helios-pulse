package com.helios.platform.pulse.dto;

import lombok.Data;

@Data
public class LoteDTO {
    private Long id;
    private String deliveryDate;
    private Integer firstSerial;
    private Integer quantity;
    private Integer lastSerial;
    private String type;
    private Integer remaining;
}
