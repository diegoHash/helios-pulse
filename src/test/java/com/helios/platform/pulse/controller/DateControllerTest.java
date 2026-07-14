package com.helios.platform.pulse.controller;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateControllerTest {

    private final DateController dateController = new DateController();

    @Test
    void testGetCurrentDate() {
        String result = dateController.getCurrentDate();
        assertNotNull(result);

        // Ensure result is a valid DayOfWeek
        boolean isValidDay = false;
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day.toString().equals(result)) {
                isValidDay = true;
                break;
            }
        }
        assertTrue(isValidDay);
    }
}
