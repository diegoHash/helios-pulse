package com.helios.platform.pulse.controller;

import com.helios.platform.pulse.entities.UserEntity;
import com.helios.platform.pulse.service.NotificationService;
import com.helios.platform.pulse.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("etecc/cbt/users")
public class UserController {

    private final UserService user_service;
    private final NotificationService notificationService;
    private final com.helios.platform.pulse.security.AuthService authService;

    // Use AtomicInteger to avoid basic race conditions, although token-based auth is recommended for production.
    private final AtomicInteger otc = new AtomicInteger(0);

    public UserController(UserService userService, NotificationService notificationService, com.helios.platform.pulse.security.AuthService authService) {
        this.user_service = userService;
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @GetMapping("/getall")
    public List<UserEntity> getAll(){
        return user_service.getAll();
    }

    /**
     *
     * @param code Codigo OTC enviado por el usuario para ser validado
     * @return Boolean Respuesta de codigo validado
     */
    @GetMapping("/auth/{code}")
    public boolean auth(@PathVariable(name = "code") int code){
        boolean isAuth = (code == otc.get());
        if(isAuth){
            notificationService.sendNotification("Authentication Successful!");
        }else{
            notificationService.sendNotification("Authentication Failed!");
        }
        otc.set(Integer.MIN_VALUE);
        return isAuth;
    }

    /**
     * Solicitar un codigo de un solo uso para modificacion de usuarios
     *
     *  */
    @GetMapping("/otc")
    public void authOtc(){
        int newOtc = (int) (Math.random() * 1000000);  // Generate a random 6-digit number.
        otc.set(newOtc);
        notificationService.sendNotification("YOUR AUTHENTICATION CODE IS: " + newOtc);
    }

    @PostMapping("/create")
    public ResponseEntity<?> newUser(@RequestBody com.helios.platform.pulse.dto.RegisterRequestDTO request){
        if(request == null || request.getUsername() == null || request.getUsername().isEmpty()){
            return ResponseEntity.badRequest().body("Invalid request");
        }
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update")
    public UserEntity updateUser(@RequestBody UserEntity username){
        if(username == null){
            return null;
        }

        if(username.getUsername() == null || username.getUsername().isEmpty()){
            return null;
        }
        return user_service.updateUser(username);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable(name = "id") Long id){
        if(id == null){
            return ResponseEntity.badRequest().body("ID IS NULL");
        }

        if(id <= 0){
            return ResponseEntity.badRequest().body("ID IS LESS THAN ZERO");
        }
        return user_service.deleteUser(id);
    }

    @PostMapping("/auth")
    public ResponseEntity<com.helios.platform.pulse.dto.AuthResponseDTO> authenticateUser(@RequestBody com.helios.platform.pulse.dto.AuthRequestDTO request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @GetMapping("/settings/me")
    public ResponseEntity<?> getCurrentProfile(java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("No autorizado");
        try {
            return ResponseEntity.ok(user_service.getProfile(principal.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/settings/request-otp")
    public ResponseEntity<?> requestSettingsOtp(java.security.Principal principal, @RequestParam(name = "method", required = false, defaultValue = "EMAIL") String method) {
        if (principal == null) return ResponseEntity.status(401).body("No autorizado");
        try {
            user_service.requestSettingsOtp(principal.getName(), method);
            return ResponseEntity.ok("OTP enviado por " + method);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/settings/verify-otp")
    public ResponseEntity<?> verifySettingsOtp(java.security.Principal principal, @RequestBody java.util.Map<String, String> request) {
        if (principal == null) return ResponseEntity.status(401).body("No autorizado");
        try {
            user_service.verifyCurrentOtp(principal.getName(), request.get("otpCode"));
            return ResponseEntity.ok("OTP validado correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/settings/request-new-email-otp")
    public ResponseEntity<?> requestNewEmailOtp(java.security.Principal principal, @RequestBody java.util.Map<String, String> request) {
        if (principal == null) return ResponseEntity.status(401).body("No autorizado");
        try {
            user_service.requestNewEmailOtp(principal.getName(), request.get("newEmail"));
            return ResponseEntity.ok("OTP enviado al nuevo correo");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/settings/telegram/link")
    public ResponseEntity<?> getTelegramLink(java.security.Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("No autorizado");
        try {
            return ResponseEntity.ok(java.util.Map.of("link", user_service.getTelegramMagicLink(principal.getName())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/settings/update")
    public ResponseEntity<?> updateSettings(java.security.Principal principal, @RequestBody com.helios.platform.pulse.dto.SettingsUpdateRequestDTO request) {
        if (principal == null) return ResponseEntity.status(401).body("No autorizado");
        try {
            return ResponseEntity.ok(user_service.updateSettings(principal.getName(), request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
