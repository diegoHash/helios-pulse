package com.helios.platform.pulse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettingsUpdateRequestDTO {
    private String newEmail;
    private String newPassword;
    private String newUsername;
    private String avatar;
    private String otpCode;
    private boolean removeTelegram;
}
