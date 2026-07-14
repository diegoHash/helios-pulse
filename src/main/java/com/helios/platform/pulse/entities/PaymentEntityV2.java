package com.helios.platform.pulse.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "etecc_cbt_payment_v2")
public class PaymentEntityV2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private TransactionEntityV2 transaction;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "exchange_rate")
    private Double exchangeRate;

    @Column(name = "referencia_pago_bs")
    private String referenciaPagoBs;

    @Column(name = "serial_billete_pago_usd")
    private String serialBilletePagoUsd;
}
