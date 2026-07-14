package com.helios.platform.pulse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequestDTO {
    private String username;

    @com.fasterxml.jackson.annotation.JsonAlias("pin")
    private String password;
}
