package com.helios.platform.pulse.controller;

import com.helios.platform.pulse.entities.ExchangeRateHistory;
import com.helios.platform.pulse.repositories.IExchangeRateHistory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/helios/pulse/exchange-rates")
public class ExchangeRateHistoryController {

    private final IExchangeRateHistory repository;

    public ExchangeRateHistoryController(IExchangeRateHistory repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ExchangeRateHistory> getAll() {
        return repository.findAllByOrderByEffectiveDateDesc();
    }

    @PostMapping
    public ExchangeRateHistory addRate(@RequestBody ExchangeRateHistory rate) {
        if (rate.getEffectiveDate() == null) {
            rate.setEffectiveDate(LocalDateTime.now());
        }
        return repository.save(rate);
    }
}
