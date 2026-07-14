package com.helios.platform.pulse.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // Caché de Caffeine con expiración automática de 10 minutos sin actividad para evitar fugas de memoria
    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(10000) // Máximo 10,000 IPs concurrentes
            .build();

    private Bucket createNewBucket() {
        // Límite de 30 peticiones. Se recarga por completo a las 30 peticiones cada 1 minuto (60 segundos)
        Bandwidth limit = Bandwidth.builder().capacity(30).refillIntervally(30, Duration.ofMinutes(1)).build();
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket resolveBucket(String ip) {
        return cache.get(ip, k -> createNewBucket());
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // Aplicar a v2 y auth para prevenir ataques de fuerza bruta y DoS
        if (uri.startsWith("/helios/pulse/v2") || uri.startsWith("/helios/pulse/auth") || uri.startsWith("/helios/pulse/users")) {
            String ip = getClientIp(request);
            Bucket bucket = resolveBucket(ip);

            if (bucket.tryConsume(1)) {
                // Hay capacidad disponible, la petición puede continuar
                filterChain.doFilter(request, response);
            } else {
                // El límite de peticiones fue excedido
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // Error 429
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"TOO MANY REQUESTS\", \"message\": \"Rate limit exceeded. Try again in 1 minute.\"}");
                return;
            }
        } else {
            // Dejar pasar otro tráfico (swagger, error, health) sin limitar
            filterChain.doFilter(request, response);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        // Usamos el header de Cloudflare si está presente, es seguro si el tráfico está forzado a pasar por Cloudflare
        String cfHeader = request.getHeader("CF-Connecting-IP");
        if (cfHeader != null && !cfHeader.isEmpty()) {
            return cfHeader.trim();
        }
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
