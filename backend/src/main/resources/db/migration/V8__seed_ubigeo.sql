-- =============================================================================
-- V8: Ubigeo Seed Data — Peru
-- All 24 departments + Callao + all provinces + complete districts (RENIEC codes)
-- Batched INSERTs for performance
-- =============================================================================

-- ============================================================
-- DEPARTMENTS (25)
-- ============================================================
INSERT INTO tb_ubigeo_departamentos (ubdep_codigo, ubdep_nombre, ubdep_activo, ubdep_created_at) VALUES
('01', 'Amazonas',       true, NOW()),
('02', 'Áncash',         true, NOW()),
('03', 'Apurímac',       true, NOW()),
('04', 'Arequipa',       true, NOW()),
('05', 'Ayacucho',       true, NOW()),
('06', 'Cajamarca',      true, NOW()),
('07', 'Callao',         true, NOW()),
('08', 'Cusco',          true, NOW()),
('09', 'Huancavelica',   true, NOW()),
('10', 'Huánuco',        true, NOW()),
('11', 'Ica',            true, NOW()),
('12', 'Junín',          true, NOW()),
('13', 'La Libertad',    true, NOW()),
('14', 'Lambayeque',     true, NOW()),
('15', 'Lima',           true, NOW()),
('16', 'Loreto',         true, NOW()),
('17', 'Madre de Dios',  true, NOW()),
('18', 'Moquegua',       true, NOW()),
('19', 'Pasco',          true, NOW()),
('20', 'Piura',          true, NOW()),
('21', 'Puno',           true, NOW()),
('22', 'San Martín',     true, NOW()),
('23', 'Tacna',          true, NOW()),
('24', 'Tumbes',         true, NOW()),
('25', 'Ucayali',        true, NOW());

-- ============================================================
-- PROVINCES (196)
-- ============================================================

-- Amazonas (01) — 7 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('0101', 'Chachapoyas',    '01', true, NOW()),
('0102', 'Bagua',          '01', true, NOW()),
('0103', 'Bongará',        '01', true, NOW()),
('0104', 'Condorcanqui',   '01', true, NOW()),
('0105', 'Luya',           '01', true, NOW()),
('0106', 'Rodríguez de Mendoza', '01', true, NOW()),
('0107', 'Utcubamba',      '01', true, NOW());

-- Áncash (02) — 20 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('0201', 'Huaraz',                 '02', true, NOW()),
('0202', 'Aija',                   '02', true, NOW()),
('0203', 'Antonio Raymondi',       '02', true, NOW()),
('0204', 'Asunción',               '02', true, NOW()),
('0205', 'Bolognesi',              '02', true, NOW()),
('0206', 'Carhuaz',                '02', true, NOW()),
('0207', 'Carlos Fermín Fitzcarrald', '02', true, NOW()),
('0208', 'Casma',                  '02', true, NOW()),
('0209', 'Corongo',                '02', true, NOW()),
('0210', 'Huari',                  '02', true, NOW()),
('0211', 'Huarmey',               '02', true, NOW()),
('0212', 'Huaylas',                '02', true, NOW()),
('0213', 'Mariscal Luzuriaga',     '02', true, NOW()),
('0214', 'Ocros',                  '02', true, NOW()),
('0215', 'Pallasca',               '02', true, NOW()),
('0216', 'Pomabamba',              '02', true, NOW()),
('0217', 'Recuay',                 '02', true, NOW()),
('0218', 'Santa',                  '02', true, NOW()),
('0219', 'Sihuas',                 '02', true, NOW()),
('0220', 'Yungay',                 '02', true, NOW());

-- Apurímac (03) — 7 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('0301', 'Abancay',                 '03', true, NOW()),
('0302', 'Andahuaylas',             '03', true, NOW()),
('0303', 'Antabamba',               '03', true, NOW()),
('0304', 'Aymaraes',                '03', true, NOW()),
('0305', 'Cotabambas',              '03', true, NOW()),
('0306', 'Chincheros',              '03', true, NOW()),
('0307', 'Grau',                   '03', true, NOW());

-- Arequipa (04) — 8 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('0401', 'Arequipa',        '04', true, NOW()),
('0402', 'Camaná',          '04', true, NOW()),
('0403', 'Caravelí',        '04', true, NOW()),
('0404', 'Castilla',        '04', true, NOW()),
('0405', 'Caylloma',        '04', true, NOW()),
('0406', 'Condesuyos',      '04', true, NOW()),
('0407', 'Islay',           '04', true, NOW()),
('0408', 'La Unión',        '04', true, NOW());

-- Ayacucho (05) — 11 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('0501', 'Huamanga',            '05', true, NOW()),
('0502', 'Cangallo',            '05', true, NOW()),
('0503', 'Huanca Sancos',       '05', true, NOW()),
('0504', 'Huanta',              '05', true, NOW()),
('0505', 'La Mar',              '05', true, NOW()),
('0506', 'Lucanas',             '05', true, NOW()),
('0507', 'Parinacochas',        '05', true, NOW()),
('0508', 'Páucar del Sara Sara','05', true, NOW()),
('0509', 'Sucre',               '05', true, NOW()),
('0510', 'Víctor Fajardo',      '05', true, NOW()),
('0511', 'Vilcas Huamán',       '05', true, NOW());

-- Cajamarca (06) — 13 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('0601', 'Cajamarca',       '06', true, NOW()),
('0602', 'Cajabamba',       '06', true, NOW()),
('0603', 'Celendín',        '06', true, NOW()),
('0604', 'Chota',           '06', true, NOW()),
('0605', 'Contumazá',       '06', true, NOW()),
('0606', 'Cutervo',         '06', true, NOW()),
('0607', 'Hualgayoc',       '06', true, NOW()),
('0608', 'Jaén',            '06', true, NOW()),
('0609', 'San Ignacio',     '06', true, NOW()),
('0610', 'San Marcos',      '06', true, NOW()),
('0611', 'San Miguel',      '06', true, NOW()),
('0612', 'San Pablo',       '06', true, NOW()),
('0613', 'Santa Cruz',      '06', true, NOW());

-- Callao (07) — 1 province
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('0701', 'Callao', '07', true, NOW());

-- Cusco (08) — 13 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('0801', 'Cusco',          '08', true, NOW()),
('0802', 'Acomayo',        '08', true, NOW()),
('0803', 'Anta',           '08', true, NOW()),
('0804', 'Calca',          '08', true, NOW()),
('0805', 'Canas',          '08', true, NOW()),
('0806', 'Canchis',        '08', true, NOW()),
('0807', 'Chumbivilcas',   '08', true, NOW()),
('0808', 'Espinar',        '08', true, NOW()),
('0809', 'La Convención',  '08', true, NOW()),
('0810', 'Paruro',         '08', true, NOW()),
('0811', 'Paucartambo',    '08', true, NOW()),
('0812', 'Quispicanchi',   '08', true, NOW()),
('0813', 'Urubamba',       '08', true, NOW());

-- Huancavelica (09) — 7 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('0901', 'Huancavelica',      '09', true, NOW()),
('0902', 'Acobamba',          '09', true, NOW()),
('0903', 'Angaraes',          '09', true, NOW()),
('0904', 'Castrovirreyna',    '09', true, NOW()),
('0905', 'Churcampa',         '09', true, NOW()),
('0906', 'Huaytará',          '09', true, NOW()),
('0907', 'Tayacaja',          '09', true, NOW());

-- Huánuco (10) — 11 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('1001', 'Huánuco',         '10', true, NOW()),
('1002', 'Ambo',            '10', true, NOW()),
('1003', 'Dos de Mayo',     '10', true, NOW()),
('1004', 'Huacaybamba',     '10', true, NOW()),
('1005', 'Huamalíes',       '10', true, NOW()),
('1006', 'Leoncio Prado',   '10', true, NOW()),
('1007', 'Marañón',         '10', true, NOW()),
('1008', 'Pachitea',        '10', true, NOW()),
('1009', 'Puerto Inca',     '10', true, NOW()),
('1010', 'Lauricocha',      '10', true, NOW()),
('1011', 'Yarowilca',       '10', true, NOW());

-- Ica (11) — 5 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('1101', 'Ica',        '11', true, NOW()),
('1102', 'Chincha',    '11', true, NOW()),
('1103', 'Nazca',      '11', true, NOW()),
('1104', 'Palpa',      '11', true, NOW()),
('1105', 'Pisco',      '11', true, NOW());

-- Junín (12) — 9 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('1201', 'Huancayo',      '12', true, NOW()),
('1202', 'Concepción',    '12', true, NOW()),
('1203', 'Chanchamayo',   '12', true, NOW()),
('1204', 'Jauja',         '12', true, NOW()),
('1205', 'Junín',         '12', true, NOW()),
('1206', 'Satipo',        '12', true, NOW()),
('1207', 'Tarma',         '12', true, NOW()),
('1208', 'Yauli',         '12', true, NOW()),
('1209', 'Chupaca',       '12', true, NOW());

-- La Libertad (13) — 12 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('1301', 'Trujillo',          '13', true, NOW()),
('1302', 'Ascope',            '13', true, NOW()),
('1303', 'Bolívar',           '13', true, NOW()),
('1304', 'Chepén',            '13', true, NOW()),
('1305', 'Julcán',            '13', true, NOW()),
('1306', 'Otuzco',            '13', true, NOW()),
('1307', 'Pacasmayo',         '13', true, NOW()),
('1308', 'Pataz',             '13', true, NOW()),
('1309', 'Sánchez Carrión',   '13', true, NOW()),
('1310', 'Santiago de Chuco', '13', true, NOW()),
('1311', 'Gran Chimú',        '13', true, NOW()),
('1312', 'Virú',              '13', true, NOW());

-- Lambayeque (14) — 3 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('1401', 'Chiclayo',   '14', true, NOW()),
('1402', 'Ferreñafe',  '14', true, NOW()),
('1403', 'Lambayeque', '14', true, NOW());

-- Lima (15) — 10 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('1501', 'Lima',              '15', true, NOW()),
('1502', 'Barranca',          '15', true, NOW()),
('1503', 'Cajatambo',         '15', true, NOW()),
('1504', 'Canta',             '15', true, NOW()),
('1505', 'Cañete',            '15', true, NOW()),
('1506', 'Huaral',            '15', true, NOW()),
('1507', 'Huarochirí',        '15', true, NOW()),
('1508', 'Huaura',            '15', true, NOW()),
('1509', 'Oyón',              '15', true, NOW()),
('1510', 'Yauyos',            '15', true, NOW());

-- Loreto (16) — 8 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('1601', 'Maynas',              '16', true, NOW()),
('1602', 'Alto Amazonas',       '16', true, NOW()),
('1603', 'Loreto',              '16', true, NOW()),
('1604', 'Mariscal Ramón Castilla', '16', true, NOW()),
('1605', 'Requena',             '16', true, NOW()),
('1606', 'Ucayali',             '16', true, NOW()),
('1607', 'Datem del Marañón',   '16', true, NOW()),
('1608', 'Putumayo',            '16', true, NOW());

-- Madre de Dios (17) — 3 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('1701', 'Tambopata',      '17', true, NOW()),
('1702', 'Manu',           '17', true, NOW()),
('1703', 'Tahuamanu',      '17', true, NOW());

-- Moquegua (18) — 3 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('1801', 'Mariscal Nieto', '18', true, NOW()),
('1802', 'General Sánchez Cerro', '18', true, NOW()),
('1803', 'Ilo',            '18', true, NOW());

-- Pasco (19) — 3 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('1901', 'Pasco',     '19', true, NOW()),
('1902', 'Daniel Alcides Carrión', '19', true, NOW()),
('1903', 'Oxapampa',  '19', true, NOW());

-- Piura (20) — 8 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('2001', 'Piura',          '20', true, NOW()),
('2002', 'Ayabaca',        '20', true, NOW()),
('2003', 'Huancabamba',    '20', true, NOW()),
('2004', 'Morropón',       '20', true, NOW()),
('2005', 'Paita',          '20', true, NOW()),
('2006', 'Sullana',        '20', true, NOW()),
('2007', 'Talara',         '20', true, NOW()),
('2008', 'Sechura',        '20', true, NOW());

-- Puno (21) — 13 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('2101', 'Puno',               '21', true, NOW()),
('2102', 'Azángaro',           '21', true, NOW()),
('2103', 'Carabaya',           '21', true, NOW()),
('2104', 'Chucuito',           '21', true, NOW()),
('2105', 'El Collao',          '21', true, NOW()),
('2106', 'Huancané',           '21', true, NOW()),
('2107', 'Lampa',              '21', true, NOW()),
('2108', 'Melgar',             '21', true, NOW()),
('2109', 'Moho',               '21', true, NOW()),
('2110', 'San Antonio de Putina', '21', true, NOW()),
('2111', 'San Román',          '21', true, NOW()),
('2112', 'Sandia',             '21', true, NOW()),
('2113', 'Yunguyo',            '21', true, NOW());

-- San Martín (22) — 10 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('2201', 'Moyobamba',          '22', true, NOW()),
('2202', 'Bellavista',         '22', true, NOW()),
('2203', 'El Dorado',          '22', true, NOW()),
('2204', 'Huallaga',           '22', true, NOW()),
('2205', 'Lamas',              '22', true, NOW()),
('2206', 'Mariscal Cáceres',   '22', true, NOW()),
('2207', 'Picota',             '22', true, NOW()),
('2208', 'Rioja',              '22', true, NOW()),
('2209', 'San Martín',         '22', true, NOW()),
('2210', 'Tocache',            '22', true, NOW());

-- Tacna (23) — 4 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('2301', 'Tacna',          '23', true, NOW()),
('2302', 'Candarave',      '23', true, NOW()),
('2303', 'Jorge Basadre',  '23', true, NOW()),
('2304', 'Tarata',         '23', true, NOW());

-- Tumbes (24) — 3 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('2401', 'Tumbes',       '24', true, NOW()),
('2402', 'Contralmirante Villar', '24', true, NOW()),
('2403', 'Zarumilla',    '24', true, NOW());

-- Ucayali (25) — 4 provinces
INSERT INTO tb_ubigeo_provincias (ubprov_codigo, ubprov_nombre, ubprov_departamento, ubprov_activo, ubprov_created_at) VALUES
('2501', 'Coronel Portillo', '25', true, NOW()),
('2502', 'Atalaya',          '25', true, NOW()),
('2503', 'Padre Abad',       '25', true, NOW()),
('2504', 'Purús',            '25', true, NOW());

-- ============================================================
-- DISTRICTS — organized by province
-- ============================================================

-- Amazonas / Chachapoyas (0101)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('010101', 'Chachapoyas', '0101', true, NOW()),
('010102', 'Asunción', '0101', true, NOW()),
('010103', 'Balsas', '0101', true, NOW()),
('010104', 'Cheto', '0101', true, NOW()),
('010105', 'Chiliquín', '0101', true, NOW()),
('010106', 'Chuquibamba', '0101', true, NOW()),
('010107', 'Granada', '0101', true, NOW()),
('010108', 'Huancas', '0101', true, NOW()),
('010109', 'La Jalca', '0101', true, NOW()),
('010110', 'Leimebamba', '0101', true, NOW()),
('010111', 'Levanto', '0101', true, NOW()),
('010112', 'Magdalena', '0101', true, NOW()),
('010113', 'Mariscal Castilla', '0101', true, NOW()),
('010114', 'Molinopampa', '0101', true, NOW()),
('010115', 'Montevideo', '0101', true, NOW()),
('010116', 'Olleros', '0101', true, NOW()),
('010117', 'Quinjalca', '0101', true, NOW()),
('010118', 'San Francisco de Daguas', '0101', true, NOW()),
('010119', 'San Isidro de Maino', '0101', true, NOW()),
('010120', 'Soloco', '0101', true, NOW()),
('010121', 'Sonche', '0101', true, NOW());

-- Amazonas / Bagua (0102)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('010201', 'Bagua', '0102', true, NOW()),
('010202', 'Aramango', '0102', true, NOW()),
('010203', 'Copallín', '0102', true, NOW()),
('010204', 'El Parco', '0102', true, NOW()),
('010205', 'Imaza', '0102', true, NOW()),
('010206', 'La Peca', '0102', true, NOW());

-- Amazonas / Bongará (0103)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('010301', 'Jumbilla', '0103', true, NOW()),
('010302', 'Chisquilla', '0103', true, NOW()),
('010303', 'Churuja', '0103', true, NOW()),
('010304', 'Corosha', '0103', true, NOW()),
('010305', 'Cuispes', '0103', true, NOW()),
('010306', 'Florida', '0103', true, NOW()),
('010307', 'Jazán', '0103', true, NOW()),
('010308', 'Recta', '0103', true, NOW()),
('010309', 'San Carlos', '0103', true, NOW()),
('010310', 'Shipasbamba', '0103', true, NOW()),
('010311', 'Valera', '0103', true, NOW()),
('010312', 'Yambrasbamba', '0103', true, NOW());

-- Amazonas / Condorcanqui (0104)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('010401', 'Nieva', '0104', true, NOW()),
('010402', 'El Cenepa', '0104', true, NOW()),
('010403', 'Río Santiago', '0104', true, NOW());

-- Amazonas / Luya (0105)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('010501', 'Lamud', '0105', true, NOW()),
('010502', 'Camporredondo', '0105', true, NOW()),
('010503', 'Cocabamba', '0105', true, NOW()),
('010504', 'Colcamar', '0105', true, NOW()),
('010505', 'Conila', '0105', true, NOW()),
('010506', 'Inguilpata', '0105', true, NOW()),
('010507', 'Longuita', '0105', true, NOW()),
('010508', 'Lonya Chico', '0105', true, NOW()),
('010509', 'Luya', '0105', true, NOW()),
('010510', 'Luya Viejo', '0105', true, NOW()),
('010511', 'María', '0105', true, NOW()),
('010512', 'Ocalli', '0105', true, NOW()),
('010513', 'Ocumal', '0105', true, NOW()),
('010514', 'Pisuquía', '0105', true, NOW()),
('010515', 'Providencia', '0105', true, NOW()),
('010516', 'San Cristóbal', '0105', true, NOW()),
('010517', 'San Francisco de Yeso', '0105', true, NOW()),
('010518', 'San Jerónimo', '0105', true, NOW()),
('010519', 'San Juan de Lopecancha', '0105', true, NOW()),
('010520', 'Santa Catalina', '0105', true, NOW()),
('010521', 'Santo Tomás', '0105', true, NOW()),
('010522', 'Tingo', '0105', true, NOW()),
('010523', 'Trita', '0105', true, NOW());

-- Amazonas / Rodríguez de Mendoza (0106)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('010601', 'San Nicolás', '0106', true, NOW()),
('010602', 'Chirimoto', '0106', true, NOW()),
('010603', 'Cochamal', '0106', true, NOW()),
('010604', 'Huambo', '0106', true, NOW()),
('010605', 'Limabamba', '0106', true, NOW()),
('010606', 'Longar', '0106', true, NOW()),
('010607', 'Mariscal Benavides', '0106', true, NOW()),
('010608', 'Milpuc', '0106', true, NOW()),
('010609', 'Omia', '0106', true, NOW()),
('010610', 'Santa Rosa', '0106', true, NOW()),
('010611', 'Totora', '0106', true, NOW()),
('010612', 'Vista Alegre', '0106', true, NOW());

-- Amazonas / Utcubamba (0107)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('010701', 'Bagua Grande', '0107', true, NOW()),
('010702', 'Cajaruro', '0107', true, NOW()),
('010703', 'Cumba', '0107', true, NOW()),
('010704', 'El Milagro', '0107', true, NOW()),
('010705', 'Jamalca', '0107', true, NOW()),
('010706', 'Lonya Grande', '0107', true, NOW()),
('010707', 'Yamón', '0107', true, NOW());

-- Áncash / Huaraz (0201)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('020101', 'Huaraz', '0201', true, NOW()),
('020102', 'Cochabamba', '0201', true, NOW()),
('020103', 'Colcabamba', '0201', true, NOW()),
('020104', 'Huanchay', '0201', true, NOW()),
('020105', 'Independencia', '0201', true, NOW()),
('020106', 'Jangas', '0201', true, NOW()),
('020107', 'La Libertad', '0201', true, NOW()),
('020108', 'Olleros', '0201', true, NOW()),
('020109', 'Pampas', '0201', true, NOW()),
('020110', 'Pariacoto', '0201', true, NOW()),
('020111', 'Pira', '0201', true, NOW()),
('020112', 'Tarica', '0201', true, NOW());

-- Áncash / Aija (0202)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('020201', 'Aija', '0202', true, NOW()),
('020202', 'Coris', '0202', true, NOW()),
('020203', 'Huacllán', '0202', true, NOW()),
('020204', 'La Merced', '0202', true, NOW()),
('020205', 'Succha', '0202', true, NOW());

-- Áncash / Antonio Raymondi (0203)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('020301', 'Llamellín', '0203', true, NOW()),
('020302', 'Aczo', '0203', true, NOW()),
('020303', 'Chaccho', '0203', true, NOW()),
('020304', 'Chingas', '0203', true, NOW()),
('020305', 'Mirgas', '0203', true, NOW()),
('020306', 'San Juan de Rontoy', '0203', true, NOW());

-- Áncash / Asunción (0204)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('020401', 'Chacas', '0204', true, NOW()),
('020402', 'Acochaca', '0204', true, NOW());

-- Áncash / Bolognesi (0205)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('020501', 'Chiquián', '0205', true, NOW()),
('020502', 'Abelardo Pardo Lezameta', '0205', true, NOW()),
('020503', 'Antonio Raymondi', '0205', true, NOW()),
('020504', 'Aquia', '0205', true, NOW()),
('020505', 'Cajacay', '0205', true, NOW()),
('020506', 'Canis', '0205', true, NOW()),
('020507', 'Colquioc', '0205', true, NOW()),
('020508', 'Huallanca', '0205', true, NOW()),
('020509', 'Huasta', '0205', true, NOW()),
('020510', 'Huayllacayán', '0205', true, NOW()),
('020511', 'La Primavera', '0205', true, NOW()),
('020512', 'Mangas', '0205', true, NOW()),
('020513', 'Pacllón', '0205', true, NOW()),
('020514', 'San Miguel de Corpanqui', '0205', true, NOW()),
('020515', 'Ticllos', '0205', true, NOW());

-- Áncash / Carhuaz (0206)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('020601', 'Carhuaz', '0206', true, NOW()),
('020602', 'Acopampa', '0206', true, NOW()),
('020603', 'Amashca', '0206', true, NOW()),
('020604', 'Anta', '0206', true, NOW()),
('020605', 'Ataquero', '0206', true, NOW()),
('020606', 'Marcará', '0206', true, NOW()),
('020607', 'Pariahuanca', '0206', true, NOW()),
('020608', 'San Miguel de Aco', '0206', true, NOW()),
('020609', 'Shilla', '0206', true, NOW()),
('020610', 'Tinco', '0206', true, NOW()),
('020611', 'Yungar', '0206', true, NOW());

-- Áncash / Carlos Fermín Fitzcarrald (0207)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('020701', 'San Luis', '0207', true, NOW()),
('020702', 'San Nicolás', '0207', true, NOW()),
('020703', 'Yauya', '0207', true, NOW());

-- Áncash / Casma (0208)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('020801', 'Casma', '0208', true, NOW()),
('020802', 'Buena Vista Alta', '0208', true, NOW()),
('020803', 'Comandante Noel', '0208', true, NOW()),
('020804', 'Yaután', '0208', true, NOW());

-- Áncash / Corongo (0209)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('020901', 'Corongo', '0209', true, NOW()),
('020902', 'Aco', '0209', true, NOW()),
('020903', 'Bambas', '0209', true, NOW()),
('020904', 'Cusca', '0209', true, NOW()),
('020905', 'La Pampa', '0209', true, NOW()),
('020906', 'Yanac', '0209', true, NOW()),
('020907', 'Yupán', '0209', true, NOW());

-- Áncash / Huari (0210)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('021001', 'Huari', '0210', true, NOW()),
('021002', 'Anra', '0210', true, NOW()),
('021003', 'Cajay', '0210', true, NOW()),
('021004', 'Chavín de Huantar', '0210', true, NOW()),
('021005', 'Huacachi', '0210', true, NOW()),
('021006', 'Huacchis', '0210', true, NOW()),
('021007', 'Huachis', '0210', true, NOW()),
('021008', 'Huantar', '0210', true, NOW()),
('021009', 'Masin', '0210', true, NOW()),
('021010', 'Paucas', '0210', true, NOW()),
('021011', 'Ponto', '0210', true, NOW()),
('021012', 'Rahuapampa', '0210', true, NOW()),
('021013', 'Rapayán', '0210', true, NOW()),
('021014', 'San Marcos', '0210', true, NOW()),
('021015', 'San Pedro de Chana', '0210', true, NOW()),
('021016', 'Uco', '0210', true, NOW());

-- Áncash / Huarmey (0211)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('021101', 'Huarmey', '0211', true, NOW()),
('021102', 'Cochapeti', '0211', true, NOW()),
('021103', 'Culebras', '0211', true, NOW()),
('021104', 'Huayán', '0211', true, NOW()),
('021105', 'Malvas', '0211', true, NOW());

-- Áncash / Huaylas (0212)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('021201', 'Caraz', '0212', true, NOW()),
('021202', 'Huallanca', '0212', true, NOW()),
('021203', 'Huata', '0212', true, NOW()),
('021204', 'Huaylas', '0212', true, NOW()),
('021205', 'Mato', '0212', true, NOW()),
('021206', 'Pamparomas', '0212', true, NOW()),
('021207', 'Pueblo Libre', '0212', true, NOW()),
('021208', 'Santa Cruz', '0212', true, NOW()),
('021209', 'Santo Toribio', '0212', true, NOW()),
('021210', 'Yuracmarca', '0212', true, NOW());

-- Áncash / Mariscal Luzuriaga (0213)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('021301', 'Piscobamba', '0213', true, NOW()),
('021302', 'Casca', '0213', true, NOW()),
('021303', 'Eleazar Guzmán Barrón', '0213', true, NOW()),
('021304', 'Fidel Olivas Escudero', '0213', true, NOW()),
('021305', 'Llama', '0213', true, NOW()),
('021306', 'Llumpa', '0213', true, NOW()),
('021307', 'Lucma', '0213', true, NOW()),
('021308', 'Musga', '0213', true, NOW());

-- Áncash / Ocros (0214)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('021401', 'Ocros', '0214', true, NOW()),
('021402', 'Acas', '0214', true, NOW()),
('021403', 'Cajamarquilla', '0214', true, NOW()),
('021404', 'Carhuapampa', '0214', true, NOW()),
('021405', 'Cochas', '0214', true, NOW()),
('021406', 'Congas', '0214', true, NOW()),
('021407', 'Llipa', '0214', true, NOW()),
('021408', 'San Cristóbal de Raján', '0214', true, NOW()),
('021409', 'San Pedro', '0214', true, NOW()),
('021410', 'Santiago de Chilcas', '0214', true, NOW());

-- Áncash / Pallasca (0215)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('021501', 'Cabana', '0215', true, NOW()),
('021502', 'Bolognesi', '0215', true, NOW()),
('021503', 'Conchucos', '0215', true, NOW()),
('021504', 'Huacaschuque', '0215', true, NOW()),
('021505', 'Huandoval', '0215', true, NOW()),
('021506', 'Lacabamba', '0215', true, NOW()),
('021507', 'Llapo', '0215', true, NOW()),
('021508', 'Pallasca', '0215', true, NOW()),
('021509', 'Pampas', '0215', true, NOW()),
('021510', 'Santa Rosa', '0215', true, NOW()),
('021511', 'Tauca', '0215', true, NOW());

-- Áncash / Pomabamba (0216)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('021601', 'Pomabamba', '0216', true, NOW()),
('021602', 'Huayllán', '0216', true, NOW()),
('021603', 'Parobamba', '0216', true, NOW()),
('021604', 'Quinuabamba', '0216', true, NOW());

-- Áncash / Recuay (0217)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('021701', 'Recuay', '0217', true, NOW()),
('021702', 'Catac', '0217', true, NOW()),
('021703', 'Cotaparaco', '0217', true, NOW()),
('021704', 'Huayllapampa', '0217', true, NOW()),
('021705', 'Llacllín', '0217', true, NOW()),
('021706', 'Marca', '0217', true, NOW()),
('021707', 'Pampas Chico', '0217', true, NOW()),
('021708', 'Pararín', '0217', true, NOW()),
('021709', 'Tapacocha', '0217', true, NOW()),
('021710', 'Ticapampa', '0217', true, NOW());

-- Áncash / Santa (0218)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('021801', 'Chimbote', '0218', true, NOW()),
('021802', 'Cáceres del Perú', '0218', true, NOW()),
('021803', 'Coishco', '0218', true, NOW()),
('021804', 'Macate', '0218', true, NOW()),
('021805', 'Moro', '0218', true, NOW()),
('021806', 'Nepeña', '0218', true, NOW()),
('021807', 'Samanco', '0218', true, NOW()),
('021808', 'Santa', '0218', true, NOW()),
('021809', 'Nuevo Chimbote', '0218', true, NOW());

-- Áncash / Sihuas (0219)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('021901', 'Sihuas', '0219', true, NOW()),
('021902', 'Acobamba', '0219', true, NOW()),
('021903', 'Alfonso Ugarte', '0219', true, NOW()),
('021904', 'Cashapampa', '0219', true, NOW()),
('021905', 'Chingalpo', '0219', true, NOW()),
('021906', 'Huayllabamba', '0219', true, NOW()),
('021907', 'Quiches', '0219', true, NOW()),
('021908', 'Ragash', '0219', true, NOW()),
('021909', 'San Juan', '0219', true, NOW()),
('021910', 'Sicsibamba', '0219', true, NOW());

-- Áncash / Yungay (0220)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('022001', 'Yungay', '0220', true, NOW()),
('022002', 'Cascapara', '0220', true, NOW()),
('022003', 'Mancos', '0220', true, NOW()),
('022004', 'Matacoto', '0220', true, NOW()),
('022005', 'Quillo', '0220', true, NOW()),
('022006', 'Ranrahirca', '0220', true, NOW()),
('022007', 'Shupluy', '0220', true, NOW()),
('022008', 'Yanama', '0220', true, NOW());

-- Apurímac / Abancay (0301)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('030101', 'Abancay', '0301', true, NOW()),
('030102', 'Chacoche', '0301', true, NOW()),
('030103', 'Circa', '0301', true, NOW()),
('030104', 'Curahuasi', '0301', true, NOW()),
('030105', 'Huanipaca', '0301', true, NOW()),
('030106', 'Lambrama', '0301', true, NOW()),
('030107', 'Pichirhua', '0301', true, NOW()),
('030108', 'San Pedro de Cachora', '0301', true, NOW()),
('030109', 'Tamburco', '0301', true, NOW());

-- Apurímac / Andahuaylas (0302)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('030201', 'Andahuaylas', '0302', true, NOW()),
('030202', 'Andarapa', '0302', true, NOW()),
('030203', 'Chiara', '0302', true, NOW()),
('030204', 'Huancarama', '0302', true, NOW()),
('030205', 'Huancaray', '0302', true, NOW()),
('030206', 'Huayana', '0302', true, NOW()),
('030207', 'Kishuara', '0302', true, NOW()),
('030208', 'Pacobamba', '0302', true, NOW()),
('030209', 'Pacucha', '0302', true, NOW()),
('030210', 'Pampachiri', '0302', true, NOW()),
('030211', 'Pomacocha', '0302', true, NOW()),
('030212', 'San Antonio de Cachi', '0302', true, NOW()),
('030213', 'San Jerónimo', '0302', true, NOW()),
('030214', 'San Miguel de Chaccrampa', '0302', true, NOW()),
('030215', 'Santa María de Chicmo', '0302', true, NOW()),
('030216', 'Talavera', '0302', true, NOW()),
('030217', 'Tumay Huaraca', '0302', true, NOW()),
('030218', 'Turpo', '0302', true, NOW()),
('030219', 'Kaquiabamba', '0302', true, NOW()),
('030220', 'José María Arguedas', '0302', true, NOW());

-- Apurímac / Antabamba (0303)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('030301', 'Antabamba', '0303', true, NOW()),
('030302', 'El Oro', '0303', true, NOW()),
('030303', 'Huaquirca', '0303', true, NOW()),
('030304', 'Juan Espinoza Medrano', '0303', true, NOW()),
('030305', 'Oropesa', '0303', true, NOW()),
('030306', 'Pachaconas', '0303', true, NOW()),
('030307', 'Sabaino', '0303', true, NOW());

-- Apurímac / Aymaraes (0304)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('030401', 'Chalhuanca', '0304', true, NOW()),
('030402', 'Capaya', '0304', true, NOW()),
('030403', 'Caraybamba', '0304', true, NOW()),
('030404', 'Chapimarca', '0304', true, NOW()),
('030405', 'Colcabamba', '0304', true, NOW()),
('030406', 'Cotaruse', '0304', true, NOW()),
('030407', 'Huayllo', '0304', true, NOW()),
('030408', 'Justo Apu Sahuaraura', '0304', true, NOW()),
('030409', 'Lucre', '0304', true, NOW()),
('030410', 'Pocohuanca', '0304', true, NOW()),
('030411', 'San Juan de Chacña', '0304', true, NOW()),
('030412', 'Sañayca', '0304', true, NOW()),
('030413', 'Soraya', '0304', true, NOW()),
('030414', 'Tapairihua', '0304', true, NOW()),
('030415', 'Tintay', '0304', true, NOW()),
('030416', 'Toraya', '0304', true, NOW()),
('030417', 'Yanaca', '0304', true, NOW());

-- Apurímac / Cotabambas (0305)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('030501', 'Tambobamba', '0305', true, NOW()),
('030502', 'Cotabambas', '0305', true, NOW()),
('030503', 'Coyllurqui', '0305', true, NOW()),
('030504', 'Haquira', '0305', true, NOW()),
('030505', 'Mara', '0305', true, NOW()),
('030506', 'Challhuahuacho', '0305', true, NOW());

-- Apurímac / Chincheros (0306)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('030601', 'Chincheros', '0306', true, NOW()),
('030602', 'Anco_Huallo', '0306', true, NOW()),
('030603', 'Cocharcas', '0306', true, NOW()),
('030604', 'Huaccana', '0306', true, NOW()),
('030605', 'Ocobamba', '0306', true, NOW()),
('030606', 'Onoy', '0306', true, NOW()),
('030607', 'Ranracancha', '0306', true, NOW()),
('030608', 'Rocchacc', '0306', true, NOW()),
('030609', 'El Porvenir', '0306', true, NOW()),
('030610', 'Los Chankas', '0306', true, NOW());

-- Apurímac / Grau (0307)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('030701', 'Chuquibambilla', '0307', true, NOW()),
('030702', 'Curpahuasi', '0307', true, NOW()),
('030703', 'Gamarra', '0307', true, NOW()),
('030704', 'Huayllati', '0307', true, NOW()),
('030705', 'Mamara', '0307', true, NOW()),
('030706', 'Micaela Bastidas', '0307', true, NOW()),
('030707', 'Pataypampa', '0307', true, NOW()),
('030708', 'Progreso', '0307', true, NOW()),
('030709', 'San Antonio', '0307', true, NOW()),
('030710', 'Santa Rosa', '0307', true, NOW()),
('030711', 'Turpay', '0307', true, NOW()),
('030712', 'Vilcabamba', '0307', true, NOW()),
('030713', 'Virundo', '0307', true, NOW()),
('030714', 'Curasco', '0307', true, NOW());

-- Arequipa / Arequipa (0401)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('040101', 'Arequipa', '0401', true, NOW()),
('040102', 'Alto Selva Alegre', '0401', true, NOW()),
('040103', 'Cayma', '0401', true, NOW()),
('040104', 'Cerro Colorado', '0401', true, NOW()),
('040105', 'Characato', '0401', true, NOW()),
('040106', 'Chiguata', '0401', true, NOW()),
('040107', 'Jacobo Hunter', '0401', true, NOW()),
('040108', 'José Luis Bustamante y Rivero', '0401', true, NOW()),
('040109', 'La Joya', '0401', true, NOW()),
('040110', 'Mariano Melgar', '0401', true, NOW()),
('040111', 'Miraflores', '0401', true, NOW()),
('040112', 'Mollebaya', '0401', true, NOW()),
('040113', 'Paucarpata', '0401', true, NOW()),
('040114', 'Pocsi', '0401', true, NOW()),
('040115', 'Polobaya', '0401', true, NOW()),
('040116', 'Quequeña', '0401', true, NOW()),
('040117', 'Sabandía', '0401', true, NOW()),
('040118', 'Sachaca', '0401', true, NOW()),
('040119', 'San Juan de Siguas', '0401', true, NOW()),
('040120', 'San Juan de Tarucani', '0401', true, NOW()),
('040121', 'Santa Isabel de Siguas', '0401', true, NOW()),
('040122', 'Santa Rita de Siguas', '0401', true, NOW()),
('040123', 'Socabaya', '0401', true, NOW()),
('040124', 'Tiabaya', '0401', true, NOW()),
('040125', 'Uchumayo', '0401', true, NOW()),
('040126', 'Vítor', '0401', true, NOW()),
('040127', 'Yanahuara', '0401', true, NOW()),
('040128', 'Yarabamba', '0401', true, NOW()),
('040129', 'Yura', '0401', true, NOW()),
('040130', 'José María Quimper', '0401', true, NOW());

-- Arequipa / Camaná (0402)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('040201', 'Camaná', '0402', true, NOW()),
('040202', 'José María Quimper', '0402', true, NOW()),
('040203', 'Mariano Nicolás Valcárcel', '0402', true, NOW()),
('040204', 'Mariscal Cáceres', '0402', true, NOW()),
('040205', 'Nicolás de Piérola', '0402', true, NOW()),
('040206', 'Ocoña', '0402', true, NOW()),
('040207', 'Quilca', '0402', true, NOW()),
('040208', 'Samuel Pastor', '0402', true, NOW());

-- Arequipa / Caravelí (0403)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('040301', 'Caravelí', '0403', true, NOW()),
('040302', 'Acarí', '0403', true, NOW()),
('040303', 'Atico', '0403', true, NOW()),
('040304', 'Atiquipa', '0403', true, NOW()),
('040305', 'Bella Unión', '0403', true, NOW()),
('040306', 'Cahuacho', '0403', true, NOW()),
('040307', 'Chala', '0403', true, NOW()),
('040308', 'Chaparra', '0403', true, NOW()),
('040309', 'Huanuhuanu', '0403', true, NOW()),
('040310', 'Jaqui', '0403', true, NOW()),
('040311', 'Lomas', '0403', true, NOW()),
('040312', 'Quicacha', '0403', true, NOW()),
('040313', 'Yauca', '0403', true, NOW());

-- Arequipa / Castilla (0404)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('040401', 'Aplao', '0404', true, NOW()),
('040402', 'Andagua', '0404', true, NOW()),
('040403', 'Ayo', '0404', true, NOW()),
('040404', 'Chachas', '0404', true, NOW()),
('040405', 'Chilcaymarca', '0404', true, NOW()),
('040406', 'Choco', '0404', true, NOW()),
('040407', 'Huancarqui', '0404', true, NOW()),
('040408', 'Machaguay', '0404', true, NOW()),
('040409', 'Orcopampa', '0404', true, NOW()),
('040410', 'Pampacolca', '0404', true, NOW()),
('040411', 'Tipán', '0404', true, NOW()),
('040412', 'Uñón', '0404', true, NOW()),
('040413', 'Uraca', '0404', true, NOW()),
('040414', 'Viraco', '0404', true, NOW());

-- Arequipa / Caylloma (0405)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('040501', 'Chivay', '0405', true, NOW()),
('040502', 'Achoma', '0405', true, NOW()),
('040503', 'Cabanaconde', '0405', true, NOW()),
('040504', 'Callalli', '0405', true, NOW()),
('040505', 'Caylloma', '0405', true, NOW()),
('040506', 'Coporaque', '0405', true, NOW()),
('040507', 'Huambo', '0405', true, NOW()),
('040508', 'Huanca', '0405', true, NOW()),
('040509', 'Ichupampa', '0405', true, NOW()),
('040510', 'Lari', '0405', true, NOW()),
('040511', 'Lluta', '0405', true, NOW()),
('040512', 'Maca', '0405', true, NOW()),
('040513', 'Madrigal', '0405', true, NOW()),
('040514', 'San Antonio de Chuca', '0405', true, NOW()),
('040515', 'Sibayo', '0405', true, NOW()),
('040516', 'Tapay', '0405', true, NOW()),
('040517', 'Tisco', '0405', true, NOW()),
('040518', 'Tuti', '0405', true, NOW()),
('040519', 'Yanque', '0405', true, NOW()),
('040520', 'Majes', '0405', true, NOW());

-- Arequipa / Condesuyos (0406)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('040601', 'Chuquibamba', '0406', true, NOW()),
('040602', 'Andaray', '0406', true, NOW()),
('040603', 'Cayarani', '0406', true, NOW()),
('040604', 'Chichas', '0406', true, NOW()),
('040605', 'Iray', '0406', true, NOW()),
('040606', 'Río Grande', '0406', true, NOW()),
('040607', 'Salamanca', '0406', true, NOW()),
('040608', 'Yanaquihua', '0406', true, NOW());

-- Arequipa / Islay (0407)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('040701', 'Mollendo', '0407', true, NOW()),
('040702', 'Cocachacra', '0407', true, NOW()),
('040703', 'Dean Valdivia', '0407', true, NOW()),
('040704', 'Islay', '0407', true, NOW()),
('040705', 'Mejía', '0407', true, NOW()),
('040706', 'Punta de Bombón', '0407', true, NOW());

-- Arequipa / La Unión (0408)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('040801', 'Cotahuasi', '0408', true, NOW()),
('040802', 'Alca', '0408', true, NOW()),
('040803', 'Charcana', '0408', true, NOW()),
('040804', 'Huaynacotas', '0408', true, NOW()),
('040805', 'Pampamarca', '0408', true, NOW()),
('040806', 'Puyca', '0408', true, NOW()),
('040807', 'Quechualla', '0408', true, NOW()),
('040808', 'Sayla', '0408', true, NOW()),
('040809', 'Tauría', '0408', true, NOW()),
('040810', 'Tomepampa', '0408', true, NOW()),
('040811', 'Toro', '0408', true, NOW());

-- Ayacucho / Huamanga (0501)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('050101', 'Ayacucho', '0501', true, NOW()),
('050102', 'Acocro', '0501', true, NOW()),
('050103', 'Acos Vinchos', '0501', true, NOW()),
('050104', 'Carmen Alto', '0501', true, NOW()),
('050105', 'Chiara', '0501', true, NOW()),
('050106', 'Jesús Nazareno', '0501', true, NOW()),
('050107', 'Ocros', '0501', true, NOW()),
('050108', 'Pacaycasa', '0501', true, NOW()),
('050109', 'Quinua', '0501', true, NOW()),
('050110', 'San José de Ticllas', '0501', true, NOW()),
('050111', 'San Juan Bautista', '0501', true, NOW()),
('050112', 'Santiago de Pischa', '0501', true, NOW()),
('050113', 'Socos', '0501', true, NOW()),
('050114', 'Tambillo', '0501', true, NOW()),
('050115', 'Vinchos', '0501', true, NOW());

-- Ayacucho / Cangallo (0502)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('050201', 'Cangallo', '0502', true, NOW()),
('050202', 'Chuschi', '0502', true, NOW()),
('050203', 'Los Morochucos', '0502', true, NOW()),
('050204', 'María Parado de Bellido', '0502', true, NOW()),
('050205', 'Paras', '0502', true, NOW()),
('050206', 'Totos', '0502', true, NOW());

-- Ayacucho / Huanca Sancos (0503)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('050301', 'Huanca Sancos', '0503', true, NOW()),
('050302', 'Carapo', '0503', true, NOW()),
('050303', 'Sacsamarca', '0503', true, NOW()),
('050304', 'Santiago de Lucanamarca', '0503', true, NOW());

-- Ayacucho / Huanta (0504)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('050401', 'Huanta', '0504', true, NOW()),
('050402', 'Ayahuanco', '0504', true, NOW()),
('050403', 'Huamanguilla', '0504', true, NOW()),
('050404', 'Iguain', '0504', true, NOW()),
('050405', 'Llochegua', '0504', true, NOW()),
('050406', 'Luricocha', '0504', true, NOW()),
('050407', 'Santillana', '0504', true, NOW()),
('050408', 'Sivia', '0504', true, NOW()),
('050409', 'San Miguel', '0504', true, NOW()),
('050410', 'Pucacolpa', '0504', true, NOW()),
('050411', 'Chaca', '0504', true, NOW());

-- Ayacucho / La Mar (0505)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('050501', 'San Miguel', '0505', true, NOW()),
('050502', 'Anco', '0505', true, NOW()),
('050503', 'Ayna', '0505', true, NOW()),
('050504', 'Chilcas', '0505', true, NOW()),
('050505', 'Chungui', '0505', true, NOW()),
('050506', 'Luis Carranza', '0505', true, NOW()),
('050507', 'Santa Rosa', '0505', true, NOW()),
('050508', 'Tambo', '0505', true, NOW()),
('050509', 'Samugari', '0505', true, NOW()),
('050510', 'Anchihuay', '0505', true, NOW());

-- Ayacucho / Lucanas (0506)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('050601', 'Puquio', '0506', true, NOW()),
('050602', 'Aucara', '0506', true, NOW()),
('050603', 'Cabana', '0506', true, NOW()),
('050604', 'Carmen Salcedo', '0506', true, NOW()),
('050605', 'Chaviña', '0506', true, NOW()),
('050606', 'Chipao', '0506', true, NOW()),
('050607', 'Huac-Huas', '0506', true, NOW()),
('050608', 'Laramate', '0506', true, NOW()),
('050609', 'Leoncio Prado', '0506', true, NOW()),
('050610', 'Llauta', '0506', true, NOW()),
('050611', 'Lucanas', '0506', true, NOW()),
('050612', 'Ocaña', '0506', true, NOW()),
('050613', 'Otoca', '0506', true, NOW()),
('050614', 'Saisa', '0506', true, NOW()),
('050615', 'San Cristóbal', '0506', true, NOW()),
('050616', 'San Juan', '0506', true, NOW()),
('050617', 'San Pedro', '0506', true, NOW()),
('050618', 'San Pedro de Palco', '0506', true, NOW()),
('050619', 'Sancos', '0506', true, NOW()),
('050620', 'Santa Ana de Huaycahuacho', '0506', true, NOW()),
('050621', 'Santa Lucía', '0506', true, NOW());

-- Ayacucho / Parinacochas (0507)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('050701', 'Coracora', '0507', true, NOW()),
('050702', 'Chumpi', '0507', true, NOW()),
('050703', 'Coronel Castañeda', '0507', true, NOW()),
('050704', 'Pacapausa', '0507', true, NOW()),
('050705', 'Pullo', '0507', true, NOW()),
('050706', 'Puyusca', '0507', true, NOW()),
('050707', 'San Francisco de Ravacayco', '0507', true, NOW()),
('050708', 'Upahuacho', '0507', true, NOW());

-- Ayacucho / Páucar del Sara Sara (0508)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('050801', 'Pausa', '0508', true, NOW()),
('050802', 'Colta', '0508', true, NOW()),
('050803', 'Corculla', '0508', true, NOW()),
('050804', 'Lampa', '0508', true, NOW()),
('050805', 'Marcabamba', '0508', true, NOW()),
('050806', 'Oyolo', '0508', true, NOW()),
('050807', 'Pararca', '0508', true, NOW()),
('050808', 'San Javier de Alpabamba', '0508', true, NOW()),
('050809', 'San José de Ushua', '0508', true, NOW()),
('050810', 'Sara Sara', '0508', true, NOW());

-- Ayacucho / Sucre (0509)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('050901', 'Querobamba', '0509', true, NOW()),
('050902', 'Belén', '0509', true, NOW()),
('050903', 'Chalcos', '0509', true, NOW()),
('050904', 'Chilcayoc', '0509', true, NOW()),
('050905', 'Huacaña', '0509', true, NOW()),
('050906', 'Morcolla', '0509', true, NOW()),
('050907', 'Paico', '0509', true, NOW()),
('050908', 'San Pedro de Larcay', '0509', true, NOW()),
('050909', 'San Salvador de Quije', '0509', true, NOW()),
('050910', 'Santiago de Paucaray', '0509', true, NOW()),
('050911', 'Soras', '0509', true, NOW());

-- Ayacucho / Víctor Fajardo (0510)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('051001', 'Huancapi', '0510', true, NOW()),
('051002', 'Alcamenca', '0510', true, NOW()),
('051003', 'Apongo', '0510', true, NOW()),
('051004', 'Asquipata', '0510', true, NOW()),
('051005', 'Canaria', '0510', true, NOW()),
('051006', 'Cayara', '0510', true, NOW()),
('051007', 'Colca', '0510', true, NOW()),
('051008', 'Huamanquiquia', '0510', true, NOW()),
('051009', 'Huancaraylla', '0510', true, NOW()),
('051010', 'Huaya', '0510', true, NOW()),
('051011', 'Sarhua', '0510', true, NOW()),
('051012', 'Vilcanchos', '0510', true, NOW());

-- Ayacucho / Vilcas Huamán (0511)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('051101', 'Vilcas Huamán', '0511', true, NOW()),
('051102', 'Accomarca', '0511', true, NOW()),
('051103', 'Carhuanca', '0511', true, NOW()),
('051104', 'Concepción', '0511', true, NOW()),
('051105', 'Huambalpa', '0511', true, NOW()),
('051106', 'Independencia', '0511', true, NOW()),
('051107', 'Saurama', '0511', true, NOW()),
('051108', 'Vischongo', '0511', true, NOW());

-- Cajamarca / Cajamarca (0601)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('060101', 'Cajamarca', '0601', true, NOW()),
('060102', 'Asunción', '0601', true, NOW()),
('060103', 'Chetilla', '0601', true, NOW()),
('060104', 'Cospán', '0601', true, NOW()),
('060105', 'Encañada', '0601', true, NOW()),
('060106', 'Jesús', '0601', true, NOW()),
('060107', 'Llacanora', '0601', true, NOW()),
('060108', 'Los Baños del Inca', '0601', true, NOW()),
('060109', 'Magdalena', '0601', true, NOW()),
('060110', 'Matará', '0601', true, NOW()),
('060111', 'Namora', '0601', true, NOW()),
('060112', 'San Juan', '0601', true, NOW());

-- Cajamarca / Cajabamba (0602)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('060201', 'Cajabamba', '0602', true, NOW()),
('060202', 'Cachachi', '0602', true, NOW()),
('060203', 'Condebamba', '0602', true, NOW()),
('060204', 'Sitacocha', '0602', true, NOW());

-- Cajamarca / Celendín (0603)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('060301', 'Celendín', '0603', true, NOW()),
('060302', 'Chumuch', '0603', true, NOW()),
('060303', 'Cortegana', '0603', true, NOW()),
('060304', 'Huasmin', '0603', true, NOW()),
('060305', 'Jorge Chávez', '0603', true, NOW()),
('060306', 'José Gálvez', '0603', true, NOW()),
('060307', 'Miguel Iglesias', '0603', true, NOW()),
('060308', 'Oxamarca', '0603', true, NOW()),
('060309', 'Sorochuco', '0603', true, NOW()),
('060310', 'Sucre', '0603', true, NOW()),
('060311', 'Utco', '0603', true, NOW()),
('060312', 'La Libertad de Pallán', '0603', true, NOW());

-- Cajamarca / Chota (0604)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('060401', 'Chota', '0604', true, NOW()),
('060402', 'Anguía', '0604', true, NOW()),
('060403', 'Chadín', '0604', true, NOW()),
('060404', 'Chiguirip', '0604', true, NOW()),
('060405', 'Chimban', '0604', true, NOW()),
('060406', 'Choropampa', '0604', true, NOW()),
('060407', 'Cochabamba', '0604', true, NOW()),
('060408', 'Conchán', '0604', true, NOW()),
('060409', 'Huambos', '0604', true, NOW()),
('060410', 'Lajas', '0604', true, NOW()),
('060411', 'Llama', '0604', true, NOW()),
('060412', 'Miracosta', '0604', true, NOW()),
('060413', 'Paccha', '0604', true, NOW()),
('060414', 'Pión', '0604', true, NOW()),
('060415', 'Querocoto', '0604', true, NOW()),
('060416', 'San Juan de Licupis', '0604', true, NOW()),
('060417', 'Tacabamba', '0604', true, NOW()),
('060418', 'Tocmoche', '0604', true, NOW()),
('060419', 'Chalamarca', '0604', true, NOW());

-- Cajamarca / Contumazá (0605)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('060501', 'Contumazá', '0605', true, NOW()),
('060502', 'Chilete', '0605', true, NOW()),
('060503', 'Cupisnique', '0605', true, NOW()),
('060504', 'Guzmango', '0605', true, NOW()),
('060505', 'San Benito', '0605', true, NOW()),
('060506', 'Santa Cruz de Toledo', '0605', true, NOW()),
('060507', 'Tantarica', '0605', true, NOW()),
('060508', 'Yonán', '0605', true, NOW());

-- Cajamarca / Cutervo (0606)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('060601', 'Cutervo', '0606', true, NOW()),
('060602', 'Callayuc', '0606', true, NOW()),
('060603', 'Choros', '0606', true, NOW()),
('060604', 'Cujillo', '0606', true, NOW()),
('060605', 'La Ramada', '0606', true, NOW()),
('060606', 'Pimpingos', '0606', true, NOW()),
('060607', 'Querocotillo', '0606', true, NOW()),
('060608', 'San Andrés de Cutervo', '0606', true, NOW()),
('060609', 'San Juan de Cutervo', '0606', true, NOW()),
('060610', 'San Luis de Lucma', '0606', true, NOW()),
('060611', 'Santa Cruz', '0606', true, NOW()),
('060612', 'Santo Domingo de la Capilla', '0606', true, NOW()),
('060613', 'Santo Tomás', '0606', true, NOW()),
('060614', 'Socota', '0606', true, NOW()),
('060615', 'Toribio Casanova', '0606', true, NOW());

-- Cajamarca / Hualgayoc (0607)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('060701', 'Bambamarca', '0607', true, NOW()),
('060702', 'Chugur', '0607', true, NOW()),
('060703', 'Hualgayoc', '0607', true, NOW());

-- Cajamarca / Jaén (0608)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('060801', 'Jaén', '0608', true, NOW()),
('060802', 'Bellavista', '0608', true, NOW()),
('060803', 'Chontali', '0608', true, NOW()),
('060804', 'Colasay', '0608', true, NOW()),
('060805', 'Huabal', '0608', true, NOW()),
('060806', 'Las Pirias', '0608', true, NOW()),
('060807', 'Pomahuaca', '0608', true, NOW()),
('060808', 'Pucará', '0608', true, NOW()),
('060809', 'Sallique', '0608', true, NOW()),
('060810', 'San Felipe', '0608', true, NOW()),
('060811', 'San José del Alto', '0608', true, NOW()),
('060812', 'Santa Rosa', '0608', true, NOW());

-- Cajamarca / San Ignacio (0609)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('060901', 'San Ignacio', '0609', true, NOW()),
('060902', 'Chirinos', '0609', true, NOW()),
('060903', 'Huarango', '0609', true, NOW()),
('060904', 'La Coipa', '0609', true, NOW()),
('060905', 'Namballe', '0609', true, NOW()),
('060906', 'San José de Lourdes', '0609', true, NOW()),
('060907', 'Tabaconas', '0609', true, NOW());

-- Cajamarca / San Marcos (0610)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('061001', 'Pedro Gálvez', '0610', true, NOW()),
('061002', 'Chancay', '0610', true, NOW()),
('061003', 'Eduardo Villanueva', '0610', true, NOW()),
('061004', 'Gregorio Pita', '0610', true, NOW()),
('061005', 'Ichocán', '0610', true, NOW()),
('061006', 'José Manuel Quiroz', '0610', true, NOW()),
('061007', 'José Sabogal', '0610', true, NOW());

-- Cajamarca / San Miguel (0611)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('061101', 'San Miguel', '0611', true, NOW()),
('061102', 'Bolívar', '0611', true, NOW()),
('061103', 'Calquis', '0611', true, NOW()),
('061104', 'Catilluc', '0611', true, NOW()),
('061105', 'El Prado', '0611', true, NOW()),
('061106', 'La Florida', '0611', true, NOW()),
('061107', 'Llapa', '0611', true, NOW()),
('061108', 'Nanchoc', '0611', true, NOW()),
('061109', 'Niepos', '0611', true, NOW()),
('061110', 'San Gregorio', '0611', true, NOW()),
('061111', 'San Silvestre de Cochán', '0611', true, NOW()),
('061112', 'Tongod', '0611', true, NOW()),
('061113', 'Unión Agua Blanca', '0611', true, NOW());

-- Cajamarca / San Pablo (0612)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('061201', 'San Pablo', '0612', true, NOW()),
('061202', 'San Bernardino', '0612', true, NOW()),
('061203', 'San Luis', '0612', true, NOW()),
('061204', 'Tumbaden', '0612', true, NOW());

-- Cajamarca / Santa Cruz (0613)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('061301', 'Santa Cruz', '0613', true, NOW()),
('061302', 'Andabamba', '0613', true, NOW()),
('061303', 'Catache', '0613', true, NOW()),
('061304', 'Chancaybaños', '0613', true, NOW()),
('061305', 'La Esperanza', '0613', true, NOW()),
('061306', 'Ninabamba', '0613', true, NOW()),
('061307', 'Pulán', '0613', true, NOW()),
('061308', 'Saucepampa', '0613', true, NOW()),
('061309', 'Sexi', '0613', true, NOW()),
('061310', 'Uticyacu', '0613', true, NOW()),
('061311', 'Yauyucan', '0613', true, NOW());

-- Callao / Callao (0701)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('070101', 'Callao', '0701', true, NOW()),
('070102', 'Bellavista', '0701', true, NOW()),
('070103', 'Carmen de la Legua Reynoso', '0701', true, NOW()),
('070104', 'La Perla', '0701', true, NOW()),
('070105', 'La Punta', '0701', true, NOW()),
('070106', 'Ventanilla', '0701', true, NOW()),
('070107', 'Mi Perú', '0701', true, NOW());

-- Cusco / Cusco (0801)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('080101', 'Cusco', '0801', true, NOW()),
('080102', 'Ccorca', '0801', true, NOW()),
('080103', 'Poroy', '0801', true, NOW()),
('080104', 'San Jerónimo', '0801', true, NOW()),
('080105', 'San Sebastián', '0801', true, NOW()),
('080106', 'Santiago', '0801', true, NOW()),
('080107', 'Saylla', '0801', true, NOW()),
('080108', 'Wanchaq', '0801', true, NOW());

-- Cusco / Acomayo (0802)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('080201', 'Acomayo', '0802', true, NOW()),
('080202', 'Acopia', '0802', true, NOW()),
('080203', 'Acos', '0802', true, NOW()),
('080204', 'Mosoc Llacta', '0802', true, NOW()),
('080205', 'Pomacanchi', '0802', true, NOW()),
('080206', 'Rondocan', '0802', true, NOW()),
('080207', 'Sangarará', '0802', true, NOW());

-- Cusco / Anta (0803)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('080301', 'Anta', '0803', true, NOW()),
('080302', 'Chinchaypujio', '0803', true, NOW()),
('080303', 'Huarocondo', '0803', true, NOW()),
('080304', 'Limatambo', '0803', true, NOW()),
('080305', 'Mollepata', '0803', true, NOW()),
('080306', 'Pucyura', '0803', true, NOW()),
('080307', 'Surite', '0803', true, NOW()),
('080308', 'Zurite', '0803', true, NOW()),
('080309', 'Cachimayo', '0803', true, NOW());

-- Cusco / Calca (0804)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('080401', 'Calca', '0804', true, NOW()),
('080402', 'Coya', '0804', true, NOW()),
('080403', 'Lamay', '0804', true, NOW()),
('080404', 'Lares', '0804', true, NOW()),
('080405', 'Pisac', '0804', true, NOW()),
('080406', 'San Salvador', '0804', true, NOW()),
('080407', 'Taray', '0804', true, NOW()),
('080408', 'Yanatile', '0804', true, NOW());

-- Cusco / Canas (0805)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('080501', 'Yanaoca', '0805', true, NOW()),
('080502', 'Checca', '0805', true, NOW()),
('080503', 'Kunturkanki', '0805', true, NOW()),
('080504', 'Langui', '0805', true, NOW()),
('080505', 'Layo', '0805', true, NOW()),
('080506', 'Pampamarca', '0805', true, NOW()),
('080507', 'Quehue', '0805', true, NOW()),
('080508', 'Túpac Amaru', '0805', true, NOW());

-- Cusco / Canchis (0806)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('080601', 'Sicuani', '0806', true, NOW()),
('080602', 'Checacupe', '0806', true, NOW()),
('080603', 'Combapata', '0806', true, NOW()),
('080604', 'Marangani', '0806', true, NOW()),
('080605', 'Pitumarca', '0806', true, NOW()),
('080606', 'San Pablo', '0806', true, NOW()),
('080607', 'San Pedro', '0806', true, NOW()),
('080608', 'Tinta', '0806', true, NOW());

-- Cusco / Chumbivilcas (0807)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('080701', 'Santo Tomás', '0807', true, NOW()),
('080702', 'Capacmarca', '0807', true, NOW()),
('080703', 'Chamaca', '0807', true, NOW()),
('080704', 'Colquemarca', '0807', true, NOW()),
('080705', 'Livitaca', '0807', true, NOW()),
('080706', 'Llusco', '0807', true, NOW()),
('080707', 'Quiñota', '0807', true, NOW()),
('080708', 'Velille', '0807', true, NOW());

-- Cusco / Espinar (0808)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('080801', 'Espinar', '0808', true, NOW()),
('080802', 'Condoroma', '0808', true, NOW()),
('080803', 'Coporaque', '0808', true, NOW()),
('080804', 'Ocoruro', '0808', true, NOW()),
('080805', 'Pallpata', '0808', true, NOW()),
('080806', 'Pichigua', '0808', true, NOW()),
('080807', 'Suyckutambo', '0808', true, NOW()),
('080808', 'Alto Pichigua', '0808', true, NOW());

-- Cusco / La Convención (0809)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('080901', 'Santa Ana', '0809', true, NOW()),
('080902', 'Echarate', '0809', true, NOW()),
('080903', 'Huayopata', '0809', true, NOW()),
('080904', 'Maranura', '0809', true, NOW()),
('080905', 'Ocobamba', '0809', true, NOW()),
('080906', 'Quellouno', '0809', true, NOW()),
('080907', 'Kimbiri', '0809', true, NOW()),
('080908', 'Santa Teresa', '0809', true, NOW()),
('080909', 'Vilcabamba', '0809', true, NOW()),
('080910', 'Pichari', '0809', true, NOW()),
('080911', 'Inkawasi', '0809', true, NOW()),
('080912', 'Villa Virgen', '0809', true, NOW()),
('080913', 'Villa Kintiarina', '0809', true, NOW()),
('080914', 'Megantoni', '0809', true, NOW());

-- Cusco / Paruro (0810)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('081001', 'Paruro', '0810', true, NOW()),
('081002', 'Accha', '0810', true, NOW()),
('081003', 'Ccapi', '0810', true, NOW()),
('081004', 'Colcha', '0810', true, NOW()),
('081005', 'Huanoquite', '0810', true, NOW()),
('081006', 'Omacha', '0810', true, NOW()),
('081007', 'Paccaritambo', '0810', true, NOW()),
('081008', 'Pillpinto', '0810', true, NOW()),
('081009', 'Yaurisque', '0810', true, NOW());

-- Cusco / Paucartambo (0811)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('081101', 'Paucartambo', '0811', true, NOW()),
('081102', 'Caicay', '0811', true, NOW()),
('081103', 'Challabamba', '0811', true, NOW()),
('081104', 'Colquepata', '0811', true, NOW()),
('081105', 'Huancarani', '0811', true, NOW()),
('081106', 'Kosñipata', '0811', true, NOW());

-- Cusco / Quispicanchi (0812)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('081201', 'Urcos', '0812', true, NOW()),
('081202', 'Andahuaylillas', '0812', true, NOW()),
('081203', 'Camanti', '0812', true, NOW()),
('081204', 'Ccarhuayo', '0812', true, NOW()),
('081205', 'Ccatca', '0812', true, NOW()),
('081206', 'Cusipata', '0812', true, NOW()),
('081207', 'Huaro', '0812', true, NOW()),
('081208', 'Lucre', '0812', true, NOW()),
('081209', 'Marcapata', '0812', true, NOW()),
('081210', 'Ocongate', '0812', true, NOW()),
('081211', 'Oropesa', '0812', true, NOW()),
('081212', 'Quiquijana', '0812', true, NOW());

-- Cusco / Urubamba (0813)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('081301', 'Urubamba', '0813', true, NOW()),
('081302', 'Chinchero', '0813', true, NOW()),
('081303', 'Huayllabamba', '0813', true, NOW()),
('081304', 'Machupicchu', '0813', true, NOW()),
('081305', 'Maras', '0813', true, NOW()),
('081306', 'Ollantaytambo', '0813', true, NOW()),
('081307', 'Yucay', '0813', true, NOW());

-- Huancavelica / Huancavelica (0901)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('090101', 'Huancavelica', '0901', true, NOW()),
('090102', 'Acobambilla', '0901', true, NOW()),
('090103', 'Acoria', '0901', true, NOW()),
('090104', 'Conayca', '0901', true, NOW()),
('090105', 'Cuenca', '0901', true, NOW()),
('090106', 'Huachocolpa', '0901', true, NOW()),
('090107', 'Huayllahuara', '0901', true, NOW()),
('090108', 'Izcuchaca', '0901', true, NOW()),
('090109', 'Laria', '0901', true, NOW()),
('090110', 'Manta', '0901', true, NOW()),
('090111', 'Mariscal Cáceres', '0901', true, NOW()),
('090112', 'Moya', '0901', true, NOW()),
('090113', 'Nuevo Occoro', '0901', true, NOW()),
('090114', 'Palca', '0901', true, NOW()),
('090115', 'Pilchaca', '0901', true, NOW()),
('090116', 'Vilca', '0901', true, NOW()),
('090117', 'Yauli', '0901', true, NOW()),
('090118', 'Ascensión', '0901', true, NOW()),
('090119', 'Huando', '0901', true, NOW());

-- Huancavelica / Acobamba (0902)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('090201', 'Acobamba', '0902', true, NOW()),
('090202', 'Andabamba', '0902', true, NOW()),
('090203', 'Anta', '0902', true, NOW()),
('090204', 'Caja', '0902', true, NOW()),
('090205', 'Marcas', '0902', true, NOW()),
('090206', 'Paucará', '0902', true, NOW()),
('090207', 'Pomacocha', '0902', true, NOW()),
('090208', 'Rosario', '0902', true, NOW());

-- Huancavelica / Angaraes (0903)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('090301', 'Lircay', '0903', true, NOW()),
('090302', 'Antaparco', '0903', true, NOW()),
('090303', 'Callanmarca', '0903', true, NOW()),
('090304', 'Ccochaccasa', '0903', true, NOW()),
('090305', 'Chincho', '0903', true, NOW()),
('090306', 'Congalla', '0903', true, NOW()),
('090307', 'Huanca Huanca', '0903', true, NOW()),
('090308', 'Huayllay Grande', '0903', true, NOW()),
('090309', 'Julcamarca', '0903', true, NOW()),
('090310', 'San Antonio de Antaparco', '0903', true, NOW()),
('090311', 'Santo Tomás de Pata', '0903', true, NOW()),
('090312', 'Secclla', '0903', true, NOW());

-- Huancavelica / Castrovirreyna (0904)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('090401', 'Castrovirreyna', '0904', true, NOW()),
('090402', 'Arma', '0904', true, NOW()),
('090403', 'Aurahua', '0904', true, NOW()),
('090404', 'Capillas', '0904', true, NOW()),
('090405', 'Chupamarca', '0904', true, NOW()),
('090406', 'Cocas', '0904', true, NOW()),
('090407', 'Huachos', '0904', true, NOW()),
('090408', 'Huamatambo', '0904', true, NOW()),
('090409', 'Mollepampa', '0904', true, NOW()),
('090410', 'San Juan', '0904', true, NOW()),
('090411', 'Santa Ana', '0904', true, NOW()),
('090412', 'Tantara', '0904', true, NOW()),
('090413', 'Ticrapo', '0904', true, NOW());

-- Huancavelica / Churcampa (0905)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('090501', 'Churcampa', '0905', true, NOW()),
('090502', 'Anco', '0905', true, NOW()),
('090503', 'Chinchihuasi', '0905', true, NOW()),
('090504', 'El Carmen', '0905', true, NOW()),
('090505', 'La Merced', '0905', true, NOW()),
('090506', 'Locroja', '0905', true, NOW()),
('090507', 'Paucarbamba', '0905', true, NOW()),
('090508', 'San Miguel de Mayocc', '0905', true, NOW()),
('090509', 'San Pedro de Coris', '0905', true, NOW()),
('090510', 'Pachamarca', '0905', true, NOW()),
('090511', 'Cosme', '0905', true, NOW());

-- Huancavelica / Huaytará (0906)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('090601', 'Huaytará', '0906', true, NOW()),
('090602', 'Ayavi', '0906', true, NOW()),
('090603', 'Córdova', '0906', true, NOW()),
('090604', 'Huayacundo Arma', '0906', true, NOW()),
('090605', 'Laramarca', '0906', true, NOW()),
('090606', 'Ocoyo', '0906', true, NOW()),
('090607', 'Pilpichaca', '0906', true, NOW()),
('090608', 'Querco', '0906', true, NOW()),
('090609', 'Quito Arma', '0906', true, NOW()),
('090610', 'San Antonio de Cusicancha', '0906', true, NOW()),
('090611', 'San Francisco de Sangayaico', '0906', true, NOW()),
('090612', 'San Isidro de Huirpacancha', '0906', true, NOW()),
('090613', 'Santo Domingo de Capillas', '0906', true, NOW()),
('090614', 'Tambo', '0906', true, NOW());

-- Huancavelica / Tayacaja (0907)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('090701', 'Pampas', '0907', true, NOW()),
('090702', 'Acostambo', '0907', true, NOW()),
('090703', 'Acraquia', '0907', true, NOW()),
('090704', 'Ahuaycha', '0907', true, NOW()),
('090705', 'Colcabamba', '0907', true, NOW()),
('090706', 'Daniel Hernández', '0907', true, NOW()),
('090707', 'Huachocolpa', '0907', true, NOW()),
('090708', 'Huaribamba', '0907', true, NOW()),
('090709', 'Ñahuimpuquio', '0907', true, NOW()),
('090710', 'Pazos', '0907', true, NOW()),
('090711', 'Quishuar', '0907', true, NOW()),
('090712', 'Salcabamba', '0907', true, NOW()),
('090713', 'Salcahuasi', '0907', true, NOW()),
('090714', 'San Marcos de Rocchac', '0907', true, NOW()),
('090715', 'Surcubamba', '0907', true, NOW()),
('090716', 'Tintay Puncu', '0907', true, NOW()),
('090717', 'Quichuas', '0907', true, NOW()),
('090718', 'Santiago de Tucuma', '0907', true, NOW());

-- Huánuco / Huánuco (1001)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('100101', 'Huánuco', '1001', true, NOW()),
('100102', 'Amarilis', '1001', true, NOW()),
('100103', 'Chinchao', '1001', true, NOW()),
('100104', 'Churubamba', '1001', true, NOW()),
('100105', 'Margos', '1001', true, NOW()),
('100106', 'Quisqui', '1001', true, NOW()),
('100107', 'San Francisco de Cayrán', '1001', true, NOW()),
('100108', 'San Pedro de Chaulán', '1001', true, NOW()),
('100109', 'Santa María del Valle', '1001', true, NOW()),
('100110', 'Yarumayo', '1001', true, NOW()),
('100111', 'Pillco Marca', '1001', true, NOW()),
('100112', 'Yacus', '1001', true, NOW());

-- Huánuco / Ambo (1002)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('100201', 'Ambo', '1002', true, NOW()),
('100202', 'Cayna', '1002', true, NOW()),
('100203', 'Colpas', '1002', true, NOW()),
('100204', 'Conchamarca', '1002', true, NOW()),
('100205', 'Huácar', '1002', true, NOW()),
('100206', 'San Francisco', '1002', true, NOW()),
('100207', 'San Rafael', '1002', true, NOW()),
('100208', 'Tomay Kichwa', '1002', true, NOW());

-- Huánuco / Dos de Mayo (1003)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('100301', 'La Unión', '1003', true, NOW()),
('100302', 'Chuquis', '1003', true, NOW()),
('100303', 'Marías', '1003', true, NOW()),
('100304', 'Pachas', '1003', true, NOW()),
('100305', 'Quivilla', '1003', true, NOW()),
('100306', 'Ripán', '1003', true, NOW()),
('100307', 'Shunqui', '1003', true, NOW()),
('100308', 'Sillapata', '1003', true, NOW()),
('100309', 'Yanas', '1003', true, NOW());

-- Huánuco / Huacaybamba (1004)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('100401', 'Huacaybamba', '1004', true, NOW()),
('100402', 'Canchabamba', '1004', true, NOW()),
('100403', 'Cochabamba', '1004', true, NOW()),
('100404', 'Pinra', '1004', true, NOW());

-- Huánuco / Huamalíes (1005)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('100501', 'Llata', '1005', true, NOW()),
('100502', 'Arancay', '1005', true, NOW()),
('100503', 'Chavín de Pariarca', '1005', true, NOW()),
('100504', 'Jacas Grande', '1005', true, NOW()),
('100505', 'Jircan', '1005', true, NOW()),
('100506', 'Miraflores', '1005', true, NOW()),
('100507', 'Monzón', '1005', true, NOW()),
('100508', 'Punchao', '1005', true, NOW()),
('100509', 'Puños', '1005', true, NOW()),
('100510', 'Singa', '1005', true, NOW()),
('100511', 'Tantamayo', '1005', true, NOW());

-- Huánuco / Leoncio Prado (1006)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('100601', 'Rupa Rupa', '1006', true, NOW()),
('100602', 'Daniel Alomía Robles', '1006', true, NOW()),
('100603', 'Hermilio Valdizán', '1006', true, NOW()),
('100604', 'José Crespo y Castillo', '1006', true, NOW()),
('100605', 'Luyando', '1006', true, NOW()),
('100606', 'Mariano Dámaso Beraún', '1006', true, NOW()),
('100607', 'Pucayacu', '1006', true, NOW()),
('100608', 'Castillo Grande', '1006', true, NOW()),
('100609', 'Pueblo Nuevo', '1006', true, NOW()),
('100610', 'Santo Domingo de Anda', '1006', true, NOW());

-- Huánuco / Marañón (1007)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('100701', 'Huacrachuco', '1007', true, NOW()),
('100702', 'Cholón', '1007', true, NOW()),
('100703', 'San Buenaventura', '1007', true, NOW()),
('100704', 'La Morada', '1007', true, NOW()),
('100705', 'Santa Rosa de Alto Yanajanca', '1007', true, NOW());

-- Huánuco / Pachitea (1008)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('100801', 'Panao', '1008', true, NOW()),
('100802', 'Chaglla', '1008', true, NOW()),
('100803', 'Molino', '1008', true, NOW()),
('100804', 'Umari', '1008', true, NOW());

-- Huánuco / Puerto Inca (1009)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('100901', 'Puerto Inca', '1009', true, NOW()),
('100902', 'Codo del Pozuzo', '1009', true, NOW()),
('100903', 'Honoria', '1009', true, NOW()),
('100904', 'Tournavista', '1009', true, NOW()),
('100905', 'Yuyapichis', '1009', true, NOW());

-- Huánuco / Lauricocha (1010)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('101001', 'Jesús', '1010', true, NOW()),
('101002', 'Baños', '1010', true, NOW()),
('101003', 'Jivia', '1010', true, NOW()),
('101004', 'Queropalca', '1010', true, NOW()),
('101005', 'Rondos', '1010', true, NOW()),
('101006', 'San Francisco de Asís', '1010', true, NOW()),
('101007', 'San Miguel de Cauri', '1010', true, NOW());

-- Huánuco / Yarowilca (1011)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('101101', 'Chavinillo', '1011', true, NOW()),
('101102', 'Cahuac', '1011', true, NOW()),
('101103', 'Chacabamba', '1011', true, NOW()),
('101104', 'Aparicio Pomares', '1011', true, NOW()),
('101105', 'Jacas Chico', '1011', true, NOW()),
('101106', 'Obas', '1011', true, NOW()),
('101107', 'Pampamarca', '1011', true, NOW()),
('101108', 'Choras', '1011', true, NOW());

-- Ica / Ica (1101)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('110101', 'Ica', '1101', true, NOW()),
('110102', 'La Tinguiña', '1101', true, NOW()),
('110103', 'Los Aquijes', '1101', true, NOW()),
('110104', 'Ocucaje', '1101', true, NOW()),
('110105', 'Pachacutec', '1101', true, NOW()),
('110106', 'Parcona', '1101', true, NOW()),
('110107', 'Pueblo Nuevo', '1101', true, NOW()),
('110108', 'Salas', '1101', true, NOW()),
('110109', 'San José de los Molinos', '1101', true, NOW()),
('110110', 'San Juan Bautista', '1101', true, NOW()),
('110111', 'Santiago', '1101', true, NOW()),
('110112', 'Subtanjalla', '1101', true, NOW()),
('110113', 'Tate', '1101', true, NOW()),
('110114', 'Yauca del Rosario', '1101', true, NOW());

-- Ica / Chincha (1102)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('110201', 'Chincha Alta', '1102', true, NOW()),
('110202', 'Alto Larán', '1102', true, NOW()),
('110203', 'Chavín', '1102', true, NOW()),
('110204', 'Chincha Baja', '1102', true, NOW()),
('110205', 'El Carmen', '1102', true, NOW()),
('110206', 'Grocio Prado', '1102', true, NOW()),
('110207', 'Pueblo Nuevo', '1102', true, NOW()),
('110208', 'San Juan de Yanac', '1102', true, NOW()),
('110209', 'San Pedro de Huacarpana', '1102', true, NOW()),
('110210', 'Sunampe', '1102', true, NOW()),
('110211', 'Tambo de Mora', '1102', true, NOW());

-- Ica / Nazca (1103)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('110301', 'Nazca', '1103', true, NOW()),
('110302', 'Changuillo', '1103', true, NOW()),
('110303', 'El Ingenio', '1103', true, NOW()),
('110304', 'Marcona', '1103', true, NOW()),
('110305', 'Vista Alegre', '1103', true, NOW());

-- Ica / Palpa (1104)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('110401', 'Palpa', '1104', true, NOW()),
('110402', 'Llipata', '1104', true, NOW()),
('110403', 'Río Grande', '1104', true, NOW()),
('110404', 'Santa Cruz', '1104', true, NOW()),
('110405', 'Tibillo', '1104', true, NOW());

-- Ica / Pisco (1105)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('110501', 'Pisco', '1105', true, NOW()),
('110502', 'Huancano', '1105', true, NOW()),
('110503', 'Humay', '1105', true, NOW()),
('110504', 'Independencia', '1105', true, NOW()),
('110505', 'Paracas', '1105', true, NOW()),
('110506', 'San Andrés', '1105', true, NOW()),
('110507', 'San Clemente', '1105', true, NOW()),
('110508', 'Tupac Amaru Inca', '1105', true, NOW());

-- Junín / Huancayo (1201)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('120101', 'Huancayo', '1201', true, NOW()),
('120102', 'Carhuacallanga', '1201', true, NOW()),
('120103', 'Chacapampa', '1201', true, NOW()),
('120104', 'Chicche', '1201', true, NOW()),
('120105', 'Chilca', '1201', true, NOW()),
('120106', 'Chongos Alto', '1201', true, NOW()),
('120107', 'Chupuro', '1201', true, NOW()),
('120108', 'Colca', '1201', true, NOW()),
('120109', 'Cullhuas', '1201', true, NOW()),
('120110', 'El Tambo', '1201', true, NOW()),
('120111', 'Huacrapuquio', '1201', true, NOW()),
('120112', 'Hualhuas', '1201', true, NOW()),
('120113', 'Huancán', '1201', true, NOW()),
('120114', 'Huasicancha', '1201', true, NOW()),
('120115', 'Huayucachi', '1201', true, NOW()),
('120116', 'Ingenio', '1201', true, NOW()),
('120117', 'Pariahuanca', '1201', true, NOW()),
('120118', 'Pilcomayo', '1201', true, NOW()),
('120119', 'Pucará', '1201', true, NOW()),
('120120', 'Quichuay', '1201', true, NOW()),
('120121', 'Quilcas', '1201', true, NOW()),
('120122', 'San Agustín de Cajas', '1201', true, NOW()),
('120123', 'San Jerónimo de Tunán', '1201', true, NOW()),
('120124', 'San Pedro de Saño', '1201', true, NOW()),
('120125', 'Santo Domingo de Acobamba', '1201', true, NOW()),
('120126', 'Sapallanga', '1201', true, NOW()),
('120127', 'Sicaya', '1201', true, NOW()),
('120128', 'Viques', '1201', true, NOW());

-- Junín / Concepción (1202)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('120201', 'Concepción', '1202', true, NOW()),
('120202', 'Aco', '1202', true, NOW()),
('120203', 'Andamarca', '1202', true, NOW()),
('120204', 'Chambara', '1202', true, NOW()),
('120205', 'Cochas', '1202', true, NOW()),
('120206', 'Comas', '1202', true, NOW()),
('120207', 'Heroínas Toledo', '1202', true, NOW()),
('120208', 'Manzanares', '1202', true, NOW()),
('120209', 'Mariscal Castilla', '1202', true, NOW()),
('120210', 'Matahuasi', '1202', true, NOW()),
('120211', 'Mito', '1202', true, NOW()),
('120212', 'Nueve de Julio', '1202', true, NOW()),
('120213', 'Orcotuna', '1202', true, NOW()),
('120214', 'San José de Quero', '1202', true, NOW()),
('120215', 'Santa Rosa de Ocopa', '1202', true, NOW());

-- Junín / Chanchamayo (1203)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('120301', 'Chanchamayo', '1203', true, NOW()),
('120302', 'Perené', '1203', true, NOW()),
('120303', 'Pichanaqui', '1203', true, NOW()),
('120304', 'San Luis de Shuaro', '1203', true, NOW()),
('120305', 'San Ramón', '1203', true, NOW()),
('120306', 'Vitoc', '1203', true, NOW());

-- Junín / Jauja (1204)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('120401', 'Jauja', '1204', true, NOW()),
('120402', 'Acolla', '1204', true, NOW()),
('120403', 'Apata', '1204', true, NOW()),
('120404', 'Ataura', '1204', true, NOW()),
('120405', 'Canchayllo', '1204', true, NOW()),
('120406', 'Curicaca', '1204', true, NOW()),
('120407', 'El Mantaro', '1204', true, NOW()),
('120408', 'Huamalí', '1204', true, NOW()),
('120409', 'Huaripampa', '1204', true, NOW()),
('120410', 'Huertas', '1204', true, NOW()),
('120411', 'Janjaillo', '1204', true, NOW()),
('120412', 'Julcán', '1204', true, NOW()),
('120413', 'Leonor Ordóñez', '1204', true, NOW()),
('120414', 'Llocllapampa', '1204', true, NOW()),
('120415', 'Marco', '1204', true, NOW()),
('120416', 'Masma', '1204', true, NOW()),
('120417', 'Masma Chicche', '1204', true, NOW()),
('120418', 'Molinos', '1204', true, NOW()),
('120419', 'Monobamba', '1204', true, NOW()),
('120420', 'Muqui', '1204', true, NOW()),
('120421', 'Muquiyauyo', '1204', true, NOW()),
('120422', 'Paca', '1204', true, NOW()),
('120423', 'Paccha', '1204', true, NOW()),
('120424', 'Pancán', '1204', true, NOW()),
('120425', 'Parco', '1204', true, NOW()),
('120426', 'Pomacancha', '1204', true, NOW()),
('120427', 'Ricrán', '1204', true, NOW()),
('120428', 'San Lorenzo', '1204', true, NOW()),
('120429', 'San Pedro de Chunán', '1204', true, NOW()),
('120430', 'Sausa', '1204', true, NOW()),
('120431', 'Sincos', '1204', true, NOW()),
('120432', 'Tunanán Marca', '1204', true, NOW()),
('120433', 'Yauli', '1204', true, NOW()),
('120434', 'Yauyos', '1204', true, NOW());

-- Junín / Junín (1205)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('120501', 'Junín', '1205', true, NOW()),
('120502', 'Carhuamayo', '1205', true, NOW()),
('120503', 'Ondores', '1205', true, NOW()),
('120504', 'Ulcumayo', '1205', true, NOW());

-- Junín / Satipo (1206)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('120601', 'Satipo', '1206', true, NOW()),
('120602', 'Coviriali', '1206', true, NOW()),
('120603', 'Llaylla', '1206', true, NOW()),
('120604', 'Mazamari', '1206', true, NOW()),
('120605', 'Pampa Hermosa', '1206', true, NOW()),
('120606', 'Pantay', '1206', true, NOW()),
('120607', 'Río Negro', '1206', true, NOW()),
('120608', 'Río Tambo', '1206', true, NOW()),
('120609', 'Vizcatán del Ene', '1206', true, NOW());

-- Junín / Tarma (1207)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('120701', 'Tarma', '1207', true, NOW()),
('120702', 'Acobamba', '1207', true, NOW()),
('120703', 'Huaricolca', '1207', true, NOW()),
('120704', 'Huasahuasi', '1207', true, NOW()),
('120705', 'La Unión', '1207', true, NOW()),
('120706', 'Palca', '1207', true, NOW()),
('120707', 'Palcamayo', '1207', true, NOW()),
('120708', 'San Pedro de Cajas', '1207', true, NOW()),
('120709', 'Tapo', '1207', true, NOW());

-- Junín / Yauli (1208)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('120801', 'La Oroya', '1208', true, NOW()),
('120802', 'Chacapalpa', '1208', true, NOW()),
('120803', 'Huay Huay', '1208', true, NOW()),
('120804', 'Marcapomacocha', '1208', true, NOW()),
('120805', 'Morococha', '1208', true, NOW()),
('120806', 'Paccha', '1208', true, NOW()),
('120807', 'Santa Bárbara de Carhuacayán', '1208', true, NOW()),
('120808', 'Suitucancha', '1208', true, NOW()),
('120809', 'Yauli', '1208', true, NOW());

-- Junín / Chupaca (1209)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('120901', 'Chupaca', '1209', true, NOW()),
('120902', 'Áhuac', '1209', true, NOW()),
('120903', 'Chongos Bajo', '1209', true, NOW()),
('120904', 'Huachac', '1209', true, NOW()),
('120905', 'Huamancaca Chico', '1209', true, NOW()),
('120906', 'San Juan de Jarpa', '1209', true, NOW()),
('120907', 'San Juan de Yscos', '1209', true, NOW()),
('120908', 'Tres de Diciembre', '1209', true, NOW()),
('120909', 'Yanacancha', '1209', true, NOW());

-- La Libertad / Trujillo (1301)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('130101', 'Trujillo', '1301', true, NOW()),
('130102', 'El Porvenir', '1301', true, NOW()),
('130103', 'Florencia de Mora', '1301', true, NOW()),
('130104', 'Huanchaco', '1301', true, NOW()),
('130105', 'La Esperanza', '1301', true, NOW()),
('130106', 'Laredo', '1301', true, NOW()),
('130107', 'Moche', '1301', true, NOW()),
('130108', 'Poroto', '1301', true, NOW()),
('130109', 'Salaverry', '1301', true, NOW()),
('130110', 'Simbal', '1301', true, NOW()),
('130111', 'Víctor Larco Herrera', '1301', true, NOW());

-- La Libertad / Ascope (1302)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('130201', 'Ascope', '1302', true, NOW()),
('130202', 'Chicama', '1302', true, NOW()),
('130203', 'Chocope', '1302', true, NOW()),
('130204', 'Magdalena de Cao', '1302', true, NOW()),
('130205', 'Paiján', '1302', true, NOW()),
('130206', 'Rázuri', '1302', true, NOW()),
('130207', 'Santiago de Cao', '1302', true, NOW()),
('130208', 'Casa Grande', '1302', true, NOW());

-- La Libertad / Bolívar (1303)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('130301', 'Bolívar', '1303', true, NOW()),
('130302', 'Bambamarca', '1303', true, NOW()),
('130303', 'Condormarca', '1303', true, NOW()),
('130304', 'Longotea', '1303', true, NOW()),
('130305', 'Uchumarca', '1303', true, NOW()),
('130306', 'Ucuncha', '1303', true, NOW());

-- La Libertad / Chepén (1304)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('130401', 'Chepén', '1304', true, NOW()),
('130402', 'Pacanga', '1304', true, NOW()),
('130403', 'Pueblo Nuevo', '1304', true, NOW());

-- La Libertad / Julcán (1305)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('130501', 'Julcán', '1305', true, NOW()),
('130502', 'Calamarca', '1305', true, NOW()),
('130503', 'Carabamba', '1305', true, NOW()),
('130504', 'Huaso', '1305', true, NOW());

-- La Libertad / Otuzco (1306)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('130601', 'Otuzco', '1306', true, NOW()),
('130602', 'Agallpampa', '1306', true, NOW()),
('130603', 'Charat', '1306', true, NOW()),
('130604', 'Huaranchal', '1306', true, NOW()),
('130605', 'La Cuesta', '1306', true, NOW()),
('130606', 'Mache', '1306', true, NOW()),
('130607', 'Paranday', '1306', true, NOW()),
('130608', 'Salpo', '1306', true, NOW()),
('130609', 'Sinsicap', '1306', true, NOW()),
('130610', 'Usquil', '1306', true, NOW());

-- La Libertad / Pacasmayo (1307)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('130701', 'San Pedro de Lloc', '1307', true, NOW()),
('130702', 'Guadalupe', '1307', true, NOW()),
('130703', 'Jequetepeque', '1307', true, NOW()),
('130704', 'Pacasmayo', '1307', true, NOW()),
('130705', 'San José', '1307', true, NOW());

-- La Libertad / Pataz (1308)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('130801', 'Tayabamba', '1308', true, NOW()),
('130802', 'Buldibuyo', '1308', true, NOW()),
('130803', 'Chillia', '1308', true, NOW()),
('130804', 'Huancaspata', '1308', true, NOW()),
('130805', 'Huaylillas', '1308', true, NOW()),
('130806', 'Huayo', '1308', true, NOW()),
('130807', 'Ongón', '1308', true, NOW()),
('130808', 'Parcoy', '1308', true, NOW()),
('130809', 'Pataz', '1308', true, NOW()),
('130810', 'Pías', '1308', true, NOW()),
('130811', 'Santiago de Challas', '1308', true, NOW()),
('130812', 'Taurija', '1308', true, NOW()),
('130813', 'Urpay', '1308', true, NOW());

-- La Libertad / Sánchez Carrión (1309)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('130901', 'Huamachuco', '1309', true, NOW()),
('130902', 'Chugay', '1309', true, NOW()),
('130903', 'Cochorco', '1309', true, NOW()),
('130904', 'Curgos', '1309', true, NOW()),
('130905', 'Marcabal', '1309', true, NOW()),
('130906', 'Sanagorán', '1309', true, NOW()),
('130907', 'Sarin', '1309', true, NOW()),
('130908', 'Sartimbamba', '1309', true, NOW());

-- La Libertad / Santiago de Chuco (1310)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('131001', 'Santiago de Chuco', '1310', true, NOW()),
('131002', 'Angasmarca', '1310', true, NOW()),
('131003', 'Cachicadán', '1310', true, NOW()),
('131004', 'Mollebamba', '1310', true, NOW()),
('131005', 'Mollepata', '1310', true, NOW()),
('131006', 'Quiruvilca', '1310', true, NOW()),
('131007', 'Santa Cruz de Chuca', '1310', true, NOW()),
('131008', 'Sitabamba', '1310', true, NOW());

-- La Libertad / Gran Chimú (1311)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('131101', 'Cascas', '1311', true, NOW()),
('131102', 'Lucma', '1311', true, NOW()),
('131103', 'Marmot', '1311', true, NOW()),
('131104', 'Sayapullo', '1311', true, NOW());

-- La Libertad / Virú (1312)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('131201', 'Virú', '1312', true, NOW()),
('131202', 'Chao', '1312', true, NOW()),
('131203', 'Guadalupito', '1312', true, NOW());

-- Lambayeque / Chiclayo (1401)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('140101', 'Chiclayo', '1401', true, NOW()),
('140102', 'Chongoyape', '1401', true, NOW()),
('140103', 'Eten', '1401', true, NOW()),
('140104', 'Eten Puerto', '1401', true, NOW()),
('140105', 'José Leonardo Ortiz', '1401', true, NOW()),
('140106', 'La Victoria', '1401', true, NOW()),
('140107', 'Lagunas', '1401', true, NOW()),
('140108', 'Monsefú', '1401', true, NOW()),
('140109', 'Nueva Arica', '1401', true, NOW()),
('140110', 'Oyotún', '1401', true, NOW()),
('140111', 'Picsi', '1401', true, NOW()),
('140112', 'Pimentel', '1401', true, NOW()),
('140113', 'Reque', '1401', true, NOW()),
('140114', 'Santa Rosa', '1401', true, NOW()),
('140115', 'Saña', '1401', true, NOW()),
('140116', 'Cayaltí', '1401', true, NOW()),
('140117', 'Patapo', '1401', true, NOW()),
('140118', 'Pomalca', '1401', true, NOW()),
('140119', 'Pucalá', '1401', true, NOW()),
('140120', 'Tumán', '1401', true, NOW());

-- Lambayeque / Ferreñafe (1402)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('140201', 'Ferreñafe', '1402', true, NOW()),
('140202', 'Cañaris', '1402', true, NOW()),
('140203', 'Incahuasi', '1402', true, NOW()),
('140204', 'Manuel Antonio Mesones Muro', '1402', true, NOW()),
('140205', 'Pitipo', '1402', true, NOW()),
('140206', 'Pueblo Nuevo', '1402', true, NOW());

-- Lambayeque / Lambayeque (1403)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('140301', 'Lambayeque', '1403', true, NOW()),
('140302', 'Chóchope', '1403', true, NOW()),
('140303', 'Íllimo', '1403', true, NOW()),
('140304', 'Jayanca', '1403', true, NOW()),
('140305', 'Mochumí', '1403', true, NOW()),
('140306', 'Mórrope', '1403', true, NOW()),
('140307', 'Motupe', '1403', true, NOW()),
('140308', 'Olmos', '1403', true, NOW()),
('140309', 'Pacora', '1403', true, NOW()),
('140310', 'Salas', '1403', true, NOW()),
('140311', 'San José', '1403', true, NOW()),
('140312', 'Túcume', '1403', true, NOW());

-- Lima / Lima (1501) — 43 districts
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('150101', 'Lima', '1501', true, NOW()),
('150102', 'Ancón', '1501', true, NOW()),
('150103', 'Ate', '1501', true, NOW()),
('150104', 'Barranco', '1501', true, NOW()),
('150105', 'Breña', '1501', true, NOW()),
('150106', 'Carabayllo', '1501', true, NOW()),
('150107', 'Comas', '1501', true, NOW()),
('150108', 'Chaclacayo', '1501', true, NOW()),
('150109', 'Chorrillos', '1501', true, NOW()),
('150110', 'El Agustino', '1501', true, NOW()),
('150111', 'Jesús María', '1501', true, NOW()),
('150112', 'La Molina', '1501', true, NOW()),
('150113', 'La Victoria', '1501', true, NOW()),
('150114', 'Lince', '1501', true, NOW()),
('150115', 'Los Olivos', '1501', true, NOW()),
('150116', 'Lurigancho', '1501', true, NOW()),
('150117', 'Lurín', '1501', true, NOW()),
('150118', 'Magdalena del Mar', '1501', true, NOW()),
('150119', 'Miraflores', '1501', true, NOW()),
('150120', 'Pachacámac', '1501', true, NOW()),
('150121', 'Pucusana', '1501', true, NOW()),
('150122', 'Pueblo Libre', '1501', true, NOW()),
('150123', 'Puente Piedra', '1501', true, NOW()),
('150124', 'Punta Hermosa', '1501', true, NOW()),
('150125', 'Punta Negra', '1501', true, NOW()),
('150126', 'Rímac', '1501', true, NOW()),
('150127', 'San Bartolo', '1501', true, NOW()),
('150128', 'San Borja', '1501', true, NOW()),
('150129', 'San Isidro', '1501', true, NOW()),
('150130', 'San Juan de Lurigancho', '1501', true, NOW()),
('150131', 'San Juan de Miraflores', '1501', true, NOW()),
('150132', 'San Luis', '1501', true, NOW()),
('150133', 'San Martín de Porres', '1501', true, NOW()),
('150134', 'San Miguel', '1501', true, NOW()),
('150135', 'Santa Anita', '1501', true, NOW()),
('150136', 'Santa María del Mar', '1501', true, NOW()),
('150137', 'Santa Rosa', '1501', true, NOW()),
('150138', 'Santiago de Surco', '1501', true, NOW()),
('150139', 'Surquillo', '1501', true, NOW()),
('150140', 'Villa El Salvador', '1501', true, NOW()),
('150141', 'Villa María del Triunfo', '1501', true, NOW()),
('150142', 'San Pedro de Carabayllo', '1501', true, NOW()),
('150143', 'Cieneguilla', '1501', true, NOW());

-- Lima / Barranca (1502)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('150201', 'Barranca', '1502', true, NOW()),
('150202', 'Paramonga', '1502', true, NOW()),
('150203', 'Pativilca', '1502', true, NOW()),
('150204', 'Supe', '1502', true, NOW()),
('150205', 'Supe Puerto', '1502', true, NOW());

-- Lima / Cajatambo (1503)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('150301', 'Cajatambo', '1503', true, NOW()),
('150302', 'Copa', '1503', true, NOW()),
('150303', 'Gorgor', '1503', true, NOW()),
('150304', 'Huancapón', '1503', true, NOW()),
('150305', 'Manas', '1503', true, NOW());

-- Lima / Canta (1504)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('150401', 'Canta', '1504', true, NOW()),
('150402', 'Arahuay', '1504', true, NOW()),
('150403', 'Huamantanga', '1504', true, NOW()),
('150404', 'Huaros', '1504', true, NOW()),
('150405', 'Lachaqui', '1504', true, NOW()),
('150406', 'San Buenaventura', '1504', true, NOW()),
('150407', 'Santa Rosa de Quives', '1504', true, NOW());

-- Lima / Cañete (1505)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('150501', 'San Vicente de Cañete', '1505', true, NOW()),
('150502', 'Asia', '1505', true, NOW()),
('150503', 'Calango', '1505', true, NOW()),
('150504', 'Cerro Azul', '1505', true, NOW()),
('150505', 'Chilca', '1505', true, NOW()),
('150506', 'Coayllo', '1505', true, NOW()),
('150507', 'Imperial', '1505', true, NOW()),
('150508', 'Lunahuaná', '1505', true, NOW()),
('150509', 'Mala', '1505', true, NOW()),
('150510', 'Nuevo Imperial', '1505', true, NOW()),
('150511', 'Pacarán', '1505', true, NOW()),
('150512', 'Quilmaná', '1505', true, NOW()),
('150513', 'San Antonio', '1505', true, NOW()),
('150514', 'San Luis', '1505', true, NOW()),
('150515', 'Santa Cruz de Flores', '1505', true, NOW()),
('150516', 'Zúñiga', '1505', true, NOW());

-- Lima / Huaral (1506)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('150601', 'Huaral', '1506', true, NOW()),
('150602', 'Atavillos Alto', '1506', true, NOW()),
('150603', 'Atavillos Bajo', '1506', true, NOW()),
('150604', 'Aucallama', '1506', true, NOW()),
('150605', 'Chancay', '1506', true, NOW()),
('150606', 'Ihuarí', '1506', true, NOW()),
('150607', 'Lampian', '1506', true, NOW()),
('150608', 'Pacaraos', '1506', true, NOW()),
('150609', 'San Miguel de Acos', '1506', true, NOW()),
('150610', 'Santa Cruz de Andamarca', '1506', true, NOW()),
('150611', 'Sumbilca', '1506', true, NOW()),
('150612', 'Veintisiete de Noviembre', '1506', true, NOW());

-- Lima / Huarochirí (1507)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('150701', 'Matucana', '1507', true, NOW()),
('150702', 'Antioquía', '1507', true, NOW()),
('150703', 'Callahuanca', '1507', true, NOW()),
('150704', 'Carampoma', '1507', true, NOW()),
('150705', 'Cieneguilla', '1507', true, NOW()),
('150706', 'Cuenca', '1507', true, NOW()),
('150707', 'Chicla', '1507', true, NOW()),
('150708', 'Huachupampa', '1507', true, NOW()),
('150709', 'Huanza', '1507', true, NOW()),
('150710', 'Huarochirí', '1507', true, NOW()),
('150711', 'Lahuaytambo', '1507', true, NOW()),
('150712', 'Langa', '1507', true, NOW()),
('150713', 'Laraos', '1507', true, NOW()),
('150714', 'Mariatana', '1507', true, NOW()),
('150715', 'Ricardo Palma', '1507', true, NOW()),
('150716', 'San Andrés de Tupicocha', '1507', true, NOW()),
('150717', 'San Antonio', '1507', true, NOW()),
('150718', 'San Bartolomé', '1507', true, NOW()),
('150719', 'San Damian', '1507', true, NOW()),
('150720', 'San Juan de Iris', '1507', true, NOW()),
('150721', 'San Juan de Tantaranche', '1507', true, NOW()),
('150722', 'San Lorenzo de Quinti', '1507', true, NOW()),
('150723', 'San Mateo', '1507', true, NOW()),
('150724', 'San Mateo de Otao', '1507', true, NOW()),
('150725', 'San Pedro de Casta', '1507', true, NOW()),
('150726', 'San Pedro de Huancayre', '1507', true, NOW()),
('150727', 'Sangallaya', '1507', true, NOW()),
('150728', 'Santa Cruz de Cocachacra', '1507', true, NOW()),
('150729', 'Santa Eulalia', '1507', true, NOW()),
('150730', 'Santiago de Anchucaya', '1507', true, NOW()),
('150731', 'Santiago de Tuna', '1507', true, NOW()),
('150732', 'Santo Domingo de los Olleros', '1507', true, NOW()),
('150733', 'Surco', '1507', true, NOW());

-- Lima / Huaura (1508)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('150801', 'Huacho', '1508', true, NOW()),
('150802', 'Ámbar', '1508', true, NOW()),
('150803', 'Caleta de Carquín', '1508', true, NOW()),
('150804', 'Checras', '1508', true, NOW()),
('150805', 'Hualmay', '1508', true, NOW()),
('150806', 'Huaura', '1508', true, NOW()),
('150807', 'Leoncio Prado', '1508', true, NOW()),
('150808', 'Paccho', '1508', true, NOW()),
('150809', 'Santa Leonor', '1508', true, NOW()),
('150810', 'Santa María', '1508', true, NOW()),
('150811', 'Sayan', '1508', true, NOW()),
('150812', 'Vegueta', '1508', true, NOW());

-- Lima / Oyón (1509)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('150901', 'Oyón', '1509', true, NOW()),
('150902', 'Andajes', '1509', true, NOW()),
('150903', 'Caujul', '1509', true, NOW()),
('150904', 'Cochamarca', '1509', true, NOW()),
('150905', 'Naván', '1509', true, NOW()),
('150906', 'Pachangara', '1509', true, NOW());

-- Lima / Yauyos (1510)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('151001', 'Yauyos', '1510', true, NOW()),
('151002', 'Alis', '1510', true, NOW()),
('151003', 'Ayauca', '1510', true, NOW()),
('151004', 'Ayavirí', '1510', true, NOW()),
('151005', 'Azángaro', '1510', true, NOW()),
('151006', 'Cacra', '1510', true, NOW()),
('151007', 'Carania', '1510', true, NOW()),
('151008', 'Catahuasi', '1510', true, NOW()),
('151009', 'Chocos', '1510', true, NOW()),
('151010', 'Cochas', '1510', true, NOW()),
('151011', 'Colonia', '1510', true, NOW()),
('151012', 'Hongos', '1510', true, NOW()),
('151013', 'Huampara', '1510', true, NOW()),
('151014', 'Huancaya', '1510', true, NOW()),
('151015', 'Huangascar', '1510', true, NOW()),
('151016', 'Huantán', '1510', true, NOW()),
('151017', 'Huañec', '1510', true, NOW()),
('151018', 'Laraos', '1510', true, NOW()),
('151019', 'Lincha', '1510', true, NOW()),
('151020', 'Madeán', '1510', true, NOW()),
('151021', 'Miraflores', '1510', true, NOW()),
('151022', 'Omas', '1510', true, NOW()),
('151023', 'Putinza', '1510', true, NOW()),
('151024', 'Quinches', '1510', true, NOW()),
('151025', 'Quinocay', '1510', true, NOW()),
('151026', 'San Joaquín', '1510', true, NOW()),
('151027', 'San Pedro de Pilas', '1510', true, NOW()),
('151028', 'Tanta', '1510', true, NOW()),
('151029', 'Tauripampa', '1510', true, NOW()),
('151030', 'Tomas', '1510', true, NOW()),
('151031', 'Tupe', '1510', true, NOW()),
('151032', 'Viñac', '1510', true, NOW()),
('151033', 'Vitis', '1510', true, NOW());

-- Loreto / Maynas (1601)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('160101', 'Iquitos', '1601', true, NOW()),
('160102', 'Alto Nanay', '1601', true, NOW()),
('160103', 'Fernando Lores', '1601', true, NOW()),
('160104', 'Indiana', '1601', true, NOW()),
('160105', 'Las Amazonas', '1601', true, NOW()),
('160106', 'Mazán', '1601', true, NOW()),
('160107', 'Napo', '1601', true, NOW()),
('160108', 'Punchana', '1601', true, NOW()),
('160109', 'Putumayo', '1601', true, NOW()),
('160110', 'San Juan Bautista', '1601', true, NOW()),
('160111', 'Teniente Manuel Clavero', '1601', true, NOW()),
('160112', 'Torres Causana', '1601', true, NOW()),
('160113', 'Belen', '1601', true, NOW());

-- Loreto / Alto Amazonas (1602)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('160201', 'Yurimaguas', '1602', true, NOW()),
('160202', 'Balsapuerto', '1602', true, NOW()),
('160203', 'Barranca', '1602', true, NOW()),
('160204', 'Cahuapanas', '1602', true, NOW()),
('160205', 'Jesús de Rupa Rupa', '1602', true, NOW()),
('160206', 'Lagunas', '1602', true, NOW()),
('160207', 'Manseriche', '1602', true, NOW()),
('160208', 'Morona', '1602', true, NOW()),
('160209', 'Pastaza', '1602', true, NOW()),
('160210', 'Santa Cruz', '1602', true, NOW()),
('160211', 'Teniente César López Rojas', '1602', true, NOW());

-- Loreto / Loreto (1603)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('160301', 'Nauta', '1603', true, NOW()),
('160302', 'Parinari', '1603', true, NOW()),
('160303', 'Tigre', '1603', true, NOW()),
('160304', 'Trompeteros', '1603', true, NOW()),
('160305', 'Urarinas', '1603', true, NOW());

-- Loreto / Mariscal Ramón Castilla (1604)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('160401', 'Ramón Castilla', '1604', true, NOW()),
('160402', 'Pebas', '1604', true, NOW()),
('160403', 'Yavari', '1604', true, NOW()),
('160404', 'San Pablo', '1604', true, NOW());

-- Loreto / Requena (1605)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('160501', 'Requena', '1605', true, NOW()),
('160502', 'Alto Tapiche', '1605', true, NOW()),
('160503', 'Capelo', '1605', true, NOW()),
('160504', 'Emilio San Martín', '1605', true, NOW()),
('160505', 'Maquía', '1605', true, NOW()),
('160506', 'Puinahua', '1605', true, NOW()),
('160507', 'Saquena', '1605', true, NOW()),
('160508', 'Soplin', '1605', true, NOW()),
('160509', 'Tapiche', '1605', true, NOW()),
('160510', 'Yaquerana', '1605', true, NOW()),
('160511', 'Jenaro Herrera', '1605', true, NOW());

-- Loreto / Ucayali (1606)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('160601', 'Contamana', '1606', true, NOW()),
('160602', 'Inahuaya', '1606', true, NOW()),
('160603', 'Padre Márquez', '1606', true, NOW()),
('160604', 'Pampa Hermosa', '1606', true, NOW()),
('160605', 'Sarayacu', '1606', true, NOW()),
('160606', 'Vargas Guerra', '1606', true, NOW());

-- Loreto / Datem del Marañón (1607)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('160701', 'San Lorenzo', '1607', true, NOW()),
('160702', 'Barranca', '1607', true, NOW()),
('160703', 'Cahuapanas', '1607', true, NOW()),
('160704', 'Manseriche', '1607', true, NOW()),
('160705', 'Morona', '1607', true, NOW()),
('160706', 'Pastaza', '1607', true, NOW());

-- Loreto / Putumayo (1608)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('160801', 'Putumayo', '1608', true, NOW()),
('160802', 'Rosa Panduro', '1608', true, NOW()),
('160803', 'Teniente Manuel Clavero', '1608', true, NOW()),
('160804', 'Yaguas', '1608', true, NOW());

-- Madre de Dios / Tambopata (1701)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('170101', 'Tambopata', '1701', true, NOW()),
('170102', 'Inambari', '1701', true, NOW()),
('170103', 'Las Piedras', '1701', true, NOW()),
('170104', 'Laberinto', '1701', true, NOW());

-- Madre de Dios / Manu (1702)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('170201', 'Manu', '1702', true, NOW()),
('170202', 'Fitzcarrald', '1702', true, NOW()),
('170203', 'Madre de Dios', '1702', true, NOW()),
('170204', 'Huepetuhe', '1702', true, NOW());

-- Madre de Dios / Tahuamanu (1703)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('170301', 'Iñapari', '1703', true, NOW()),
('170302', 'Iberia', '1703', true, NOW()),
('170303', 'Tahuamanu', '1703', true, NOW());

-- Moquegua / Mariscal Nieto (1801)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('180101', 'Moquegua', '1801', true, NOW()),
('180102', 'Carumas', '1801', true, NOW()),
('180103', 'Cuchumbaya', '1801', true, NOW()),
('180104', 'San Cristóbal', '1801', true, NOW()),
('180105', 'Torata', '1801', true, NOW()),
('180106', 'Samegua', '1801', true, NOW());

-- Moquegua / General Sánchez Cerro (1802)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('180201', 'Omate', '1802', true, NOW()),
('180202', 'Chojata', '1802', true, NOW()),
('180203', 'Coalaque', '1802', true, NOW()),
('180204', 'Ichuña', '1802', true, NOW()),
('180205', 'La Capilla', '1802', true, NOW()),
('180206', 'Lloque', '1802', true, NOW()),
('180207', 'Matalaque', '1802', true, NOW()),
('180208', 'Puquina', '1802', true, NOW()),
('180209', 'Quinistaquillas', '1802', true, NOW()),
('180210', 'Ubinas', '1802', true, NOW()),
('180211', 'Yunga', '1802', true, NOW());

-- Moquegua / Ilo (1803)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('180301', 'Ilo', '1803', true, NOW()),
('180302', 'El Algarrobal', '1803', true, NOW()),
('180303', 'Pacocha', '1803', true, NOW());

-- Pasco / Pasco (1901)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('190101', 'Chaupimarca', '1901', true, NOW()),
('190102', 'Huachón', '1901', true, NOW()),
('190103', 'Huariaca', '1901', true, NOW()),
('190104', 'Huayllay', '1901', true, NOW()),
('190105', 'Ninacaca', '1901', true, NOW()),
('190106', 'Pallanchacra', '1901', true, NOW()),
('190107', 'Paucartambo', '1901', true, NOW()),
('190108', 'San Francisco de Asís de Yarusyacán', '1901', true, NOW()),
('190109', 'Simón Bolívar', '1901', true, NOW()),
('190110', 'Ticlacayán', '1901', true, NOW()),
('190111', 'Tinyahuarco', '1901', true, NOW()),
('190112', 'Vicco', '1901', true, NOW()),
('190113', 'Yanacancha', '1901', true, NOW());

-- Pasco / Daniel Alcides Carrión (1902)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('190201', 'Yanahuanca', '1902', true, NOW()),
('190202', 'Chacayán', '1902', true, NOW()),
('190203', 'Goyllarisquizga', '1902', true, NOW()),
('190204', 'Paucar', '1902', true, NOW()),
('190205', 'San Pedro de Pillao', '1902', true, NOW()),
('190206', 'Santa Ana de Tusi', '1902', true, NOW()),
('190207', 'Tapuc', '1902', true, NOW()),
('190208', 'Vilcabamba', '1902', true, NOW());

-- Pasco / Oxapampa (1903)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('190301', 'Oxapampa', '1903', true, NOW()),
('190302', 'Chontabamba', '1903', true, NOW()),
('190303', 'Huancabamba', '1903', true, NOW()),
('190304', 'Palcazú', '1903', true, NOW()),
('190305', 'Pozuzo', '1903', true, NOW()),
('190306', 'Puerto Bermúdez', '1903', true, NOW()),
('190307', 'Villa Rica', '1903', true, NOW()),
('190308', 'Constitución', '1903', true, NOW());

-- Piura / Piura (2001)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('200101', 'Piura', '2001', true, NOW()),
('200102', 'Castilla', '2001', true, NOW()),
('200103', 'Catacaos', '2001', true, NOW()),
('200104', 'Cura Mori', '2001', true, NOW()),
('200105', 'El Tallán', '2001', true, NOW()),
('200106', 'La Arena', '2001', true, NOW()),
('200107', 'La Unión', '2001', true, NOW()),
('200108', 'Las Lomas', '2001', true, NOW()),
('200109', 'Tambo Grande', '2001', true, NOW()),
('200110', 'Veintiséis de Octubre', '2001', true, NOW());

-- Piura / Ayabaca (2002)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('200201', 'Ayabaca', '2002', true, NOW()),
('200202', 'Frías', '2002', true, NOW()),
('200203', 'Jililí', '2002', true, NOW()),
('200204', 'Lagunas', '2002', true, NOW()),
('200205', 'Montero', '2002', true, NOW()),
('200206', 'Pacaipampa', '2002', true, NOW()),
('200207', 'Paimas', '2002', true, NOW()),
('200208', 'Sapillica', '2002', true, NOW()),
('200209', 'Sicchez', '2002', true, NOW()),
('200210', 'Suyo', '2002', true, NOW());

-- Piura / Huancabamba (2003)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('200301', 'Huancabamba', '2003', true, NOW()),
('200302', 'Canchaque', '2003', true, NOW()),
('200303', 'El Carmen de la Frontera', '2003', true, NOW()),
('200304', 'Huarmaca', '2003', true, NOW()),
('200305', 'Lalaquiz', '2003', true, NOW()),
('200306', 'San Miguel de El Faique', '2003', true, NOW()),
('200307', 'Sondor', '2003', true, NOW()),
('200308', 'Sondorillo', '2003', true, NOW());

-- Piura / Morropón (2004)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('200401', 'Chulucanas', '2004', true, NOW()),
('200402', 'Buenos Aires', '2004', true, NOW()),
('200403', 'Chalaco', '2004', true, NOW()),
('200404', 'La Matanza', '2004', true, NOW()),
('200405', 'Morropón', '2004', true, NOW()),
('200406', 'Salitral', '2004', true, NOW()),
('200407', 'San Juan de Bigote', '2004', true, NOW()),
('200408', 'Santa Catalina de Mossa', '2004', true, NOW()),
('200409', 'Santo Domingo', '2004', true, NOW()),
('200410', 'Yamango', '2004', true, NOW());

-- Piura / Paita (2005)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('200501', 'Paita', '2005', true, NOW()),
('200502', 'Amotape', '2005', true, NOW()),
('200503', 'Arenal', '2005', true, NOW()),
('200504', 'Colán', '2005', true, NOW()),
('200505', 'La Huaca', '2005', true, NOW()),
('200506', 'Tamarindo', '2005', true, NOW()),
('200507', 'Vichayal', '2005', true, NOW());

-- Piura / Sullana (2006)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('200601', 'Sullana', '2006', true, NOW()),
('200602', 'Bellavista', '2006', true, NOW()),
('200603', 'Ignacio Escudero', '2006', true, NOW()),
('200604', 'Lancones', '2006', true, NOW()),
('200605', 'Marcavelica', '2006', true, NOW()),
('200606', 'Miguel Checa', '2006', true, NOW()),
('200607', 'Querecotillo', '2006', true, NOW()),
('200608', 'Salitral', '2006', true, NOW());

-- Piura / Talara (2007)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('200701', 'Talara', '2007', true, NOW()),
('200702', 'El Alto', '2007', true, NOW()),
('200703', 'La Brea', '2007', true, NOW()),
('200704', 'Lobitos', '2007', true, NOW()),
('200705', 'Los Órganos', '2007', true, NOW()),
('200706', 'Máncora', '2007', true, NOW()),
('200707', 'Pariñas', '2007', true, NOW());

-- Piura / Sechura (2008)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('200801', 'Sechura', '2008', true, NOW()),
('200802', 'Bellavista de la Unión', '2008', true, NOW()),
('200803', 'Bernal', '2008', true, NOW()),
('200804', 'Cristo Nos Valga', '2008', true, NOW()),
('200805', 'Vice', '2008', true, NOW()),
('200806', 'Rinconada Llicuar', '2008', true, NOW());

-- Puno / Puno (2101)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('210101', 'Puno', '2101', true, NOW()),
('210102', 'Acora', '2101', true, NOW()),
('210103', 'Amantaní', '2101', true, NOW()),
('210104', 'Atuncolla', '2101', true, NOW()),
('210105', 'Capachica', '2101', true, NOW()),
('210106', 'Chucuito', '2101', true, NOW()),
('210107', 'Coata', '2101', true, NOW()),
('210108', 'Huata', '2101', true, NOW()),
('210109', 'Mañazo', '2101', true, NOW()),
('210110', 'Paucarcolla', '2101', true, NOW()),
('210111', 'Pichacani', '2101', true, NOW()),
('210112', 'Platería', '2101', true, NOW()),
('210113', 'San Antonio', '2101', true, NOW()),
('210114', 'Tiquillaca', '2101', true, NOW()),
('210115', 'Vilque', '2101', true, NOW());

-- Puno / Azángaro (2102)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('210201', 'Azángaro', '2102', true, NOW()),
('210202', 'Achaya', '2102', true, NOW()),
('210203', 'Arapa', '2102', true, NOW()),
('210204', 'Asillo', '2102', true, NOW()),
('210205', 'Caminaca', '2102', true, NOW()),
('210206', 'Chupa', '2102', true, NOW()),
('210207', 'José Domingo Choquehuanca', '2102', true, NOW()),
('210208', 'Muñani', '2102', true, NOW()),
('210209', 'Potoni', '2102', true, NOW()),
('210210', 'Samán', '2102', true, NOW()),
('210211', 'San Antón', '2102', true, NOW()),
('210212', 'San José', '2102', true, NOW()),
('210213', 'San Juan de Salinas', '2102', true, NOW()),
('210214', 'Santiago de Pupuja', '2102', true, NOW()),
('210215', 'Tirapata', '2102', true, NOW());

-- Puno / Carabaya (2103)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('210301', 'Macusani', '2103', true, NOW()),
('210302', 'Ajoyani', '2103', true, NOW()),
('210303', 'Ayapata', '2103', true, NOW()),
('210304', 'Coasa', '2103', true, NOW()),
('210305', 'Corani', '2103', true, NOW()),
('210306', 'Crucero', '2103', true, NOW()),
('210307', 'Ituata', '2103', true, NOW()),
('210308', 'Ollachea', '2103', true, NOW()),
('210309', 'San Gaban', '2103', true, NOW()),
('210310', 'Usicayos', '2103', true, NOW());

-- Puno / Chucuito (2104)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('210401', 'Juli', '2104', true, NOW()),
('210402', 'Desaguadero', '2104', true, NOW()),
('210403', 'Huacullani', '2104', true, NOW()),
('210404', 'Kelluyo', '2104', true, NOW()),
('210405', 'Pisacoma', '2104', true, NOW()),
('210406', 'Pomata', '2104', true, NOW()),
('210407', 'Zepita', '2104', true, NOW());

-- Puno / El Collao (2105)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('210501', 'Ilave', '2105', true, NOW()),
('210502', 'Capazo', '2105', true, NOW()),
('210503', 'Pilcuyo', '2105', true, NOW()),
('210504', 'Santa Rosa', '2105', true, NOW()),
('210505', 'Conduriri', '2105', true, NOW());

-- Puno / Huancané (2106)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('210601', 'Huancané', '2106', true, NOW()),
('210602', 'Cojata', '2106', true, NOW()),
('210603', 'Huatasani', '2106', true, NOW()),
('210604', 'Inchupalla', '2106', true, NOW()),
('210605', 'Pusi', '2106', true, NOW()),
('210606', 'Rosaspata', '2106', true, NOW()),
('210607', 'Taraco', '2106', true, NOW()),
('210608', 'Vilque Chico', '2106', true, NOW());

-- Puno / Lampa (2107)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('210701', 'Lampa', '2107', true, NOW()),
('210702', 'Cabanilla', '2107', true, NOW()),
('210703', 'Calapuja', '2107', true, NOW()),
('210704', 'Nicasio', '2107', true, NOW()),
('210705', 'Ocuviri', '2107', true, NOW()),
('210706', 'Palca', '2107', true, NOW()),
('210707', 'Paratía', '2107', true, NOW()),
('210708', 'Pucará', '2107', true, NOW()),
('210709', 'Santa Lucía', '2107', true, NOW()),
('210710', 'Vilavila', '2107', true, NOW());

-- Puno / Melgar (2108)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('210801', 'Ayaviri', '2108', true, NOW()),
('210802', 'Antauta', '2108', true, NOW()),
('210803', 'Cupi', '2108', true, NOW()),
('210804', 'Llalli', '2108', true, NOW()),
('210805', 'Macari', '2108', true, NOW()),
('210806', 'Nuñoa', '2108', true, NOW()),
('210807', 'Orurillo', '2108', true, NOW()),
('210808', 'Santa Rosa', '2108', true, NOW()),
('210809', 'Umachiri', '2108', true, NOW());

-- Puno / Moho (2109)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('210901', 'Moho', '2109', true, NOW()),
('210902', 'Conima', '2109', true, NOW()),
('210903', 'Huayrapata', '2109', true, NOW()),
('210904', 'Tilali', '2109', true, NOW());

-- Puno / San Antonio de Putina (2110)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('211001', 'Putina', '2110', true, NOW()),
('211002', 'Ananea', '2110', true, NOW()),
('211003', 'Pedro Vilca Apaza', '2110', true, NOW()),
('211004', 'Quilcapuncu', '2110', true, NOW()),
('211005', 'Sina', '2110', true, NOW());

-- Puno / San Román (2111)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('211101', 'Juliaca', '2111', true, NOW()),
('211102', 'Cabana', '2111', true, NOW()),
('211103', 'Cabanillas', '2111', true, NOW()),
('211104', 'Caracoto', '2111', true, NOW()),
('211105', 'San Miguel', '2111', true, NOW());

-- Puno / Sandia (2112)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('211201', 'Sandia', '2112', true, NOW()),
('211202', 'Cuyocuyo', '2112', true, NOW()),
('211203', 'Limbani', '2112', true, NOW()),
('211204', 'Patambuco', '2112', true, NOW()),
('211205', 'Phara', '2112', true, NOW()),
('211206', 'Quiaca', '2112', true, NOW()),
('211207', 'San Juan del Oro', '2112', true, NOW()),
('211208', 'Yanahuaya', '2112', true, NOW()),
('211209', 'Alto Inambari', '2112', true, NOW()),
('211210', 'San Pedro de Putina Punco', '2112', true, NOW());

-- Puno / Yunguyo (2113)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('211301', 'Yunguyo', '2113', true, NOW()),
('211302', 'Anapia', '2113', true, NOW()),
('211303', 'Copani', '2113', true, NOW()),
('211304', 'Cuturapi', '2113', true, NOW()),
('211305', 'Ollaraya', '2113', true, NOW()),
('211306', 'Tinicachi', '2113', true, NOW()),
('211307', 'Unicachi', '2113', true, NOW());

-- San Martín / Moyobamba (2201)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('220101', 'Moyobamba', '2201', true, NOW()),
('220102', 'Calzada', '2201', true, NOW()),
('220103', 'Habana', '2201', true, NOW()),
('220104', 'Jepelacio', '2201', true, NOW()),
('220105', 'Soritor', '2201', true, NOW()),
('220106', 'Yantalo', '2201', true, NOW());

-- San Martín / Bellavista (2202)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('220201', 'Bellavista', '2202', true, NOW()),
('220202', 'Alto Biavo', '2202', true, NOW()),
('220203', 'Bajo Biavo', '2202', true, NOW()),
('220204', 'Huallaga', '2202', true, NOW()),
('220205', 'San Pablo', '2202', true, NOW()),
('220206', 'San Rafael', '2202', true, NOW());

-- San Martín / El Dorado (2203)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('220301', 'San José de Sisa', '2203', true, NOW()),
('220302', 'Agua Blanca', '2203', true, NOW()),
('220303', 'San Martín', '2203', true, NOW()),
('220304', 'Santa Rosa', '2203', true, NOW()),
('220305', 'Shatoja', '2203', true, NOW());

-- San Martín / Huallaga (2204)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('220401', 'Saposoa', '2204', true, NOW()),
('220402', 'Alto Saposoa', '2204', true, NOW()),
('220403', 'El Eslabón', '2204', true, NOW()),
('220404', 'Piscoyacu', '2204', true, NOW()),
('220405', 'Sacanche', '2204', true, NOW()),
('220406', 'Tingo de Saposoa', '2204', true, NOW());

-- San Martín / Lamas (2205)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('220501', 'Lamas', '2205', true, NOW()),
('220502', 'Alonso de Alvarado', '2205', true, NOW()),
('220503', 'Barranquita', '2205', true, NOW()),
('220504', 'Caynarachi', '2205', true, NOW()),
('220505', 'Cuñumbuqui', '2205', true, NOW()),
('220506', 'Pinto Recodo', '2205', true, NOW()),
('220507', 'Rumisapa', '2205', true, NOW()),
('220508', 'San Roque de Cumbaza', '2205', true, NOW()),
('220509', 'Shanao', '2205', true, NOW()),
('220510', 'Tabalosos', '2205', true, NOW()),
('220511', 'Zapatero', '2205', true, NOW());

-- San Martín / Mariscal Cáceres (2206)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('220601', 'Juanjuí', '2206', true, NOW()),
('220602', 'Campanilla', '2206', true, NOW()),
('220603', 'Huicungo', '2206', true, NOW()),
('220604', 'Pachiza', '2206', true, NOW()),
('220605', 'Pajarillo', '2206', true, NOW());

-- San Martín / Picota (2207)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('220701', 'Picota', '2207', true, NOW()),
('220702', 'Buenos Aires', '2207', true, NOW()),
('220703', 'Caspisapa', '2207', true, NOW()),
('220704', 'Pilluana', '2207', true, NOW()),
('220705', 'Pucacaca', '2207', true, NOW()),
('220706', 'San Cristóbal', '2207', true, NOW()),
('220707', 'San Hilarión', '2207', true, NOW()),
('220708', 'Shamboyacu', '2207', true, NOW()),
('220709', 'Tingo de Ponasa', '2207', true, NOW()),
('220710', 'Tres Unidos', '2207', true, NOW());

-- San Martín / Rioja (2208)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('220801', 'Rioja', '2208', true, NOW()),
('220802', 'Awajun', '2208', true, NOW()),
('220803', 'Elías Soplin Vargas', '2208', true, NOW()),
('220804', 'Nueva Cajamarca', '2208', true, NOW()),
('220805', 'Pardo Miguel', '2208', true, NOW()),
('220806', 'Posic', '2208', true, NOW()),
('220807', 'San Fernando', '2208', true, NOW()),
('220808', 'Yorongos', '2208', true, NOW()),
('220809', 'Yuracyacu', '2208', true, NOW());

-- San Martín / San Martín (2209)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('220901', 'Tarapoto', '2209', true, NOW()),
('220902', 'Alberto Leveau', '2209', true, NOW()),
('220903', 'Cacatachi', '2209', true, NOW()),
('220904', 'Chazuta', '2209', true, NOW()),
('220905', 'Chipurana', '2209', true, NOW()),
('220906', 'El Porvenir', '2209', true, NOW()),
('220907', 'Huimbayoc', '2209', true, NOW()),
('220908', 'Juan Guerra', '2209', true, NOW()),
('220909', 'La Banda de Shilcayo', '2209', true, NOW()),
('220910', 'Morales', '2209', true, NOW()),
('220911', 'Papaplaya', '2209', true, NOW()),
('220912', 'San Antonio', '2209', true, NOW()),
('220913', 'Sauce', '2209', true, NOW()),
('220914', 'Shapaja', '2209', true, NOW());

-- San Martín / Tocache (2210)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('221001', 'Tocache', '2210', true, NOW()),
('221002', 'Nuevo Progreso', '2210', true, NOW()),
('221003', 'Polvora', '2210', true, NOW()),
('221004', 'Shunte', '2210', true, NOW()),
('221005', 'Uchiza', '2210', true, NOW());

-- Tacna / Tacna (2301)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('230101', 'Tacna', '2301', true, NOW()),
('230102', 'Alto de la Alianza', '2301', true, NOW()),
('230103', 'Calana', '2301', true, NOW()),
('230104', 'Ciudad Nueva', '2301', true, NOW()),
('230105', 'Inclán', '2301', true, NOW()),
('230106', 'Pachía', '2301', true, NOW()),
('230107', 'Palca', '2301', true, NOW()),
('230108', 'Pocollay', '2301', true, NOW()),
('230109', 'Sama', '2301', true, NOW()),
('230110', 'Coronel Gregorio Albarracín Lanchipa', '2301', true, NOW()),
('230111', 'La Yarada los Palos', '2301', true, NOW());

-- Tacna / Candarave (2302)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('230201', 'Candarave', '2302', true, NOW()),
('230202', 'Cairani', '2302', true, NOW()),
('230203', 'Camilaca', '2302', true, NOW()),
('230204', 'Curibaya', '2302', true, NOW()),
('230205', 'Huanuara', '2302', true, NOW()),
('230206', 'Quilahuani', '2302', true, NOW());

-- Tacna / Jorge Basadre (2303)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('230301', 'Locumba', '2303', true, NOW()),
('230302', 'Ilabaya', '2303', true, NOW()),
('230303', 'Ite', '2303', true, NOW());

-- Tacna / Tarata (2304)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('230401', 'Tarata', '2304', true, NOW()),
('230402', 'Héroes Albarracín', '2304', true, NOW()),
('230403', 'Estique', '2304', true, NOW()),
('230404', 'Estique Pampa', '2304', true, NOW()),
('230405', 'Sitajara', '2304', true, NOW()),
('230406', 'Suscapaya', '2304', true, NOW()),
('230407', 'Tarucachi', '2304', true, NOW()),
('230408', 'Ticaco', '2304', true, NOW());

-- Tumbes / Tumbes (2401)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('240101', 'Tumbes', '2401', true, NOW()),
('240102', 'Corrales', '2401', true, NOW()),
('240103', 'La Cruz', '2401', true, NOW()),
('240104', 'Pampas de Hospital', '2401', true, NOW()),
('240105', 'San Jacinto', '2401', true, NOW()),
('240106', 'San Juan de la Virgen', '2401', true, NOW());

-- Tumbes / Contralmirante Villar (2402)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('240201', 'Zorritos', '2402', true, NOW()),
('240202', 'Casitas', '2402', true, NOW()),
('240203', 'Canoas de Punta Sal', '2402', true, NOW());

-- Tumbes / Zarumilla (2403)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('240301', 'Zarumilla', '2403', true, NOW()),
('240302', 'Aguas Verdes', '2403', true, NOW()),
('240303', 'Matapalo', '2403', true, NOW()),
('240304', 'Papayal', '2403', true, NOW());

-- Ucayali / Coronel Portillo (2501)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('250101', 'Callería', '2501', true, NOW()),
('250102', 'Campoverde', '2501', true, NOW()),
('250103', 'Iparía', '2501', true, NOW()),
('250104', 'Masisea', '2501', true, NOW()),
('250105', 'Yarinacocha', '2501', true, NOW()),
('250106', 'Nueva Requena', '2501', true, NOW()),
('250107', 'Manantay', '2501', true, NOW());

-- Ucayali / Atalaya (2502)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('250201', 'Raimondi', '2502', true, NOW()),
('250202', 'Sepahua', '2502', true, NOW()),
('250203', 'Tahuanía', '2502', true, NOW()),
('250204', 'Yurúa', '2502', true, NOW());

-- Ucayali / Padre Abad (2503)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('250301', 'Padre Abad', '2503', true, NOW()),
('250302', 'Irazola', '2503', true, NOW()),
('250303', 'Curimaná', '2503', true, NOW()),
('250304', 'Neshuya', '2503', true, NOW()),
('250305', 'Alexander Von Humboldt', '2503', true, NOW());

-- Ucayali / Purús (2504)
INSERT INTO tb_ubigeo_distritos (ubdist_codigo, ubdist_nombre, ubdist_provincia, ubdist_activo, ubdist_created_at) VALUES
('250401', 'Purús', '2504', true, NOW());
