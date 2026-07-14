package com.helios.platform.pulse.dto.response;

import lombok.Data;
import com.helios.platform.pulse.dto.TransactionBatchDTO;

import java.util.List;

@Data
public class ReportResponseV2DTO {
    private Integer qty;
    private List<TransactionBatchDTO> data;
}
