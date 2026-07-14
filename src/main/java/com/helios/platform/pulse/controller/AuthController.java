package com.helios.platform.pulse.controller;

import lombok.RequiredArgsConstructor;
import com.helios.platform.pulse.dto.AuthRequestDTO;
import com.helios.platform.pulse.dto.AuthResponseDTO;
import com.helios.platform.pulse.dto.RegisterRequestDTO;
import com.helios.platform.pulse.security.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/helios/pulse/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        AuthResponseDTO response = service.register(request);
        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("jwt", response.getToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .domain(".caribbean-one.site") // Cross-domain cookie
                .maxAge(24 * 60 * 60)
                .sameSite("None") // Requires secure(true)
                .build();
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> authenticate(@RequestBody AuthRequestDTO request) {
        AuthResponseDTO response = service.authenticate(request);
        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("jwt", response.getToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .domain(".caribbean-one.site") // Cross-domain cookie
                .maxAge(24 * 60 * 60)
                .sameSite("None") // Requires secure(true)
                .build();
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/request-otp")
    public ResponseEntity<String> requestOtp(@RequestBody com.helios.platform.pulse.dto.OtpRequestDTO request) {
        service.requestOtp(request);
        return ResponseEntity.ok("OTP sent to your email");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody com.helios.platform.pulse.dto.ResetPasswordDTO request) {
        service.resetPasswordWithOtp(request);
        return ResponseEntity.ok("Password recovered successfully");
    }
}
