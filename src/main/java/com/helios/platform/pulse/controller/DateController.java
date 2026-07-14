package com.helios.platform.pulse.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/helios/pulse/")
public class DateController {

    @RequestMapping("date")
    public String getCurrentDate() {
        LocalDateTime localDateTime = LocalDateTime.now();
        DayOfWeek day = localDateTime.getDayOfWeek();
        return day.toString();
    }
}
