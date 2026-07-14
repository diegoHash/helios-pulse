package com.helios.platform.pulse.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider)
            throws Exception {
        http
                .cors(org.springframework.security.config.Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Permite TODOS los preflight OPTIONS (necesario para CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/helios/pulse/auth/**",
                                "/helios/pulse/users/auth",
                                "/helios/pulse/health",
                                "/helios/pulse/logs/push",
                                "/helios/pulse/analytics/**",
                                "/error",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html")
                        .permitAll()
                        .requestMatchers("/helios/pulse/users/getall", "/helios/pulse/owners", "/helios/pulse/currentserial", "/helios/pulse/remaining").hasAnyRole("ROOT", "CAJERO", "OBSERVADOR")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/helios/pulse/v2/save").hasAnyRole("ROOT", "CAJERO")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/helios/pulse/v2/**").hasAnyRole("ROOT", "CAJERO", "OBSERVADOR")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/helios/pulse/v2/**").hasAnyRole("ROOT", "CAJERO")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/helios/pulse/logs/**").hasAnyRole("ROOT", "CAJERO", "OBSERVADOR")
                        .requestMatchers("/helios/pulse/logs/**").hasRole("ROOT")
                        .requestMatchers("/helios/pulse/requests", "/helios/pulse/requests/**").authenticated()
                        .requestMatchers("/helios/pulse/root/**").hasRole("ROOT")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/helios/pulse/lotes/**").hasAnyRole("ROOT", "CAJERO", "OBSERVADOR")
                        .requestMatchers("/helios/pulse/lotes/**").hasAnyRole("ROOT", "CAJERO")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/helios/pulse/operators/**").hasAnyRole("ROOT", "CAJERO", "OBSERVADOR")
                        .requestMatchers("/helios/pulse/operators/**").hasAnyRole("ROOT", "CAJERO")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/helios/pulse/exchange-rates/**").hasAnyRole("ROOT", "CAJERO", "OBSERVADOR")
                        .requestMatchers("/helios/pulse/exchange-rates/**").hasAnyRole("ROOT", "CAJERO")
                        .anyRequest().authenticated())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(java.util.List.of("http://localhost:*", "https://*.caribbean-one.site", "https://caribbean-one.site"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
