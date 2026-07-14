package com.helios.platform.pulse.mapper;

import com.helios.platform.pulse.dto.BraceletBatchDTO;
import com.helios.platform.pulse.dto.PaymentBatchDTO;
import com.helios.platform.pulse.dto.TransactionBatchDTO;
import com.helios.platform.pulse.entities.BraceletBatchEntity;
import com.helios.platform.pulse.entities.PaymentEntityV2;
import com.helios.platform.pulse.entities.TransactionEntityV2;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionMapperImpl implements TransactionMapper {

    @Override
    public TransactionBatchDTO toDto(TransactionEntityV2 entity) {
        if (entity == null) {
            return null;
        }
        TransactionBatchDTO dto = new TransactionBatchDTO();
        dto.setId(entity.getId());
        dto.setProperty(entity.getProperty());
        dto.setDeliveryDate(entity.getDeliveryDate());
        dto.setResponsableNombre(entity.getResponsableNombre());
        dto.setWorkshift(entity.getWorkshift());
        dto.setType(entity.getType());
        dto.setRepresentaIngreso(entity.getRepresentaIngreso());
        dto.setObservacion(entity.getObservacion());

        if (entity.getPayments() != null) {
            dto.setPayments(entity.getPayments().stream()
                .map(this::paymentToDto)
                .collect(Collectors.toList()));
        }

        if (entity.getBatches() != null) {
            dto.setBatches(entity.getBatches().stream()
                .map(this::braceletToDto)
                .collect(Collectors.toList()));
        }
        return dto;
    }

    @Override
    public List<TransactionBatchDTO> toDtoList(List<TransactionEntityV2> entities) {
        if (entities == null) {
            return null;
        }
        List<TransactionBatchDTO> list = new ArrayList<>(entities.size());
        for (TransactionEntityV2 entity : entities) {
            list.add(toDto(entity));
        }
        return list;
    }

    @Override
    public PaymentBatchDTO paymentToDto(PaymentEntityV2 payment) {
        if (payment == null) {
            return null;
        }
        PaymentBatchDTO dto = new PaymentBatchDTO();
        dto.setId(payment.getId());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setAmount(payment.getAmount());
        dto.setTasaBcv(payment.getExchangeRate());
        dto.setReferenciaPagoBs(payment.getReferenciaPagoBs());
        dto.setSerialBilletePagoUsd(payment.getSerialBilletePagoUsd());
        return dto;
    }

    @Override
    public BraceletBatchDTO braceletToDto(BraceletBatchEntity batch) {
        if (batch == null) {
            return null;
        }
        BraceletBatchDTO dto = new BraceletBatchDTO();
        dto.setId(batch.getId());
        dto.setSerialInicial(batch.getSerialInicial());
        dto.setSerialFinal(batch.getSerialFinal());
        dto.setCantidad(batch.getCantidad());
        dto.setPrice(batch.getPrice());
        return dto;
    }
}
