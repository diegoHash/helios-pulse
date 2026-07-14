# HELIOS Pulse

API de HELIOS Platform para operaciones de punto de venta, pulseras, pagos,
transacciones, cierres, usuarios y analítica. Es el sucesor independiente del
servicio CBT/POS; el repositorio legacy no se modifica ni se despliega desde aquí.

## Requisitos

- Java 21
- MySQL 8
- Redis opcional (puede usarse caché local)
- Variables de entorno basadas en [`.env.example`](.env.example)

## Ejecución local

```powershell
$env:HELIOS_SWAGGER_ENABLED="true"
.\gradlew.bat bootRun
```

La API escucha en el puerto configurado por `server.port` (actualmente `9595`).
Para verificar el proyecto:

```powershell
.\gradlew.bat clean test
```

## Swagger / OpenAPI

Swagger está deshabilitado por defecto. Debe habilitarse únicamente en desarrollo
o detrás de un control administrativo:

- Swagger UI local: `http://localhost:9595/swagger-ui.html`
- OpenAPI JSON local: `http://localhost:9595/v3/api-docs`
- Swagger agregado en Gateway: `https://<gateway>/swagger-ui.html`
- OpenAPI mediante Gateway: `https://<gateway>/docs/pulse/v3/api-docs`

En Swagger, usa **Authorize** e introduce el JWT como `Bearer <token>`. Los
endpoints públicos de autenticación no requieren token; el resto conserva las
reglas definidas en `SecurityConfig`.

Los controladores publicados se agrupan automáticamente en el documento OpenAPI:
autenticación, usuarios, propietarios, operadores, lotes, pulseras, pagos,
transacciones, reportes, cierres, correcciones, analítica y salud.

## Contrato HTTP

- Prefijo público: `/helios/pulse/**`
- Identidad Spring: `helios-pulse`
- Paquete Java: `com.helios.platform.pulse`
- Autenticación: JWT Bearer
- Formato principal: JSON

## Configuración sensible

No almacenes secretos en Git. Las variables principales son:

- `HELIOS_PULSE_ENCRYPTION_KEY`
- `ETECC_DDBB_URL`, `ETECC_DDBB_USERNAME`, `ETECC_DDBB_PASS` (aliases pendientes de migración)
- `ETECC_JWT_SECRET` (alias pendiente de migración)
- credenciales opcionales de correo, Telegram, WhatsApp, Gemini y OpenWeather

Los nombres físicos de tablas se conservan temporalmente por compatibilidad con
la base de datos compartida. Consulta [`HELIOS_MIGRATION.md`](HELIOS_MIGRATION.md).

## Despliegue seguro

Los cambios deben pasar pruebas, escaneo de secretos y revisión antes de llegar a
`main`. Un push a ramas de trabajo no debe desplegar producción.
