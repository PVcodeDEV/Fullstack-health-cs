# Delta for modulo-personas-api

## MODIFIED Requirements

### Requirement: R-005 — API configuration in tb_configuracion_api

API provider configuration (base URL, token, enabled flag) SHALL be stored in `tb_configuracion_api` within the `seguridad` module. The `persona` module SHALL read them at runtime via `@ConfigurationProperties` backed by `DbApiConfigService`. Values SHALL be updatable without restart.
(Previously: API config stored as application properties in the `seguridad` module)

#### Scenario: SC-005-1 — Config read from database
- GIVEN a `tb_configuracion_api` row with `modulo=reniec`, `clave=base_url`, `valor=https://api.reniec.gob.pe/v1`
- WHEN `ReniecClient.consultaDni(dni)` is called
- THEN `base_url` is read from `tb_configuracion_api` and used for the HTTP call

#### Scenario: SC-005-2 — Runtime update without restart
- GIVEN `base_url` is `https://old.url` in the database
- WHEN the row is updated to `https://new.url`
- THEN the next API call uses `https://new.url` without application restart
