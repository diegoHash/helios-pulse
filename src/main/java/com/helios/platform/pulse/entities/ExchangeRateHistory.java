package com.helios.platform.pulse.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rate_history")
public class ExchangeRateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double rate;

    @Column(nullable = false)
    private LocalDateTime effectiveDate;

    public ExchangeRateHistory() {}

    public ExchangeRateHistory(Double rate, LocalDateTime effectiveDate) {
        this.rate = rate;
        this.effectiveDate = effectiveDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getRate() { return rate; }
    public void setRate(Double rate) { this.rate = rate; }
    public LocalDateTime getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDateTime effectiveDate) { this.effectiveDate = effectiveDate; }
}
