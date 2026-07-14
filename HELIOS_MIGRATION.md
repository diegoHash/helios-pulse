# HELIOS Pulse migration boundary

This repository is the independent successor to the legacy CBT/POS service.
The legacy repository is not a deployment target and must not be modified from
this repository.

## Current identity

- Application: `helios-pulse`
- Java package: `com.helios.platform.pulse`
- Public route prefix: `/helios/pulse`
- Runtime secret prefix: `HELIOS_PULSE_`

## Compatibility boundary

Existing database table and column names are intentionally unchanged. Renaming
physical schema objects requires a separately reviewed migration with rollback
and compatibility testing against the shared production database.

The legacy frontend URL remains only as a configurable default until HELIOS DNS
and CORS origins are approved. It is not the permanent HELIOS public endpoint.

The tag `pre-helios-rebrand-2026-07-13` identifies the imported stable baseline.
