package com.helios.platform.pulse.controller;

import com.helios.platform.pulse.entities.ExchangeRateHistory;
import com.helios.platform.pulse.repositories.IExchangeRateHistory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateHistoryControllerTest {

    @Mock
    private IExchangeRateHistory repository;

    @InjectMocks
    private ExchangeRateHistoryController controller;

    @Test
    void testGetAll() {
        ExchangeRateHistory rate = new ExchangeRateHistory();
        rate.setRate(36.5);
        when(repository.findAllByOrderByEffectiveDateDesc()).thenReturn(Collections.singletonList(rate));

        List<ExchangeRateHistory> result = controller.getAll();
        assertEquals(1, result.size());
        assertEquals(36.5, result.get(0).getRate());
    }

    @Test
    void testAddRate_WithDate() {
        ExchangeRateHistory rate = new ExchangeRateHistory();
        rate.setRate(36.5);
        LocalDateTime date = LocalDateTime.now();
        rate.setEffectiveDate(date);

        when(repository.save(any(ExchangeRateHistory.class))).thenAnswer(i -> i.getArguments()[0]);

        ExchangeRateHistory result = controller.addRate(rate);
        assertEquals(36.5, result.getRate());
        assertEquals(date, result.getEffectiveDate());
    }

    @Test
    void testAddRate_WithoutDate() {
        ExchangeRateHistory rate = new ExchangeRateHistory();
        rate.setRate(36.5);

        when(repository.save(any(ExchangeRateHistory.class))).thenAnswer(i -> i.getArguments()[0]);

        ExchangeRateHistory result = controller.addRate(rate);
        assertEquals(36.5, result.getRate());
        assertNotNull(result.getEffectiveDate());
    }
}
