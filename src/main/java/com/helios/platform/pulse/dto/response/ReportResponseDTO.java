package com.helios.platform.pulse.dto.response;

import lombok.Data;
import com.helios.platform.pulse.dto.BraceletDTO;

import java.util.List;

@Data
public class ReportResponseDTO {
    private int qty;
    private List<BraceletDTO> data;
}
