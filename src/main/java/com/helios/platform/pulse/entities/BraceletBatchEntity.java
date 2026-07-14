package com.helios.platform.pulse.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "etecc_cbt_bracelet_batch")
public class BraceletBatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_inicial", nullable = false)
    private Integer serialInicial;

    @Column(name = "serial_final", nullable = false)
    private Integer serialFinal;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "price")
    private Double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private TransactionEntityV2 transaction;
}
