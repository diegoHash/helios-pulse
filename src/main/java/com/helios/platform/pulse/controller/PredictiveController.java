package com.helios.platform.pulse.controller;

import lombok.RequiredArgsConstructor;
import com.helios.platform.pulse.dto.predictive.PredictiveDashboardDTO;
import com.helios.platform.pulse.service.predictive.PredictiveFacadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/predictive")
@RequiredArgsConstructor
public class PredictiveController {

    private final PredictiveFacadeService predictiveFacade;

    @GetMapping("/dashboard")
    public ResponseEntity<PredictiveDashboardDTO> getDashboard() {
        PredictiveDashboardDTO data = predictiveFacade.getDashboardData();
        return ResponseEntity.ok(data);
    }
}
