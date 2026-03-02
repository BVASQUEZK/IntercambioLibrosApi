-- ==========================================================
-- DEMO REAL: INTERCAMBIO COMPLETO EN CHICLAYO, PERU
-- Crea 2 usuarios cercanos, 2 libros, chat, punto de encuentro
-- y solicitud finalizada.
-- ==========================================================

-- 1) Usuarios demo
INSERT INTO usuario (nombres, apellidos, dni, correo, telefono, password, url_foto_perfil)
VALUES
('Andrea', 'Sanchez', '80000011', 'andrea.chiclayo@demo.com', '987111222', 'demo123', NULL),
('Miguel', 'Cruz', '80000012', 'miguel.chiclayo@demo.com', '987333444', 'demo123', NULL)
ON CONFLICT (correo) DO NOTHING;

-- 2) Ubicaciones cercanas (Chiclayo)
INSERT INTO ubicacion (id_usuario, latitud, longitud, direccion, ciudad)
SELECT u.id_usuario, -6.771405, -79.840881, 'Plaza de Armas de Chiclayo', 'Chiclayo'
FROM usuario u
WHERE u.correo = 'andrea.chiclayo@demo.com'
  AND NOT EXISTS (SELECT 1 FROM ubicacion x WHERE x.id_usuario = u.id_usuario);

INSERT INTO ubicacion (id_usuario, latitud, longitud, direccion, ciudad)
SELECT u.id_usuario, -6.776200, -79.844100, 'Av. Balta, Chiclayo', 'Chiclayo'
FROM usuario u
WHERE u.correo = 'miguel.chiclayo@demo.com'
  AND NOT EXISTS (SELECT 1 FROM ubicacion x WHERE x.id_usuario = u.id_usuario);

-- 3) Categoria base para los libros
INSERT INTO categoria (nombre) VALUES ('Novela')
ON CONFLICT (nombre) DO NOTHING;

-- 4) Libros de cada usuario (ambos tienen libro subido)
INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, ubicacion, disponible)
SELECT
    u.id_usuario,
    c.id_categoria,
    'La ciudad y los perros',
    'Mario Vargas Llosa',
    'Libro en buen estado para intercambio en Chiclayo.',
    'bueno',
    'lat=-6.771405,lng=-79.840881|ref=Plaza de Armas de Chiclayo',
    FALSE
FROM usuario u
CROSS JOIN categoria c
WHERE u.correo = 'andrea.chiclayo@demo.com'
  AND c.nombre = 'Novela'
  AND NOT EXISTS (
      SELECT 1
      FROM libro l
      WHERE l.id_usuario = u.id_usuario
        AND l.titulo = 'La ciudad y los perros'
  );

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, ubicacion, disponible)
SELECT
    u.id_usuario,
    c.id_categoria,
    'Conversacion en La Catedral',
    'Mario Vargas Llosa',
    'Listo para intercambio; entrega en punto acordado.',
    'muy bueno',
    'lat=-6.776200,lng=-79.844100|ref=Av. Balta, Chiclayo',
    FALSE
FROM usuario u
CROSS JOIN categoria c
WHERE u.correo = 'miguel.chiclayo@demo.com'
  AND c.nombre = 'Novela'
  AND NOT EXISTS (
      SELECT 1
      FROM libro l
      WHERE l.id_usuario = u.id_usuario
        AND l.titulo = 'Conversacion en La Catedral'
  );

INSERT INTO libro_categoria (id_libro, id_categoria)
SELECT l.id_libro, l.id_categoria
FROM libro l
WHERE l.id_categoria IS NOT NULL
ON CONFLICT DO NOTHING;

-- 5) Imagenes de libros
INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT l.id_libro, 'https://picsum.photos/seed/chiclayo-libro-a/700/1000'
FROM libro l
WHERE l.titulo = 'La ciudad y los perros'
  AND NOT EXISTS (
      SELECT 1 FROM imagen_libro i
      WHERE i.id_libro = l.id_libro
        AND i.url_imagen = 'https://picsum.photos/seed/chiclayo-libro-a/700/1000'
  );

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT l.id_libro, 'https://picsum.photos/seed/chiclayo-libro-b/700/1000'
FROM libro l
WHERE l.titulo = 'Conversacion en La Catedral'
  AND NOT EXISTS (
      SELECT 1 FROM imagen_libro i
      WHERE i.id_libro = l.id_libro
        AND i.url_imagen = 'https://picsum.photos/seed/chiclayo-libro-b/700/1000'
  );

-- 6) Solicitud + detalle + encuentro + mensajes + estado finalizado
DO $$
DECLARE
    v_andrea_id INT;
    v_miguel_id INT;
    v_libro_andrea INT;
    v_libro_miguel INT;
    v_solicitud_id INT;
BEGIN
    SELECT id_usuario INTO v_andrea_id FROM usuario WHERE correo = 'andrea.chiclayo@demo.com';
    SELECT id_usuario INTO v_miguel_id FROM usuario WHERE correo = 'miguel.chiclayo@demo.com';
    SELECT id_libro INTO v_libro_andrea
    FROM libro
    WHERE id_usuario = v_andrea_id AND titulo = 'La ciudad y los perros';
    SELECT id_libro INTO v_libro_miguel
    FROM libro
    WHERE id_usuario = v_miguel_id AND titulo = 'Conversacion en La Catedral';

    SELECT s.id_solicitud
      INTO v_solicitud_id
      FROM solicitud s
      JOIN detalle_solicitud ds
        ON ds.id_solicitud = s.id_solicitud
       AND ds.id_libro = v_libro_miguel
       AND ds.propietario = 'receptor'
     WHERE s.id_solicitante = v_andrea_id
       AND s.id_receptor = v_miguel_id
       AND s.tipo = 'intercambio'
     ORDER BY s.id_solicitud DESC
     LIMIT 1;

    IF v_solicitud_id IS NULL THEN
        INSERT INTO solicitud (id_solicitante, id_receptor, tipo, estado)
        VALUES (v_andrea_id, v_miguel_id, 'intercambio', 'finalizado')
        RETURNING id_solicitud INTO v_solicitud_id;
    ELSE
        UPDATE solicitud
           SET estado = 'finalizado'
         WHERE id_solicitud = v_solicitud_id;
    END IF;

    INSERT INTO detalle_solicitud (id_solicitud, id_libro, propietario)
    SELECT v_solicitud_id, v_libro_miguel, 'receptor'
    WHERE NOT EXISTS (
        SELECT 1 FROM detalle_solicitud
        WHERE id_solicitud = v_solicitud_id
          AND id_libro = v_libro_miguel
          AND propietario = 'receptor'
    );

    INSERT INTO detalle_solicitud (id_solicitud, id_libro, propietario)
    SELECT v_solicitud_id, v_libro_andrea, 'solicitante'
    WHERE NOT EXISTS (
        SELECT 1 FROM detalle_solicitud
        WHERE id_solicitud = v_solicitud_id
          AND id_libro = v_libro_andrea
          AND propietario = 'solicitante'
    );

    INSERT INTO encuentro (id_solicitud, latitud, longitud, direccion, fecha_hora, estado)
    SELECT
        v_solicitud_id,
        -6.771405,
        -79.840881,
        'Plaza de Armas de Chiclayo',
        NOW() - INTERVAL '2 day',
        'realizado'
    WHERE NOT EXISTS (
        SELECT 1 FROM encuentro WHERE id_solicitud = v_solicitud_id
    );

    UPDATE encuentro
       SET latitud = -6.771405,
           longitud = -79.840881,
           direccion = 'Plaza de Armas de Chiclayo',
           estado = 'realizado'
     WHERE id_solicitud = v_solicitud_id;

    UPDATE libro
       SET disponible = FALSE
     WHERE id_libro IN (v_libro_andrea, v_libro_miguel);

    INSERT INTO mensaje (id_solicitud, id_emisor, contenido)
    SELECT v_solicitud_id, v_andrea_id, 'Hola, te interesa intercambiar en Chiclayo?'
    WHERE NOT EXISTS (
        SELECT 1 FROM mensaje
        WHERE id_solicitud = v_solicitud_id
          AND id_emisor = v_andrea_id
          AND contenido = 'Hola, te interesa intercambiar en Chiclayo?'
    );

    INSERT INTO mensaje (id_solicitud, id_emisor, contenido)
    SELECT v_solicitud_id, v_miguel_id, 'Si, nos vemos en Plaza de Armas. Comparto punto: https://www.google.com/maps/search/?api=1&query=-6.771405,-79.840881'
    WHERE NOT EXISTS (
        SELECT 1 FROM mensaje
        WHERE id_solicitud = v_solicitud_id
          AND id_emisor = v_miguel_id
          AND contenido LIKE 'Si, nos vemos en Plaza de Armas.%'
    );

    INSERT INTO mensaje (id_solicitud, id_emisor, contenido)
    SELECT v_solicitud_id, v_andrea_id, 'Perfecto, ya realicé el intercambio. Gracias.'
    WHERE NOT EXISTS (
        SELECT 1 FROM mensaje
        WHERE id_solicitud = v_solicitud_id
          AND id_emisor = v_andrea_id
          AND contenido = 'Perfecto, ya realicé el intercambio. Gracias.'
    );
END $$;
