package com.helios.platform.pulse.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

class HealthControllerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private HealthController healthController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHealthCheck() {
        // Assume db is up
        ResponseEntity<String> response = healthController.healthCheck();
        assertEquals(200, response.getStatusCode().value());
        assertEquals("OK", response.getBody());
    }
}
