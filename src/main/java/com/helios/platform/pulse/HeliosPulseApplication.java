package com.helios.platform.pulse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.cache.annotation.EnableCaching;

/**
 * Aplicación principal del sistema CBT (Control de Brazaletes)
 * Esta API proporciona servicios para la gestión de brazaletes, propietarios y reportes
 *
 * @version 1.1
 * @author ETECC Development Team
 */
@SpringBootApplication
@EnableAsync(proxyTargetClass = true)
@EnableCaching
@EnableScheduling
public class HeliosPulseApplication {
	public static void main(String[] args) {
		SpringApplication.run(HeliosPulseApplication.class, args);
	}
}
