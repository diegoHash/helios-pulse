package com.helios.platform.pulse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private String role;
    private String permissions;
    private String operatorName;
    private String avatar;
    private boolean hasTelegram;
    private String originApp;


    public UserResponseDTO(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.permissions = null;
        this.operatorName = null;
    }
}
