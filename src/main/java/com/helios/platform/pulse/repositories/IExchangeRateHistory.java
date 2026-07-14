package com.helios.platform.pulse.repositories;

import com.helios.platform.pulse.entities.ExchangeRateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IExchangeRateHistory extends JpaRepository<ExchangeRateHistory, Long> {
    List<ExchangeRateHistory> findAllByOrderByEffectiveDateDesc();
}
