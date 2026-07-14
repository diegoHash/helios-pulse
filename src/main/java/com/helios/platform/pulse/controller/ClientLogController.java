package com.helios.platform.pulse.controller;

import jakarta.servlet.http.HttpServletRequest;
import com.helios.platform.pulse.entities.ClientLog;
import com.helios.platform.pulse.repositories.ClientLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/helios/pulse/logs")
public class ClientLogController {

    @Autowired
    private ClientLogRepository clientLogRepository;

    @PostMapping("/push")
    public ResponseEntity<?> pushLog(@RequestBody ClientLog logEntry, HttpServletRequest request) {
        try {
            // Extract IP if not provided
            if (logEntry.getIpAddress() == null || logEntry.getIpAddress().isEmpty()) {
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                logEntry.setIpAddress(ip);
            }

            if (logEntry.getTimestamp() == null) {
                logEntry.setTimestamp(LocalDateTime.now());
            }

            clientLogRepository.save(logEntry);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Log stored"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/devices")
    public ResponseEntity<?> getUniqueDevices() {
        List<Object[]> rawDevices = clientLogRepository.findDistinctDevices();
        List<Map<String, String>> devices = rawDevices.stream().map(obj -> {
            Map<String, String> map = new HashMap<>();
            map.put("deviceAlias", (String) obj[0]);
            map.put("pcName", (String) obj[1]);
            map.put("ipAddress", (String) obj[2]);
            map.put("deviceId", (String) obj[3]);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(devices);
    }

    @GetMapping("/device")
    public ResponseEntity<List<ClientLog>> getLogsByDevice(@RequestParam("alias") String alias) {
        return ResponseEntity.ok(clientLogRepository.findByDeviceAliasOrderByTimestampDesc(alias));
    }

    @DeleteMapping("/device")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> deleteLogsByDevice(@RequestParam("alias") String alias) {
        try {
            clientLogRepository.deleteByDeviceAlias(alias);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Logs deleted for device: " + alias));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
