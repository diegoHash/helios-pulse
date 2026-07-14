package com.helios.platform.pulse.controller;

import lombok.RequiredArgsConstructor;
import com.helios.platform.pulse.dto.RootUpdateTransactionRequest;
import com.helios.platform.pulse.service.CorrectionRequestService;
import com.helios.platform.pulse.service.RootService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/helios/pulse/root")
@RequiredArgsConstructor
public class RootSuperController {

    private final RootService rootService;
    private final CorrectionRequestService correctionRequestService;

    @GetMapping("/requests")
    public ResponseEntity<?> getRequests(@RequestParam(name = "reqStatus", required = false, defaultValue = "PENDING") String reqStatus) {
        if ("ALL".equalsIgnoreCase(reqStatus)) {
            return ResponseEntity.ok(correctionRequestService.getAllRequests());
        }
        return ResponseEntity.ok(correctionRequestService.getPendingRequests());
    }

    @PutMapping("/requests/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable("id") Long id) {
        correctionRequestService.updateStatus(id, "REJECTED");
        return ResponseEntity.ok().build();
    }

    @PutMapping("/transactions/{id}")
    public ResponseEntity<?> superUpdateTransaction(
            @PathVariable("id") Long id,
            @RequestBody RootUpdateTransactionRequest request,
            @RequestParam(name = "requestId", required = false) Long requestId) {
        try {
            rootService.superUpdateTransaction(id, request, requestId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> getTransaction(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(rootService.getTransaction(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
