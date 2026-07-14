package com.helios.platform.pulse.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "etecc_cbt_lote")
@Data
public class LoteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_date", nullable = false)
    private String deliveryDate;

    @Column(name = "first_serial", nullable = false)
    private Integer firstSerial;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "last_serial", nullable = false)
    private Integer lastSerial;

    @Column(name = "type", nullable = false)
    private String type; // Propietario, Invitado, Alquilado

    @Column(name = "remaining", nullable = false)
    private Integer remaining;
}
