-- Seed document types needed by DataInitializer (idempotent)
INSERT INTO tb_tipos_documento_identidad (tdi_codigo_sunat, tdi_nombre, tdi_longitud_minima, tdi_longitud_maxima, tdi_activo, tdi_created_at)
SELECT '01', 'DNI',                      8,  8,  true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tb_tipos_documento_identidad WHERE tdi_codigo_sunat = '01');
INSERT INTO tb_tipos_documento_identidad (tdi_codigo_sunat, tdi_nombre, tdi_longitud_minima, tdi_longitud_maxima, tdi_activo, tdi_created_at)
SELECT '04', 'Carnet de Extranjería',    12, 12, true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tb_tipos_documento_identidad WHERE tdi_codigo_sunat = '04');
INSERT INTO tb_tipos_documento_identidad (tdi_codigo_sunat, tdi_nombre, tdi_longitud_minima, tdi_longitud_maxima, tdi_activo, tdi_created_at)
SELECT '06', 'RUC',                      11, 11, true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tb_tipos_documento_identidad WHERE tdi_codigo_sunat = '06');
INSERT INTO tb_tipos_documento_identidad (tdi_codigo_sunat, tdi_nombre, tdi_longitud_minima, tdi_longitud_maxima, tdi_activo, tdi_created_at)
SELECT '07', 'Pasaporte',                1,  20, true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tb_tipos_documento_identidad WHERE tdi_codigo_sunat = '07');
INSERT INTO tb_tipos_documento_identidad (tdi_codigo_sunat, tdi_nombre, tdi_longitud_minima, tdi_longitud_maxima, tdi_activo, tdi_created_at)
SELECT '11', 'Carné de Extranjería',     1,  20, true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tb_tipos_documento_identidad WHERE tdi_codigo_sunat = '11');

-- Seed tipos de contrato (idempotent)
INSERT INTO tb_tipos_contrato (tco_codigo, tco_nombre, tco_descripcion, tco_activo, tco_created_at)
SELECT 'INDETERMINADO', 'Contrato Indeterminado', 'Contrato a plazo no determinado - REMYPE', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tb_tipos_contrato WHERE tco_codigo = 'INDETERMINADO');
INSERT INTO tb_tipos_contrato (tco_codigo, tco_nombre, tco_descripcion, tco_activo, tco_created_at)
SELECT 'DETERMINADO', 'Contrato a Plazo Determinado', 'Contrato por tiempo específico', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tb_tipos_contrato WHERE tco_codigo = 'DETERMINADO');
INSERT INTO tb_tipos_contrato (tco_codigo, tco_nombre, tco_descripcion, tco_activo, tco_created_at)
SELECT 'CAS', 'Contrato Administrativo de Servicios', 'Régimen CAS', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tb_tipos_contrato WHERE tco_codigo = 'CAS');
INSERT INTO tb_tipos_contrato (tco_codigo, tco_nombre, tco_descripcion, tco_activo, tco_created_at)
SELECT 'LOCACION', 'Locación de Servicios', 'Locación de servicios - Recibo por honorarios', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tb_tipos_contrato WHERE tco_codigo = 'LOCACION');
INSERT INTO tb_tipos_contrato (tco_codigo, tco_nombre, tco_descripcion, tco_activo, tco_created_at)
SELECT 'TIEMPO_PARCIAL', 'Tiempo Parcial', 'Jornada menor a 4 horas diarias', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tb_tipos_contrato WHERE tco_codigo = 'TIEMPO_PARCIAL');
INSERT INTO tb_tipos_contrato (tco_codigo, tco_nombre, tco_descripcion, tco_activo, tco_created_at)
SELECT 'INTERMITENTE', 'Intermitente', 'Servicios intermitentes', true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tb_tipos_contrato WHERE tco_codigo = 'INTERMITENTE');

-- Seed tipos de colegiatura (idempotent)
INSERT INTO tb_tipos_colegiatura (tcl_codigo, tcl_nombre, tcl_descripcion, tcl_activo, tcl_created_at)
SELECT 'CMP', 'Colegio Médico del Perú', null, true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tb_tipos_colegiatura WHERE tcl_codigo = 'CMP');
INSERT INTO tb_tipos_colegiatura (tcl_codigo, tcl_nombre, tcl_descripcion, tcl_activo, tcl_created_at)
SELECT 'CEP', 'Colegio de Enfermeros del Perú', null, true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tb_tipos_colegiatura WHERE tcl_codigo = 'CEP');
INSERT INTO tb_tipos_colegiatura (tcl_codigo, tcl_nombre, tcl_descripcion, tcl_activo, tcl_created_at)
SELECT 'CPN', 'Colegio de Nutricionistas del Perú', null, true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tb_tipos_colegiatura WHERE tcl_codigo = 'CPN');
INSERT INTO tb_tipos_colegiatura (tcl_codigo, tcl_nombre, tcl_descripcion, tcl_activo, tcl_created_at)
SELECT 'OTROS', 'Otros colegios profesionales', null, true, NOW()
WHERE NOT EXISTS (SELECT 1 FROM tb_tipos_colegiatura WHERE tcl_codigo = 'OTROS');
