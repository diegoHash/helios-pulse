package com.helios.platform.pulse.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${application.frontend.url:https://caribbean-one.site}")
    private String frontendUrl;

    public void sendOtpEmail(String toEmail, String otpCode) {
        CompletableFuture.runAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setTo(toEmail);
                helper.setSubject("Recuperación de Contraseña - Caribbean One");

                String link = frontendUrl + "/?recoveryToken=" + otpCode;

                String htmlContent = "<div style=\"font-family: 'Inter', Helvetica, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f8fafc; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);\">" +
                    "<div style=\"background-color: #0f172a; padding: 30px; text-align: center;\">" +
                    "<h1 style=\"color: #ffffff; margin: 0; font-size: 24px; font-weight: 700; letter-spacing: 1px;\">CARIBBEAN ONE</h1>" +
                    "<p style=\"color: #94a3b8; margin-top: 5px; font-size: 14px;\">Sistema de Gestión Centralizada</p>" +
                    "</div>" +
                    "<div style=\"padding: 40px 30px; background-color: #ffffff;\">" +
                    "<h2 style=\"color: #1e293b; margin-top: 0; font-size: 20px;\">Restablecimiento de Contraseña</h2>" +
                    "<p style=\"color: #475569; line-height: 1.6; font-size: 15px;\">Has solicitado restablecer tu contraseña para acceder al ecosistema de Caribbean Cloud. Para continuar con el proceso, por favor haz clic en el botón de abajo:</p>" +
                    "<div style=\"text-align: center; margin: 35px 0;\">" +
                    "<a href=\"" + link + "\" style=\"background-color: #10b981; color: #ffffff; padding: 14px 28px; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 16px; display: inline-block; box-shadow: 0 4px 10px rgba(16, 185, 129, 0.3);\">Establecer Nueva Contraseña</a>" +
                    "</div>" +
                    "<p style=\"color: #64748b; font-size: 13px; line-height: 1.5; margin-bottom: 0;\">Este enlace es de un solo uso y expirará automáticamente en <strong>10 minutos</strong> por razones de seguridad.</p>" +
                    "</div>" +
                    "<div style=\"background-color: #f1f5f9; padding: 20px; text-align: center; border-top: 1px solid #e2e8f0;\">" +
                    "<p style=\"color: #94a3b8; font-size: 12px; margin: 0;\">Si no solicitaste este cambio, puedes ignorar este mensaje de forma segura.</p>" +
                    "<p style=\"color: #94a3b8; font-size: 12px; margin-top: 5px;\">&copy; " + java.time.Year.now().getValue() + " Caribbean Cloud. Todos los derechos reservados.</p>" +
                    "</div>" +
                    "</div>";

                helper.setText(htmlContent, true);
                mailSender.send(message);
                System.out.println("SMTP Email sent asynchronously to: " + toEmail);
            } catch (Exception e) {
                System.err.println("Error al enviar correo SMTP asíncrono: " + e.getMessage());
            }
        });
    }

    public void sendSettingsOtpEmail(String toEmail, String otpCode) {
        CompletableFuture.runAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setTo(toEmail);
                helper.setSubject("Código de Verificación - Configuración de Cuenta");

                String htmlContent = "<div style=\"font-family: 'Inter', Helvetica, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f8fafc; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);\">" +
                    "<div style=\"background-color: #0f172a; padding: 30px; text-align: center;\">" +
                    "<h1 style=\"color: #ffffff; margin: 0; font-size: 24px; font-weight: 700; letter-spacing: 1px;\">CARIBBEAN ONE</h1>" +
                    "</div>" +
                    "<div style=\"padding: 40px 30px; background-color: #ffffff; text-align: center;\">" +
                    "<h2 style=\"color: #1e293b; margin-top: 0; font-size: 20px;\">Código de Verificación</h2>" +
                    "<p style=\"color: #475569; line-height: 1.6; font-size: 15px;\">Usa el siguiente código de 6 dígitos para confirmar los cambios en tu configuración de cuenta:</p>" +
                    "<div style=\"margin: 30px auto; background-color: #f1f5f9; padding: 15px; border-radius: 8px; font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #10b981; max-width: 250px;\">" +
                    otpCode +
                    "</div>" +
                    "<p style=\"color: #64748b; font-size: 13px; line-height: 1.5; margin-bottom: 0;\">Este código expirará automáticamente en <strong>10 minutos</strong> por razones de seguridad.</p>" +
                    "</div>" +
                    "</div>";

                helper.setText(htmlContent, true);
                mailSender.send(message);
                System.out.println("SMTP OTP Settings Email sent asynchronously to: " + toEmail);
            } catch (Exception e) {
                System.err.println("Error al enviar correo SMTP asíncrono para OTP: " + e.getMessage());
            }
        });
    }
    public void sendCorrectionRequestEmail(String toEmail, String requesterName, Long transactionId, String fieldToModify, String messageText, String deviceInfo) {
        CompletableFuture.runAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setTo(toEmail);
                helper.setSubject("Nueva Solicitud de Corrección - Transacción #" + transactionId);

                String htmlContent = "<div style=\"font-family: 'Inter', Helvetica, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f8fafc; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);\">" +
                    "<div style=\"background-color: #0f172a; padding: 30px; text-align: center;\">" +
                    "<h1 style=\"color: #ffffff; margin: 0; font-size: 24px; font-weight: 700; letter-spacing: 1px;\">CARIBBEAN ONE</h1>" +
                    "<p style=\"color: #94a3b8; margin-top: 5px; font-size: 14px;\">Sistema de Gestión Centralizada</p>" +
                    "</div>" +
                    "<div style=\"padding: 40px 30px; background-color: #ffffff;\">" +
                    "<h2 style=\"color: #1e293b; margin-top: 0; font-size: 20px;\">Nueva Solicitud de Cambio</h2>" +
                    "<p style=\"color: #475569; line-height: 1.6; font-size: 15px;\">El usuario <strong>" + requesterName + "</strong> ha solicitado una corrección en el sistema.</p>" +
                    "<div style=\"margin: 20px 0; background-color: #f1f5f9; padding: 15px; border-radius: 8px; font-size: 14px; color: #1e293b;\">" +
                    "<p style=\"margin: 5px 0;\"><strong>Transacción ID:</strong> #" + transactionId + "</p>" +
                    "<p style=\"margin: 5px 0;\"><strong>Campo a modificar:</strong> " + fieldToModify + "</p>" +
                    "<p style=\"margin: 5px 0;\"><strong>Mensaje del Cajero:</strong> <em>\"" + messageText + "\"</em></p>" +
                    "<p style=\"margin: 5px 0; color: #64748b; font-size: 12px;\"><strong>Dispositivo:</strong> " + deviceInfo + "</p>" +
                    "</div>" +
                    "<div style=\"text-align: center; margin: 30px 0;\">" +
                    "<a href=\"" + frontendUrl + "/root/requests\" style=\"background-color: #10b981; color: #ffffff; padding: 12px 24px; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 15px; display: inline-block; box-shadow: 0 4px 10px rgba(16, 185, 129, 0.3);\">Revisar en el Panel Root</a>" +
                    "</div>" +
                    "</div>" +
                    "<div style=\"background-color: #f1f5f9; padding: 20px; text-align: center; border-top: 1px solid #e2e8f0;\">" +
                    "<p style=\"color: #94a3b8; font-size: 12px; margin-top: 5px;\">&copy; " + java.time.Year.now().getValue() + " Caribbean Cloud. Todos los derechos reservados.</p>" +
                    "</div>" +
                    "</div>";

                helper.setText(htmlContent, true);
                mailSender.send(message);
                System.out.println("SMTP Correction Request Email sent asynchronously to: " + toEmail);
            } catch (Exception e) {
                System.err.println("Error al enviar correo SMTP asíncrono para Correction Request: " + e.getMessage());
            }
        });
    }
}
