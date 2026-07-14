package com.helios.platform.pulse.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import com.helios.platform.pulse.dto.CorrectionRequestDto;
import com.helios.platform.pulse.entities.UserEntity;
import com.helios.platform.pulse.repositories.IUser;
import com.helios.platform.pulse.service.CorrectionRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/helios/pulse/requests")
@RequiredArgsConstructor
public class CorrectionRequestController {

    private final CorrectionRequestService service;
    private final IUser userRepository;

    @PostMapping
    public ResponseEntity<?> createRequest(@RequestBody CorrectionRequestDto dto, org.springframework.security.core.Authentication authentication, HttpServletRequest request) {
        try {
            String username = authentication.getName();
            UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));


            String ipAddress = request.getRemoteAddr();
            String deviceInfo = request.getHeader("User-Agent");

            service.createRequest(dto, user, deviceInfo, ipAddress);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear la solicitud: " + e.getMessage());
        }
    }
}
