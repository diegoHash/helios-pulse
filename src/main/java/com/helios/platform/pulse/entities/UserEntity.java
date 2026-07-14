package com.helios.platform.pulse.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "caribbean_one_users")
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = com.helios.platform.pulse.security.AesEncryptorConverter.class)
    @Column(unique = true, nullable = false)
    private String username;

    @Convert(converter = com.helios.platform.pulse.security.AesEncryptorConverter.class)
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "operator_name", nullable = false)
    private String operatorName;

    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    private String avatar;

    @Convert(converter = com.helios.platform.pulse.security.AesEncryptorConverter.class)
    @Column(name = "telegram_chat_id", nullable = true)
    private String telegramChatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(columnDefinition = "TEXT")
    private String permissions;

    @Column(name = "origin_app", nullable = false)
    private String originApp = "POS"; // Default to POS to maintain retrocompatibility

    @Column(nullable = false)
    private String status = "active";

    public UserEntity(String username, String password, Role role, String email, String operatorName) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.operatorName = operatorName;
        this.originApp = "POS";
        this.status = "active";
    }

    public UserEntity(String username, String password, Role role, String permissions, String email, String operatorName, String originApp) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.permissions = permissions;
        this.email = email;
        this.operatorName = operatorName;
        this.originApp = originApp;
        this.status = "active";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
