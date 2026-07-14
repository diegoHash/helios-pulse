package com.helios.platform.pulse.controller;

import com.helios.platform.pulse.dto.TransactionBatchDTO;
import com.helios.platform.pulse.dto.response.ReportResponseV2DTO;
import com.helios.platform.pulse.service.BraceletServiceV2;
import com.helios.platform.pulse.repositories.ITransactionV2;
import com.helios.platform.pulse.repositories.IPulseOwnerRepository;
import com.helios.platform.pulse.service.NotificationService;
import com.helios.platform.pulse.mapper.TransactionMapper;
import com.helios.platform.pulse.repositories.ILoteRepo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/helios/pulse/v2/")
@org.springframework.validation.annotation.Validated
public class BraceletControllerV2 {

    private final BraceletServiceV2 braceletServiceV2;
    private final com.helios.platform.pulse.service.IdempotencyService idempotencyService;
    private final ITransactionV2 transactionV2Repository;
    private final IPulseOwnerRepository cbtOwnerService;
    private final NotificationService notificationService;
    private final TransactionMapper transactionMapper;
    private final ILoteRepo loteRepo;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BraceletControllerV2(BraceletServiceV2 braceletServiceV2, com.helios.platform.pulse.service.IdempotencyService idempotencyService, ITransactionV2 transactionV2Repository, IPulseOwnerRepository cbtOwnerService, NotificationService notificationService, TransactionMapper transactionMapper, ILoteRepo loteRepo, JdbcTemplate jdbcTemplate) {
        this.braceletServiceV2 = braceletServiceV2;
        this.idempotencyService = idempotencyService;
        this.transactionV2Repository = transactionV2Repository;
        this.cbtOwnerService = cbtOwnerService;
        this.notificationService = notificationService;
        this.transactionMapper = transactionMapper;
        this.loteRepo = loteRepo;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("save")
    public ResponseEntity<?> saveTransactions(
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @jakarta.validation.Valid @RequestBody List<TransactionBatchDTO> dtoList) {

        if (idempotencyKey != null && idempotencyService.isAlreadyProcessed(idempotencyKey)) {
            return ResponseEntity.ok().body(java.util.Map.of("message", "Transaction already processed successfully (Idempotent)"));
        }

        ResponseEntity<?> response = braceletServiceV2.saveTransactionsV2(dtoList);

        if (idempotencyKey != null && response.getStatusCode().is2xxSuccessful()) {
            idempotencyService.markAsProcessed(idempotencyKey);
        }

        return response;
    }

    @org.springframework.security.access.prepost.PreAuthorize("@securityService.isTransactionOwner(authentication, #id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable("id") Long id) {
        braceletServiceV2.deleteTransaction(id);
        return ResponseEntity.ok().body("Transaction deleted successfully");
    }

    @GetMapping("/generalDayReport")
    public ResponseEntity<ReportResponseV2DTO> getGeneralDayReport(
            @RequestHeader(value = "fromDate", required = false) String fromDateHeader,
            @RequestParam(value = "fromDate", required = false) String fromDateParam,
            @RequestHeader(value = "toDate", required = false) String toDateHeader,
            @RequestParam(value = "toDate", required = false) String toDateParam) {

        String resolvedFromDate = fromDateHeader != null ? fromDateHeader : fromDateParam;
        String resolvedToDate = toDateHeader != null ? toDateHeader : toDateParam;

        if (resolvedFromDate != null && resolvedFromDate.length() > 10)
            resolvedFromDate = resolvedFromDate.substring(0, 10);
        if (resolvedToDate != null && resolvedToDate.length() > 10)
            resolvedToDate = resolvedToDate.substring(0, 10);

        String fromDateFull = formatStartOfDay(resolvedFromDate);
        String toDateFull   = formatEndOfDay(resolvedToDate);

        Integer qty = braceletServiceV2.getGeneralDayCount(fromDateFull, toDateFull);
        List<TransactionBatchDTO> transactions = braceletServiceV2.getGeneralDayReport(fromDateFull,
                toDateFull);
        ReportResponseV2DTO reportResponse = new ReportResponseV2DTO();
        reportResponse.setQty(qty);
        reportResponse.setData(transactions);
        return ResponseEntity.ok().body(reportResponse);
    }

    @GetMapping("/customDayReport")
    public ResponseEntity<ReportResponseV2DTO> getCustomDayReport(
            @RequestHeader(value = "fromDate", required = false) String fromDateHeader,
            @RequestParam(value = "fromDate", required = false) String fromDateParam,
            @RequestHeader(value = "toDate", required = false) String toDateHeader,
            @RequestParam(value = "toDate", required = false) String toDateParam,
            @RequestHeader(value = "type", required = false) String typeHeader,
            @RequestParam(value = "type", required = false) String typeParam) {

        String resolvedFromDate = fromDateHeader != null ? fromDateHeader : fromDateParam;
        String resolvedToDate = toDateHeader != null ? toDateHeader : toDateParam;
        String resolvedType = typeHeader != null ? typeHeader : typeParam;

        if (resolvedFromDate != null && resolvedFromDate.length() > 10)
            resolvedFromDate = resolvedFromDate.substring(0, 10);
        if (resolvedToDate != null && resolvedToDate.length() > 10)
            resolvedToDate = resolvedToDate.substring(0, 10);

        String fromDateFull = formatStartOfDay(resolvedFromDate);
        String toDateFull   = formatEndOfDay(resolvedToDate);

        Integer qty = braceletServiceV2.getCustomTypeCount(fromDateFull, toDateFull, resolvedType);
        List<TransactionBatchDTO> transactions = braceletServiceV2.getCustomTypeReport(fromDateFull, toDateFull,
                resolvedType);
        ReportResponseV2DTO reportResponse = new ReportResponseV2DTO();
        reportResponse.setQty(qty);
        reportResponse.setData(transactions);
        return ResponseEntity.ok().body(reportResponse);
    }

    @GetMapping("/customReport")
    public ResponseEntity<ReportResponseV2DTO> getCustomReport(
            @RequestHeader(value = "fromDate", required = true) String fromDate,
            @RequestHeader(value = "toDate",   required = true) String toDate,
            @RequestHeader(value = "type",      required = true) String type,
            @RequestHeader(value = "workshift", required = true) String workshift) {

        String fromDateFull = formatStartOfDay(fromDate);
        String toDateFull   = formatEndOfDay(toDate);

        // Normalizar valores de tipo y turno
        String normalizedType      = (type == null || type.isBlank()) ? "GENERAL" : type.trim().toUpperCase();
        String normalizedWorkshift = (workshift == null || workshift.isBlank()) ? "GENERAL" : workshift.trim().toUpperCase();

        boolean allTypes     = "GENERAL".equals(normalizedType);
        boolean allWorkshift = "GENERAL".equals(normalizedWorkshift);

        ReportResponseV2DTO reportResponse = new ReportResponseV2DTO();
        Integer qty;
        List<TransactionBatchDTO> transactions;

        if (allTypes && allWorkshift) { // caso 1: sin filtros
            qty = braceletServiceV2.getGeneralDayCount(fromDateFull, toDateFull);
            transactions = braceletServiceV2.getGeneralDayReport(fromDateFull, toDateFull);
        } else if (!allTypes && allWorkshift) { // caso 2: filtrar por tipo
            qty = braceletServiceV2.getCustomTypeCount(fromDateFull, toDateFull, normalizedType);
            transactions = braceletServiceV2.getCustomTypeReport(fromDateFull, toDateFull, normalizedType);
        } else if (allTypes && !allWorkshift) { // caso 3: filtrar por turno
            qty = braceletServiceV2.getCustomWorkshiftCount(fromDateFull, toDateFull, normalizedWorkshift);
            transactions = braceletServiceV2.getCustomWorkshiftReport(fromDateFull, toDateFull, normalizedWorkshift);
        } else { // caso 4: filtrar por tipo Y turno
            qty = braceletServiceV2.getCustomCount(fromDateFull, toDateFull, normalizedType, normalizedWorkshift);
            transactions = braceletServiceV2.getCustomReport(fromDateFull, toDateFull, normalizedType, normalizedWorkshift);
        }

        reportResponse.setQty(qty);
        reportResponse.setData(transactions);
        return ResponseEntity.ok().body(reportResponse);
    }

    @GetMapping("/cashClose")
    public ResponseEntity<?> getCashClose(
            @RequestHeader(value = "date", required = false) String dateHeader,
            @RequestParam(value = "date", required = false) String dateParam,
            @RequestHeader(value = "type", required = false) String typeHeader,
            @RequestParam(value = "type", required = false) String typeParam) {

        String resolvedDate = dateHeader != null ? dateHeader : dateParam;
        if (resolvedDate == null || resolvedDate.isBlank()) {
            resolvedDate = java.time.LocalDate.now().toString();
        }

        String resolvedType = typeHeader != null ? typeHeader : typeParam;
        if (resolvedType == null || resolvedType.isBlank()) {
            return ResponseEntity.badRequest().body("Bracelet type is required for cash close");
        }

        return ResponseEntity.ok(braceletServiceV2.getCashClose(resolvedDate, resolvedType.trim().toUpperCase()));
    }

    @GetMapping("owners/{propertyId}/history")
    public ResponseEntity<?> getOwnerHistory(@PathVariable("propertyId") String propertyId) {
        if (propertyId == null || propertyId.isBlank()) {
            return ResponseEntity.badRequest().body("Property ID is required");
        }
        return ResponseEntity.ok(
            java.util.Map.of(
                "status", "success",
                "data", braceletServiceV2.getOwnerHistory(propertyId)
            )
        );
    }

    private String formatStartOfDay(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return "";
        dateStr = dateStr.trim();
        return (dateStr.length() >= 10 ? dateStr.substring(0, 10) : dateStr) + " 00:00";
    }

    private String formatEndOfDay(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return "";
        dateStr = dateStr.trim();
        return (dateStr.length() >= 10 ? dateStr.substring(0, 10) : dateStr) + " 23:59";
    }


}
