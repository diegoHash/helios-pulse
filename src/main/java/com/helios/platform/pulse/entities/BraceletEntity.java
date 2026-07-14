package com.helios.platform.pulse.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "etecc_cbt_record")
public class BraceletEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_number", nullable = false)
    private Integer serialNumber;

    @Column(name = "property", nullable = false)
    private String property;

    @Column(name = "type")
    private String type;

    @Column(name="delivery_date")
    private String deliveryDate;

    @Column(name="responsable_nombre")
    private String responsableNombre;

    @Column(name = "workshift")
    private String workshift;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "price")
    private Double price;

    @Column(name = "exchange_rate")
    private Double exchangeRate;

    @Column(name = "monto_pagado")
    private Double montoPagado;

    @Column(name = "referencia_pago_bs")
    private String referenciaPagoBs;

    @Column(name = "serial_billete_pago_usd")
    private String serialBilletePagoUsd;

    @Column(name = "observacion", columnDefinition = "VARCHAR(1000)")
    private String observacion;

    @Column(name = "representa_ingreso")
    private Boolean representaIngreso;

    @Column(name = "damaged")
    private Boolean damaged;

    @Column(name = "migrated_to_v2", columnDefinition = "boolean default false")
    private Boolean migratedToV2 = false;
}
