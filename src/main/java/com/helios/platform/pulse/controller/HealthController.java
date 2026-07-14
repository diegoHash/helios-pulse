package com.helios.platform.pulse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/helios/pulse")
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        try {
            // Verifica la conectividad con la base de datos central AWS RDS
            jdbcTemplate.execute("SELECT 1");
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            // Si la base de datos no es accesible, responde con 503 para indicar modo offline
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("DATABASE_OFFLINE");
        }
    }
}
