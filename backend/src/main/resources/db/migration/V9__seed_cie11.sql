-- =============================================================================
-- V9: CIE-11 Seed Data — Representative diagnostic codes
-- 100+ codes covering major clinical categories
-- Batched INSERTs for performance
-- =============================================================================

-- Category A: Certain infectious or parasitic diseases
INSERT INTO tb_cie11_diagnosticos (cie_codigo, cie_descripcion, cie_categoria, cie_sexo_aplicable, cie_version, cie_frecuencia_uso) VALUES
('1A00.0', 'Cólera', 'A', 'AMBOS', 'CIE-11', 1),
('1A01', 'Fiebre tifoidea', 'A', 'AMBOS', 'CIE-11', 5),
('1A04', 'Shigelosis', 'A', 'AMBOS', 'CIE-11', 3),
('1A05', 'Infecciones por Escherichia coli', 'A', 'AMBOS', 'CIE-11', 10),
('1A07', 'Campilobacteriosis intestinal', 'A', 'AMBOS', 'CIE-11', 8),
('1A11', 'Tuberculosis respiratoria', 'A', 'AMBOS', 'CIE-11', 15),
('1B20', 'Sífilis', 'A', 'AMBOS', 'CIE-11', 12),
('1B21', 'Infección gonocócica', 'A', 'AMBOS', 'CIE-11', 20),
('1B50', 'Hepatitis viral aguda tipo A', 'A', 'AMBOS', 'CIE-11', 25),
('1B51', 'Hepatitis viral aguda tipo B', 'A', 'AMBOS', 'CIE-11', 30),
('1C00', 'VIH/SIDA', 'A', 'AMBOS', 'CIE-11', 18),
('1C1A', 'Dengue', 'A', 'AMBOS', 'CIE-11', 50),
('1C1B', 'Fiebre chikungunya', 'A', 'AMBOS', 'CIE-11', 35),
('1C1C', 'Zika', 'A', 'AMBOS', 'CIE-11', 15),
('1C30', 'Malaria por Plasmodium falciparum', 'A', 'AMBOS', 'CIE-11', 22),
('1C31', 'Malaria por Plasmodium vivax', 'A', 'AMBOS', 'CIE-11', 28),
('1C40', 'Leishmaniasis', 'A', 'AMBOS', 'CIE-11', 10),
('1D00', 'Infección de vías urinarias', 'A', 'AMBOS', 'CIE-11', 100),
('1D01', 'Infección intestinal bacteriana no especificada', 'A', 'AMBOS', 'CIE-11', 45),
('1E20', 'Candidiasis', 'A', 'AMBOS', 'CIE-11', 40),
('1F00', 'COVID-19', 'A', 'AMBOS', 'CIE-11', 200);

-- Category B: Endocrine, nutritional or metabolic diseases
INSERT INTO tb_cie11_diagnosticos (cie_codigo, cie_descripcion, cie_categoria, cie_sexo_aplicable, cie_version, cie_frecuencia_uso) VALUES
('5A10', 'Diabetes mellitus tipo 1', 'B', 'AMBOS', 'CIE-11', 60),
('5A11', 'Diabetes mellitus tipo 2', 'B', 'AMBOS', 'CIE-11', 200),
('5A20', 'Obesidad', 'B', 'AMBOS', 'CIE-11', 80),
('5A40', 'Hipotiroidismo', 'B', 'AMBOS', 'CIE-11', 55),
('5A41', 'Hipertiroidismo', 'B', 'AMBOS', 'CIE-11', 25),
('5B00', 'Desnutrición', 'B', 'AMBOS', 'CIE-11', 30),
('5B51', 'Dislipidemia', 'B', 'AMBOS', 'CIE-11', 90),
('5C50', 'Deshidratación', 'B', 'AMBOS', 'CIE-11', 70);

-- Category C: Mental, behavioural or neurodevelopmental disorders
INSERT INTO tb_cie11_diagnosticos (cie_codigo, cie_descripcion, cie_categoria, cie_sexo_aplicable, cie_version, cie_frecuencia_uso) VALUES
('6A00', 'Trastorno depresivo recurrente', 'C', 'AMBOS', 'CIE-11', 45),
('6A01', 'Trastorno de ansiedad generalizada', 'C', 'AMBOS', 'CIE-11', 60),
('6A02', 'Trastorno de pánico', 'C', 'AMBOS', 'CIE-11', 30),
('6A20', 'Esquizofrenia', 'C', 'AMBOS', 'CIE-11', 15),
('6A70', 'Insomnio crónico', 'C', 'AMBOS', 'CIE-11', 40),
('6B20', 'Trastorno por consumo de alcohol', 'C', 'AMBOS', 'CIE-11', 35),
('6C40', 'Trastorno de estrés postraumático', 'C', 'AMBOS', 'CIE-11', 20);

-- Category D: Diseases of the nervous system
INSERT INTO tb_cie11_diagnosticos (cie_codigo, cie_descripcion, cie_categoria, cie_sexo_aplicable, cie_version, cie_frecuencia_uso) VALUES
('8A00', 'Migraña', 'D', 'AMBOS', 'CIE-11', 90),
('8A01', 'Cefalea tensional', 'D', 'AMBOS', 'CIE-11', 75),
('8A20', 'Epilepsia', 'D', 'AMBOS', 'CIE-11', 25),
('8B00', 'Enfermedad de Parkinson', 'D', 'AMBOS', 'CIE-11', 10),
('8B20', 'Enfermedad de Alzheimer', 'D', 'AMBOS', 'CIE-11', 12),
('8C00', 'Neuropatía periférica', 'D', 'AMBOS', 'CIE-11', 30),
('8C70', 'Accidente cerebrovascular isquémico', 'D', 'AMBOS', 'CIE-11', 20),
('8D00', 'Hernia de disco lumbar', 'D', 'AMBOS', 'CIE-11', 40);

-- Category E: Diseases of the circulatory system
INSERT INTO tb_cie11_diagnosticos (cie_codigo, cie_descripcion, cie_categoria, cie_sexo_aplicable, cie_version, cie_frecuencia_uso) VALUES
('BA00', 'Hipertensión esencial primaria', 'E', 'AMBOS', 'CIE-11', 250),
('BA40', 'Cardiopatía isquémica', 'E', 'AMBOS', 'CIE-11', 60),
('BA60', 'Insuficiencia cardíaca', 'E', 'AMBOS', 'CIE-11', 40),
('BA80', 'Fibrilación auricular', 'E', 'AMBOS', 'CIE-11', 25),
('BB00', 'Flebitis y tromboflebitis', 'E', 'AMBOS', 'CIE-11', 15),
('BB10', 'Venas varicosas', 'E', 'AMBOS', 'CIE-11', 20),
('BB20', 'Hemorroides', 'E', 'AMBOS', 'CIE-11', 50),
('BC10', 'Infarto agudo de miocardio', 'E', 'AMBOS', 'CIE-11', 18);

-- Category F: Diseases of the respiratory system
INSERT INTO tb_cie11_diagnosticos (cie_codigo, cie_descripcion, cie_categoria, cie_sexo_aplicable, cie_version, cie_frecuencia_uso) VALUES
('CA00', 'Faringitis aguda', 'F', 'AMBOS', 'CIE-11', 180),
('CA01', 'Rinitis alérgica', 'F', 'AMBOS', 'CIE-11', 90),
('CA02', 'Sinusitis aguda', 'F', 'AMBOS', 'CIE-11', 60),
('CA10', 'Amigdalitis aguda', 'F', 'AMBOS', 'CIE-11', 120),
('CA20', 'Laringitis aguda', 'F', 'AMBOS', 'CIE-11', 50),
('CA40', 'Bronquitis aguda', 'F', 'AMBOS', 'CIE-11', 100),
('CA60', 'Neumonía bacteriana', 'F', 'AMBOS', 'CIE-11', 70),
('CA61', 'Neumonía viral', 'F', 'AMBOS', 'CIE-11', 45),
('CA70', 'Asma bronquial', 'F', 'AMBOS', 'CIE-11', 85),
('CA80', 'Enfermedad pulmonar obstructiva crónica', 'F', 'AMBOS', 'CIE-11', 55),
('CB00', 'Infección respiratoria aguda no especificada', 'F', 'AMBOS', 'CIE-11', 300);

-- Category G: Diseases of the digestive system
INSERT INTO tb_cie11_diagnosticos (cie_codigo, cie_descripcion, cie_categoria, cie_sexo_aplicable, cie_version, cie_frecuencia_uso) VALUES
('DA00', 'Caries dental', 'G', 'AMBOS', 'CIE-11', 150),
('DA01', 'Gingivitis', 'G', 'AMBOS', 'CIE-11', 80),
('DA20', 'Esofagitis', 'G', 'AMBOS', 'CIE-11', 40),
('DA21', 'Enfermedad por reflujo gastroesofágico', 'G', 'AMBOS', 'CIE-11', 70),
('DA40', 'Gastritis aguda', 'G', 'AMBOS', 'CIE-11', 110),
('DA41', 'Gastritis crónica', 'G', 'AMBOS', 'CIE-11', 90),
('DA42', 'Úlcera péptica', 'G', 'AMBOS', 'CIE-11', 30),
('DA60', 'Apendicitis aguda', 'G', 'AMBOS', 'CIE-11', 25),
('DA70', 'Hernia inguinal', 'G', 'AMBOS', 'CIE-11', 35),
('DB00', 'Síndrome de intestino irritable', 'G', 'AMBOS', 'CIE-11', 65),
('DB10', 'Estreñimiento crónico', 'G', 'AMBOS', 'CIE-11', 55),
('DB20', 'Colelitiasis', 'G', 'AMBOS', 'CIE-11', 30),
('DC10', 'Enfermedad hepática alcohólica', 'G', 'AMBOS', 'CIE-11', 20),
('DC20', 'Pancreatitis aguda', 'G', 'AMBOS', 'CIE-11', 15);

-- Category H: Diseases of the musculoskeletal system or connective tissue
INSERT INTO tb_cie11_diagnosticos (cie_codigo, cie_descripcion, cie_categoria, cie_sexo_aplicable, cie_version, cie_frecuencia_uso) VALUES
('FA00', 'Artrosis de rodilla', 'H', 'AMBOS', 'CIE-11', 50),
('FA01', 'Artrosis de cadera', 'H', 'AMBOS', 'CIE-11', 30),
('FA20', 'Artritis reumatoide', 'H', 'AMBOS', 'CIE-11', 20),
('FA30', 'Lumbago', 'H', 'AMBOS', 'CIE-11', 120),
('FA31', 'Ciática', 'H', 'AMBOS', 'CIE-11', 60),
('FA40', 'Cervicalgia', 'H', 'AMBOS', 'CIE-11', 80),
('FA50', 'Osteoporosis', 'H', 'AMBOS', 'CIE-11', 25),
('FB00', 'Fractura de radio distal', 'H', 'AMBOS', 'CIE-11', 20),
('FB10', 'Esguince de tobillo', 'H', 'AMBOS', 'CIE-11', 55),
('FB30', 'Tendinitis del manguito rotador', 'H', 'AMBOS', 'CIE-11', 35);

-- Category I: Diseases of the genitourinary system
INSERT INTO tb_cie11_diagnosticos (cie_codigo, cie_descripcion, cie_categoria, cie_sexo_aplicable, cie_version, cie_frecuencia_uso) VALUES
('GA00', 'Infección urinaria baja', 'I', 'AMBOS', 'CIE-11', 130),
('GA10', 'Cálculo renal', 'I', 'AMBOS', 'CIE-11', 25),
('GA20', 'Insuficiencia renal crónica', 'I', 'AMBOS', 'CIE-11', 15),
('GA40', 'Hiperplasia prostática benigna', 'I', 'M', 'CIE-11', 40),
('GB00', 'Vaginitis', 'I', 'F', 'CIE-11', 60),
('GB10', 'Enfermedad inflamatoria pélvica', 'I', 'F', 'CIE-11', 20),
('GC00', 'Dismenorrea', 'I', 'F', 'CIE-11', 70),
('GC10', 'Trastornos de la menopausia', 'I', 'F', 'CIE-11', 35),
('GC20', 'Fibroma uterino', 'I', 'F', 'CIE-11', 20);

-- Category J: Injuries, poisoning or other consequences of external causes
INSERT INTO tb_cie11_diagnosticos (cie_codigo, cie_descripcion, cie_categoria, cie_sexo_aplicable, cie_version, cie_frecuencia_uso) VALUES
('JA00', 'Herida cortante de mano', 'J', 'AMBOS', 'CIE-11', 80),
('JA01', 'Herida contusa de cabeza', 'J', 'AMBOS', 'CIE-11', 45),
('JA10', 'Quemadura de primer grado', 'J', 'AMBOS', 'CIE-11', 30),
('JA20', 'Contusión de miembro inferior', 'J', 'AMBOS', 'CIE-11', 60),
('JB00', 'Traumatismo craneoencefálico', 'J', 'AMBOS', 'CIE-11', 15),
('JB30', 'Luxación de hombro', 'J', 'AMBOS', 'CIE-11', 20),
('JC00', 'Intoxicación alimentaria', 'J', 'AMBOS', 'CIE-11', 25),
('JD00', 'Reacción alérgica no especificada', 'J', 'AMBOS', 'CIE-11', 35);

-- Category K: Surgical procedure codes
INSERT INTO tb_cie11_diagnosticos (cie_codigo, cie_descripcion, cie_categoria, cie_sexo_aplicable, cie_version, cie_frecuencia_uso) VALUES
('KA00', 'Apendicectomía', 'K', 'AMBOS', 'CIE-11', 20),
('KA10', 'Colecistectomía laparoscópica', 'K', 'AMBOS', 'CIE-11', 18),
('KA20', 'Reparación de hernia inguinal', 'K', 'AMBOS', 'CIE-11', 25),
('KA30', 'Cesárea', 'K', 'F', 'CIE-11', 30),
('KA40', 'Cirugía de catarata', 'K', 'AMBOS', 'CIE-11', 15),
('KB00', 'Reducción abierta de fractura', 'K', 'AMBOS', 'CIE-11', 22),
('KC00', 'Amigdalectomía', 'K', 'AMBOS', 'CIE-11', 12);
