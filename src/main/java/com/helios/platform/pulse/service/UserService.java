package com.helios.platform.pulse.service;

import com.helios.platform.pulse.entities.Role;
import com.helios.platform.pulse.entities.UserEntity;
import com.helios.platform.pulse.repositories.IUser;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final IUser userRepository;
    private final EmailService emailService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final TelegramService telegramService;

    // Cache para OTP de configuración (username -> OtpData)
    private static class OtpData {
        String code;
        long expirationTime;
        OtpData(String code, long expirationTime) {
            this.code = code;
            this.expirationTime = expirationTime;
        }
    }
    private static final java.util.Map<String, OtpData> settingsOtpCache = new java.util.concurrent.ConcurrentHashMap<>();

    public UserService(IUser userRepository, EmailService emailService, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder, TelegramService telegramService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.telegramService = telegramService;
    }

    public List<UserEntity> getAll() {
       return userRepository.findByRoleNot(Role.ROOT);
    }

    public com.helios.platform.pulse.dto.UserResponseDTO getProfile(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return new com.helios.platform.pulse.dto.UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getPermissions(),
                user.getOperatorName(),
                user.getAvatar(),
                user.getTelegramChatId() != null && !user.getTelegramChatId().isEmpty(),
                user.getOriginApp()
        );
    }


    public UserEntity newUser(UserEntity username) {
        return userRepository.save(username);
    }

    public UserEntity updateUser(UserEntity username) {
        Long id = username.getId();
        Optional<UserEntity> oldUser = userRepository.findById(id);
        if(oldUser.isPresent()){
            UserEntity oldEntity = oldUser.get();
            oldEntity.setUsername(username.getUsername());
            if (username.getRole() != null) {
                oldEntity.setRole(username.getRole());
            }
            if (username.getPermissions() != null) {
                oldEntity.setPermissions(username.getPermissions());
            }
            return userRepository.save(oldEntity);
        }
        return null;
    }

    public String getTelegramMagicLink(String username) {
        return telegramService.getMagicLink(username);
    }

    public void requestSettingsOtp(String username, String method) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String otpCode = String.format("%06d", new java.util.Random().nextInt(999999));
        long expiryTime = System.currentTimeMillis() + (10 * 60 * 1000); // 10 mins
        settingsOtpCache.put(username, new OtpData(otpCode, expiryTime));

        if ("TELEGRAM".equalsIgnoreCase(method)) {
            if (user.getTelegramChatId() == null || user.getTelegramChatId().isEmpty()) {
                throw new RuntimeException("No tienes una cuenta de Telegram vinculada.");
            }
            telegramService.sendMessage(user.getTelegramChatId(), "🔑 *Código de Verificación*\n\nTu código para actualizar configuración es: *" + otpCode + "*\n\n_Válido por 10 minutos._");
        } else {
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                throw new RuntimeException("El usuario no tiene un correo configurado.");
            }
            emailService.sendSettingsOtpEmail(user.getEmail(), otpCode);
        }
    }

    public void verifyCurrentOtp(String username, String otpCode) {
        OtpData data = settingsOtpCache.get(username);
        if (data == null || !data.code.equals(otpCode)) {
            throw new RuntimeException("Código OTP inválido o no solicitado.");
        }
        if (System.currentTimeMillis() > data.expirationTime) {
            settingsOtpCache.remove(username);
            throw new RuntimeException("El código OTP ha expirado.");
        }
        // OTP válido. Lo autorizamos por 10 minutos para cambiar el correo u otros datos sensibles.
        settingsOtpCache.remove(username);
        settingsOtpCache.put(username + "_AUTH", new OtpData("AUTHORIZED", System.currentTimeMillis() + (10 * 60 * 1000)));
    }

    public void requestNewEmailOtp(String username, String newEmail) {
        OtpData auth = settingsOtpCache.get(username + "_AUTH");
        if (auth == null || System.currentTimeMillis() > auth.expirationTime) {
            throw new RuntimeException("Sesión de seguridad expirada. Vuelve a validar tu correo actual.");
        }

        String otpCode = String.format("%06d", new java.util.Random().nextInt(999999));
        long expiryTime = System.currentTimeMillis() + (10 * 60 * 1000);
        settingsOtpCache.put(username + "_NEW_EMAIL", new OtpData(otpCode, expiryTime));

        emailService.sendSettingsOtpEmail(newEmail, otpCode);
    }

    public com.helios.platform.pulse.dto.UserResponseDTO updateSettings(String username, com.helios.platform.pulse.dto.SettingsUpdateRequestDTO request) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar si requiere OTP general (Cambio de contraseña o username)
        boolean needsGeneralOtp = (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) ||
                                  (request.getNewUsername() != null && !request.getNewUsername().isEmpty());

        if (needsGeneralOtp) {
            OtpData auth = settingsOtpCache.get(username + "_AUTH");
            if (auth == null || System.currentTimeMillis() > auth.expirationTime) {
                // Compatibility for standard OTP without 2-step if they didn't call verifyCurrentOtp
                OtpData data = settingsOtpCache.get(username);
                if (data == null || !data.code.equals(request.getOtpCode())) {
                    throw new RuntimeException("Código OTP inválido o no solicitado para actualización");
                }
                if (System.currentTimeMillis() > data.expirationTime) {
                    settingsOtpCache.remove(username);
                    throw new RuntimeException("El código OTP ha expirado");
                }
                settingsOtpCache.remove(username);
            } else {
                // Usamos la autorización y la limpiamos
                settingsOtpCache.remove(username + "_AUTH");
            }

            if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            }
            if (request.getNewUsername() != null && !request.getNewUsername().isEmpty()) {
                // Verificar si ya existe
                if (userRepository.findByUsername(request.getNewUsername()).isPresent()) {
                    throw new RuntimeException("El nombre de usuario ya está en uso.");
                }
                user.setUsername(request.getNewUsername());
            }
        }

        // Flujo especial para 2-step email
        if (request.getNewEmail() != null && !request.getNewEmail().isEmpty()) {
            OtpData newEmailData = settingsOtpCache.get(username + "_NEW_EMAIL");
            if (newEmailData == null || !newEmailData.code.equals(request.getOtpCode())) {
                throw new RuntimeException("Código OTP del nuevo correo inválido.");
            }
            if (System.currentTimeMillis() > newEmailData.expirationTime) {
                settingsOtpCache.remove(username + "_NEW_EMAIL");
                throw new RuntimeException("El código OTP del nuevo correo ha expirado.");
            }
            user.setEmail(request.getNewEmail());
            settingsOtpCache.remove(username + "_NEW_EMAIL");
        }

        if (request.isRemoveTelegram()) {
            user.setTelegramChatId(null);
        }

        // El avatar no requiere OTP
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            user.setAvatar(request.getAvatar());
        }

        userRepository.save(user);

        return new com.helios.platform.pulse.dto.UserResponseDTO(
            user.getId(), user.getUsername(), user.getRole().name(), user.getPermissions(), user.getOperatorName(), user.getAvatar(),
            user.getTelegramChatId() != null && !user.getTelegramChatId().isEmpty(), user.getOriginApp()
        );
    }

    public ResponseEntity<String> deleteUser(Long id){
        userRepository.deleteById(id);
        return ResponseEntity.ok().body("Ok");
    }
}
