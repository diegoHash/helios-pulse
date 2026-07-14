package com.helios.platform.pulse.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "etecc_cbt_transaction_v2")
public class TransactionEntityV2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property", nullable = false)
    private String property;

    @Column(name="delivery_date")
    private String deliveryDate;

    @Column(name="responsable_nombre")
    private String responsableNombre;

    @Column(name = "workshift")
    private String workshift;

    @Column(name = "type")
    private String type;

    @Column(name = "representa_ingreso")
    private Boolean representaIngreso;

    @Column(name = "observacion", columnDefinition = "VARCHAR(1000)")
    private String observacion;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BraceletBatchEntity> batches;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentEntityV2> payments;
}
