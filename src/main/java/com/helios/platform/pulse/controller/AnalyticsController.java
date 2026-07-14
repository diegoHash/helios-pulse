package com.helios.platform.pulse.controller;

import com.helios.platform.pulse.dto.PredictiveResponse;
import com.helios.platform.pulse.service.PredictiveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/helios/pulse/analytics")
@CrossOrigin(originPatterns = "*")
public class AnalyticsController {

    private final PredictiveService predictiveService;

    public AnalyticsController(PredictiveService predictiveService) {
        this.predictiveService = predictiveService;
    }

    @GetMapping("/predictive")
    public ResponseEntity<PredictiveResponse> getPredictiveForecast() {
        return ResponseEntity.ok(predictiveService.generateForecast());
    }
}
