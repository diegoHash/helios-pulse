package com.helios.platform.pulse.controller;


import com.helios.platform.pulse.dto.response.ReportResponseV2DTO;
import com.helios.platform.pulse.service.BraceletServiceV2;
import com.helios.platform.pulse.service.IdempotencyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BraceletControllerV2Test {

    @Mock
    private BraceletServiceV2 braceletServiceV2;

    @Mock
    private IdempotencyService idempotencyService;

    @InjectMocks
    private BraceletControllerV2 braceletControllerV2;

    @Test
    void testSaveTransactions_IdempotentAlreadyProcessed() {
        when(idempotencyService.isAlreadyProcessed("key123")).thenReturn(true);

        ResponseEntity<?> response = braceletControllerV2.saveTransactions("key123", Collections.emptyList());

        assertEquals(200, response.getStatusCode().value());
        verify(braceletServiceV2, never()).saveTransactionsV2(any());
    }

    @Test
    void testSaveTransactions_Success() {
        when(idempotencyService.isAlreadyProcessed("key123")).thenReturn(false);
        doReturn(ResponseEntity.ok("Saved")).when(braceletServiceV2).saveTransactionsV2(any());

        ResponseEntity<?> response = braceletControllerV2.saveTransactions("key123", Collections.emptyList());

        assertEquals(200, response.getStatusCode().value());
        verify(idempotencyService, times(1)).markAsProcessed("key123");
    }

    @Test
    void testDeleteTransaction() {
        doNothing().when(braceletServiceV2).deleteTransaction(1L);

        ResponseEntity<?> response = braceletControllerV2.deleteTransaction(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Transaction deleted successfully", response.getBody());
    }

    @Test
    void testGetGeneralDayReport() {
        when(braceletServiceV2.getGeneralDayCount(any(), any())).thenReturn(5);
        when(braceletServiceV2.getGeneralDayReport(any(), any())).thenReturn(Collections.emptyList());

        ResponseEntity<ReportResponseV2DTO> response = braceletControllerV2.getGeneralDayReport("2026-01-01", null, "2026-01-02", null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(5, java.util.Objects.requireNonNull(response.getBody()).getQty());
    }

    @Test
    void testGetCustomDayReport() {
        when(braceletServiceV2.getCustomTypeCount(any(), any(), any())).thenReturn(3);
        when(braceletServiceV2.getCustomTypeReport(any(), any(), any())).thenReturn(Collections.emptyList());

        ResponseEntity<ReportResponseV2DTO> response = braceletControllerV2.getCustomDayReport("2026-01-01", null, "2026-01-02", null, "PROPIETARIO", null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(3, java.util.Objects.requireNonNull(response.getBody()).getQty());
    }

    @Test
    void testGetCustomReport_AllGeneral() {
        when(braceletServiceV2.getGeneralDayCount(any(), any())).thenReturn(10);
        when(braceletServiceV2.getGeneralDayReport(any(), any())).thenReturn(Collections.emptyList());

        ResponseEntity<ReportResponseV2DTO> response = braceletControllerV2.getCustomReport("2026-01-01", "2026-01-02", "GENERAL", "GENERAL");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(10, java.util.Objects.requireNonNull(response.getBody()).getQty());
    }

    @Test
    void testGetCustomReport_TypeFilter() {
        when(braceletServiceV2.getCustomTypeCount(any(), any(), eq("PROPIETARIO"))).thenReturn(7);
        when(braceletServiceV2.getCustomTypeReport(any(), any(), eq("PROPIETARIO"))).thenReturn(Collections.emptyList());

        ResponseEntity<ReportResponseV2DTO> response = braceletControllerV2.getCustomReport("2026-01-01", "2026-01-02", "PROPIETARIO", "GENERAL");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(7, java.util.Objects.requireNonNull(response.getBody()).getQty());
    }

    @Test
    void testGetCustomReport_WorkshiftFilter() {
        when(braceletServiceV2.getCustomWorkshiftCount(any(), any(), eq("MORNING"))).thenReturn(4);
        when(braceletServiceV2.getCustomWorkshiftReport(any(), any(), eq("MORNING"))).thenReturn(Collections.emptyList());

        ResponseEntity<ReportResponseV2DTO> response = braceletControllerV2.getCustomReport("2026-01-01", "2026-01-02", "GENERAL", "MORNING");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(4, java.util.Objects.requireNonNull(response.getBody()).getQty());
    }

    @Test
    void testGetCustomReport_TypeAndWorkshiftFilter() {
        when(braceletServiceV2.getCustomCount(any(), any(), eq("PROPIETARIO"), eq("MORNING"))).thenReturn(2);
        when(braceletServiceV2.getCustomReport(any(), any(), eq("PROPIETARIO"), eq("MORNING"))).thenReturn(Collections.emptyList());

        ResponseEntity<ReportResponseV2DTO> response = braceletControllerV2.getCustomReport("2026-01-01", "2026-01-02", "PROPIETARIO", "MORNING");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, java.util.Objects.requireNonNull(response.getBody()).getQty());
    }

    @Test
    void testGetCashClose() {
        com.helios.platform.pulse.dto.CashCloseDTO mockCashClose = new com.helios.platform.pulse.dto.CashCloseDTO();
        when(braceletServiceV2.getCashClose(any(), any())).thenReturn(mockCashClose);

        ResponseEntity<?> response = braceletControllerV2.getCashClose("2026-01-01", null, "PROPIETARIO", null);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testGetCashClose_MissingType() {
        ResponseEntity<?> response = braceletControllerV2.getCashClose("2026-01-01", null, null, null);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testGetOwnerHistory() {
        when(braceletServiceV2.getOwnerHistory("V100")).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = braceletControllerV2.getOwnerHistory("V100");

        assertEquals(200, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        org.junit.jupiter.api.Assertions.assertNotNull(body);
        assertEquals("success", body.get("status"));
    }

    @Test
    void testGetOwnerHistory_MissingPropertyId() {
        ResponseEntity<?> response = braceletControllerV2.getOwnerHistory(null);

        assertEquals(400, response.getStatusCode().value());
    }
}
