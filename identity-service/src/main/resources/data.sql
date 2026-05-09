-- 1. Poblar Distritos y Regiones
INSERT INTO districts (id, name, province, region) VALUES (150101, 'Cercado de Lima', 'Lima', 'Lima Metropolitana') ON CONFLICT DO NOTHING;
INSERT INTO districts (id, name, province, region) VALUES (150132, 'San Juan de Lurigancho', 'Lima', 'Lima Metropolitana') ON CONFLICT DO NOTHING;
INSERT INTO districts (id, name, province, region) VALUES (150103, 'Ate', 'Lima', 'Lima Metropolitana') ON CONFLICT DO NOTHING;

-- 2. Poblar Paraderos Clave de "ChapaTuRuta" (Para las pruebas de Check-in y Mapas)
-- Nota: uuid_generate_v4() funciona si la BD PostgreSQL tiene habilitada la extensión, si no, usamos UUIDs estáticos.
INSERT INTO stops (id, name, address, reference, latitude, longitude, district_id)
VALUES ('11111111-1111-1111-1111-111111111111', 'Estación Bayóvar', 'Av. Próceres de la Independencia', 'Frente a Metro', -11.9686, -77.0016, 150132)
    ON CONFLICT DO NOTHING;

INSERT INTO stops (id, name, address, reference, latitude, longitude, district_id)
VALUES ('22222222-2222-2222-2222-222222222222', 'Óvalo Santa Anita', 'Carretera Central', 'Junto al Mall Aventura', -12.0435, -76.9532, 150103)
    ON CONFLICT DO NOTHING;

INSERT INTO stops (id, name, address, reference, latitude, longitude, district_id)
VALUES ('33333333-3333-3333-3333-333333333333', 'Puente Nuevo', 'Vía de Evitamiento', 'Paradero dirección Sur', -12.0305, -76.9945, 150101)
    ON CONFLICT DO NOTHING;