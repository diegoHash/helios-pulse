package com.helios.platform.pulse.service.predictive;

import lombok.RequiredArgsConstructor;
import com.helios.platform.pulse.dto.predictive.ConsumptionDataDTO;
import com.helios.platform.pulse.repositories.ITransactionV2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ConsumptionEngineService {

    private final ITransactionV2 transactionRepository;

    public ConsumptionDataDTO calculateHistoricalConsumption() {
        LocalDate today = LocalDate.now();

        Integer sum7 = getConsumptionForDays(today, 7);
        Integer sum15 = getConsumptionForDays(today, 15);
        Integer sum30 = getConsumptionForDays(today, 30);
        Integer sum90 = getConsumptionForDays(today, 90);
        Integer sum180 = getConsumptionForDays(today, 180);
        Integer sum365 = getConsumptionForDays(today, 365);

        // Averages per day
        Integer avg7 = (int) Math.ceil((double) sum7 / 7);
        Integer avg15 = (int) Math.ceil((double) sum15 / 15);
        Integer avg30 = (int) Math.ceil((double) sum30 / 30);
        Integer avg90 = (int) Math.ceil((double) sum90 / 90);
        Integer avg180 = (int) Math.ceil((double) sum180 / 180);
        Integer avg365 = (int) Math.ceil((double) sum365 / 365);

        // Consumo ponderado (50% 7 dias, 30% 30 dias, 20% 90 dias)
        Double consumoPonderado = (avg7 * 0.50) + (avg30 * 0.30) + (avg90 * 0.20);

        return new ConsumptionDataDTO(avg7, avg15, avg30, avg90, avg180, avg365, consumoPonderado);
    }

    public Integer getConsumptionForDays(LocalDate endDateInclusive, int daysBack) {
        LocalDate startDate = endDateInclusive.minusDays(daysBack - 1); // e.g. last 7 days = today to 6 days ago

        String startStr = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00";
        String endStr = endDateInclusive.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 23:59";

        Integer sum = transactionRepository.sumCantidadByDeliveryDateBetween(startStr, endStr);
        return sum == null ? 0 : sum;
    }
}
