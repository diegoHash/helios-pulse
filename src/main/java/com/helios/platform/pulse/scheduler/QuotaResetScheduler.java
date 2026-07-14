package com.helios.platform.pulse.scheduler;

import lombok.extern.slf4j.Slf4j;
import com.helios.platform.pulse.service.PulseOwnerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QuotaResetScheduler {

    private final PulseOwnerService ownerService;

    public QuotaResetScheduler(PulseOwnerService ownerService) {
        this.ownerService = ownerService;
    }

    /**
     * Resets the owner bracelet quotas every Monday at 00:00 America/Caracas time.
     * Consults external web services to verify the day of the week in Venezuela.
     */
    @Scheduled(cron = "0 0 0 * * MON", zone = "America/Caracas")
    public void resetQuotasWeekly() {
        log.info("QuotaResetScheduler triggered. Consulting external web services for Caracas time...");
        boolean isMonday = false;

        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            // Timeout configurations to prevent hanging indefinitely
            org.springframework.http.client.SimpleClientHttpRequestFactory requestFactory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(5000);
            requestFactory.setReadTimeout(5000);
            restTemplate.setRequestFactory(requestFactory);

            log.info("Fetching time from WorldTimeAPI...");
            java.util.Map<?, ?> response = restTemplate.getForObject("http://worldtimeapi.org/api/timezone/America/Caracas", java.util.Map.class);
            if (response != null && response.containsKey("day_of_week")) {
                int dayOfWeek = ((Number) response.get("day_of_week")).intValue();
                // worldtimeapi: 1 is Monday
                if (dayOfWeek == 1) {
                    isMonday = true;
                    log.info("WorldTimeAPI verification: Today is Monday in Caracas.");
                } else {
                    log.warn("WorldTimeAPI verification: Today is day {} of the week (not Monday).", dayOfWeek);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch time from WorldTimeAPI: {}. Trying fallback TimeAPI...", e.getMessage());
            try {
                org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
                org.springframework.http.client.SimpleClientHttpRequestFactory requestFactory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
                requestFactory.setConnectTimeout(5000);
                requestFactory.setReadTimeout(5000);
                restTemplate.setRequestFactory(requestFactory);

                java.util.Map<?, ?> response = restTemplate.getForObject("https://timeapi.io/api/Time/current/zone?timeZone=America/Caracas", java.util.Map.class);
                if (response != null && response.containsKey("dayOfWeek")) {
                    String dayOfWeek = (String) response.get("dayOfWeek");
                    if ("Monday".equalsIgnoreCase(dayOfWeek)) {
                        isMonday = true;
                        log.info("TimeAPI verification: Today is Monday in Caracas.");
                    } else {
                        log.warn("TimeAPI verification: Today is {} (not Monday).", dayOfWeek);
                    }
                }
            } catch (Exception ex) {
                log.error("Failed to fetch time from fallback TimeAPI: {}. Falling back to JVM America/Caracas ZonedDateTime...", ex.getMessage());
                java.time.ZonedDateTime nowCaracas = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Caracas"));
                if (nowCaracas.getDayOfWeek() == java.time.DayOfWeek.MONDAY) {
                    isMonday = true;
                    log.info("JVM Clock verification: Today is Monday in Caracas.");
                } else {
                    log.warn("JVM Clock verification: Today is {} (not Monday).", nowCaracas.getDayOfWeek());
                }
            }
        }

        if (isMonday) {
            log.info("Time verification successful. Starting owner quota reset...");
            try {
                ownerService.reset();
                log.info("Weekly quota reset for owners completed successfully.");
            } catch (Exception e) {
                log.error("Failed to execute weekly quota reset for owners: {}", e.getMessage(), e);
            }
        } else {
            log.warn("Requisite day (Monday in America/Caracas) not satisfied. Quota reset skipped.");
        }
    }
}
