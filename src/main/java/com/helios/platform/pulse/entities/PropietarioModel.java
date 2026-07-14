package com.helios.platform.pulse.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "propietario")
@Data
public class PropietarioModel{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id_propietario")
    private Long idPropietario;

    @Column(name = "fullname")
    @JsonProperty("fullName")
    private String fullName;

    @Column(name = "num_property", columnDefinition = "VARCHAR(45)", nullable = false)
    @JsonProperty("num_property")
    private String numProperty;

    @Column(name = "state")
    @JsonProperty("state")
    private String state;

    @Column(name = "agreement", columnDefinition = "TINYINT")
    @JsonProperty("agreement")
    private Boolean agreement;

    @Column(name = "alicuota", columnDefinition = "DOUBLE")
    @JsonProperty("alicuota")
    private Double alicuota;

    @Column(name = "cupo_brazalete", columnDefinition = "INTEGER")
    @JsonProperty("cupo_brazalete")
    private Integer cupoBrazalete;

    @Column(name = "cupo_restante_brazalete", columnDefinition = "INTEGER")
    @JsonProperty("cupo_restante_brazalete")
    private Integer cupoRestanteBrazalete;

    @Column(name = "id_telegram", columnDefinition = "BIGINT")
    @JsonProperty("id_telegram")
    private Long idTelegram;

    @Column(name = "telefono", columnDefinition = "VARCHAR(25)")
    @JsonProperty("telefono")
    private String telefono;

}
