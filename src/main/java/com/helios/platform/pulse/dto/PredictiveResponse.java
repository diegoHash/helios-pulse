package com.helios.platform.pulse.dto;

import lombok.Data;

@Data
public class PredictiveResponse {
    private int expectedAttendance;
    private int currentStock;
    private boolean isDeficit;
    private boolean isCrisis;
    private String holidayInfo;
    private String weatherCondition;
    private int rainProb;
    private String geminiInsight;
}
