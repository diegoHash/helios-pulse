package com.helios.platform.pulse.security;

import lombok.RequiredArgsConstructor;
import com.helios.platform.pulse.dto.AuthRequestDTO;
import com.helios.platform.pulse.dto.AuthResponseDTO;
import com.helios.platform.pulse.dto.RegisterRequestDTO;
import com.helios.platform.pulse.entities.Role;
import com.helios.platform.pulse.entities.UserEntity;
import com.helios.platform.pulse.repositories.IUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class AuthService {

    private final IUser repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final com.helios.platform.pulse.service.EmailService emailService;
    private final com.helios.platform.pulse.service.TelegramService telegramService;

    @Value("${application.frontend.url:https://caribbean-one.site}")
    private String frontendUrl;

    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (repository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        Role assignedRole;
        if (repository.countByRole(Role.ROOT) == 0) {
            assignedRole = Role.ROOT;
        } else {
            // Verificar que el usuario actual es ROOT
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ROOT"))) {
                throw new RuntimeException("Forbidden: Sólo el usuario ROOT puede crear cuentas");
            }

            if (request.getRole() != null) {
                try {
                    assignedRole = Role.valueOf(request.getRole().toUpperCase());
                } catch (IllegalArgumentException e) {
                    assignedRole = Role.CAJERO;
                }
            } else {
                assignedRole = Role.CAJERO;
            }
        }

        var user = new UserEntity(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                assignedRole,
                request.getPermissions(),
                request.getEmail(),
                request.getOperatorName(),
                request.getOriginApp() != null ? request.getOriginApp() : "POS"
        );
        repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthResponseDTO.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .role(user.getRole().name())
                .user(new com.helios.platform.pulse.dto.UserResponseDTO(user.getId(), user.getUsername(), user.getRole().name(), user.getPermissions(), user.getOperatorName(), user.getAvatar(), user.getTelegramChatId() != null && !user.getTelegramChatId().isEmpty(), user.getOriginApp()))
                .build();
    }

    public AuthResponseDTO authenticate(AuthRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = repository.findByUsername(request.getUsername())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthResponseDTO.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .role(user.getRole().name())
                .user(new com.helios.platform.pulse.dto.UserResponseDTO(user.getId(), user.getUsername(), user.getRole().name(), user.getPermissions(), user.getOperatorName(), user.getAvatar(), user.getTelegramChatId() != null && !user.getTelegramChatId().isEmpty(), user.getOriginApp()))
                .build();
    }

    // UUID Token -> RecoveryData (username, expirationTime)
    private static class RecoveryData {
        String username;
        long expirationTime;
        RecoveryData(String username, long expirationTime) {
            this.username = username;
            this.expirationTime = expirationTime;
        }
    }
    private static final java.util.Map<String, RecoveryData> recoveryCache = new java.util.concurrent.ConcurrentHashMap<>();

    public void requestOtp(com.helios.platform.pulse.dto.OtpRequestDTO request) {
        var user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = java.util.UUID.randomUUID().toString();
        // 10 minutes expiry
        long expiryTime = System.currentTimeMillis() + (10 * 60 * 1000);
        recoveryCache.put(token, new RecoveryData(user.getUsername(), expiryTime));

        String method = request.getMethod() != null ? request.getMethod().toUpperCase() : "EMAIL";

        if (method.equals("TELEGRAM")) {
            if (user.getTelegramChatId() == null || user.getTelegramChatId().isEmpty()) {
                throw new RuntimeException("Este usuario no tiene Telegram configurado");
            }
            try {
                String link = frontendUrl + "/?recoveryToken=" + token;
                String msg = "👋 ¡Hola " + user.getUsername() + "!\n\n" +
                             "Hemos recibido una solicitud para recuperar la contraseña de tu cuenta en Caribbean One Ecosystem V2.\n\n" +
                             "Para crear tu nueva contraseña, por favor presiona el botón de abajo:\n\n" +
                             "⏳ Recuerda que por tu seguridad, este enlace expirará en 10 minutos.\n\n" +
                             "Si no fuiste tú quien solicitó esto, por favor ignora este mensaje.";
                telegramService.sendMessage(user.getTelegramChatId(), msg, "🔑 Restablecer Contraseña", link);
                System.out.println("Recovery link sent via Telegram to user: " + user.getUsername());
            } catch (Exception e) {
                System.err.println("Error enviando Telegram: " + e.getMessage());
                throw new RuntimeException("Error al enviar mensaje por Telegram");
            }
        } else {
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                throw new RuntimeException("Este usuario no tiene un correo configurado");
            }
            try {
                emailService.sendOtpEmail(user.getEmail(), token);
                System.out.println("SMTP Email sent successfully to: " + user.getEmail());
            } catch (Exception e) {
                System.err.println("Error al enviar correo SMTP: " + e.getMessage());
                throw new RuntimeException("Error al enviar correo electrónico");
            }
        }
    }

    public void resetPasswordWithOtp(com.helios.platform.pulse.dto.ResetPasswordDTO request) {
        RecoveryData data = recoveryCache.get(request.getToken());
        if (data == null) {
            throw new RuntimeException("Enlace de recuperación inválido o expirado");
        }

        if (System.currentTimeMillis() > data.expirationTime) {
            recoveryCache.remove(request.getToken());
            throw new RuntimeException("El enlace de recuperación ha expirado (validez de 10 minutos)");
        }

        var user = repository.findByUsername(data.username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
        recoveryCache.remove(request.getToken()); // Clear token after use (uso único)
    }
}
