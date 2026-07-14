package com.helios.platform.pulse.service;

import lombok.RequiredArgsConstructor;
import com.helios.platform.pulse.dto.RootUpdateTransactionRequest;
import com.helios.platform.pulse.entities.PaymentEntityV2;
import com.helios.platform.pulse.entities.TransactionEntityV2;
import com.helios.platform.pulse.repositories.IPaymentV2;
import com.helios.platform.pulse.repositories.ITransactionV2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RootService {

    private final ITransactionV2 transactionRepo;
    private final IPaymentV2 paymentRepo;
    private final CorrectionRequestService correctionRequestService;

    @Transactional
    public void superUpdateTransaction(Long transactionId, RootUpdateTransactionRequest request, Long requestIdToApprove) {
        Optional<TransactionEntityV2> optTx = transactionRepo.findById(transactionId);
        if (optTx.isEmpty()) {
            throw new RuntimeException("Transacción no encontrada");
        }
        TransactionEntityV2 tx = optTx.get();

        if (request.getFecha() != null) {
            tx.setDeliveryDate(request.getFecha());
        }
        if (request.getTipoBrazalete() != null) {
            tx.setType(request.getTipoBrazalete());
        }
        if (request.getInmueble() != null) {
            tx.setProperty(request.getInmueble());
        }
        if (request.getObservacion() != null) {
            tx.setObservacion(request.getObservacion());
        }

        transactionRepo.save(tx);

        if (request.getPagos() != null && !request.getPagos().isEmpty()) {
            for (RootUpdateTransactionRequest.PaymentUpdateDto paymentDto : request.getPagos()) {
                if (paymentDto.getId() != null) {
                    Optional<PaymentEntityV2> optPayment = paymentRepo.findById(paymentDto.getId());
                    if (optPayment.isPresent()) {
                        PaymentEntityV2 payment = optPayment.get();
                        // Solo debe pertenecer a la misma transacción por seguridad
                        if (payment.getTransaction().getId().equals(transactionId)) {
                            if (paymentDto.getMonto() != null) {
                                payment.setAmount(paymentDto.getMonto());
                            }
                            if (paymentDto.getFormaPago() != null) {
                                payment.setPaymentMethod(paymentDto.getFormaPago());
                            }
                            if (paymentDto.getReferencia() != null) {
                                String method = payment.getPaymentMethod() != null ? payment.getPaymentMethod().toUpperCase() : "";
                                boolean isUsdMethod = "ZELLE".equals(method) || "EFECTIVO".equals(method) || "PAGO_USD".equals(method) || "USD_CASH".equals(method) || "CASH_USD".equals(method);
                                if (isUsdMethod) {
                                    payment.setSerialBilletePagoUsd(paymentDto.getReferencia());
                                } else {
                                    payment.setReferenciaPagoBs(paymentDto.getReferencia());
                                }
                            }
                            paymentRepo.save(payment);
                        }
                    }
                }
            }
        }

        if (requestIdToApprove != null) {
            correctionRequestService.updateStatus(requestIdToApprove, "APPROVED");
        }
    }

    public TransactionEntityV2 getTransaction(Long id) {
        return transactionRepo.findById(id).orElseThrow(() -> new RuntimeException("Transaction not found"));
    }
}
