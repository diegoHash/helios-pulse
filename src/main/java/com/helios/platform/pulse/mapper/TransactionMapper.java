package com.helios.platform.pulse.mapper;

import com.helios.platform.pulse.dto.BraceletBatchDTO;
import com.helios.platform.pulse.dto.PaymentBatchDTO;
import com.helios.platform.pulse.dto.TransactionBatchDTO;
import com.helios.platform.pulse.entities.BraceletBatchEntity;
import com.helios.platform.pulse.entities.PaymentEntityV2;
import com.helios.platform.pulse.entities.TransactionEntityV2;

import java.util.List;

public interface TransactionMapper {

    TransactionBatchDTO toDto(TransactionEntityV2 entity);

    List<TransactionBatchDTO> toDtoList(List<TransactionEntityV2> entities);

    PaymentBatchDTO paymentToDto(PaymentEntityV2 payment);

    BraceletBatchDTO braceletToDto(BraceletBatchEntity batch);
}
