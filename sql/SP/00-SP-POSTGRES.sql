-- =========================================
-- STORED FUNCTIONS/PROCEDURES (POSTGRESQL)
-- =========================================

DROP FUNCTION IF EXISTS sp_login_usuario_app(VARCHAR, VARCHAR);
CREATE OR REPLACE FUNCTION sp_login_usuario_app(
    p_correo VARCHAR,
    p_password VARCHAR
)
RETURNS TABLE (
    idUsuario INT,
    nombres VARCHAR,
    apellidos VARCHAR,
    correo VARCHAR,
    password VARCHAR,
    estado VARCHAR
)
LANGUAGE sql
AS $$
    SELECT u.id_usuario AS idUsuario,
           u.nombres,
           u.apellidos,
           u.correo,
           u.password,
           u.estado
    FROM usuario u
    WHERE u.correo = p_correo
      AND u.password = p_password;
$$;

DROP FUNCTION IF EXISTS sp_registrar_usuario_app(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR);
CREATE OR REPLACE FUNCTION sp_registrar_usuario_app(
    p_nombres VARCHAR,
    p_apellidos VARCHAR,
    p_correo VARCHAR,
    p_password VARCHAR,
    p_dni VARCHAR
)
RETURNS TABLE (id_usuario INT)
LANGUAGE sql
AS $$
    INSERT INTO usuario (nombres, apellidos, correo, password, dni)
    VALUES (p_nombres, p_apellidos, p_correo, p_password, p_dni)
    RETURNING usuario.id_usuario;
$$;

DROP PROCEDURE IF EXISTS sp_actualizar_perfil(INT, VARCHAR, VARCHAR, VARCHAR);
CREATE OR REPLACE PROCEDURE sp_actualizar_perfil(
    p_id_usuario INT,
    p_nombres VARCHAR,
    p_apellidos VARCHAR,
    p_url_foto VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE usuario
    SET nombres = p_nombres,
        apellidos = p_apellidos,
        url_foto_perfil = p_url_foto
    WHERE id_usuario = p_id_usuario;
END;
$$;

DROP FUNCTION IF EXISTS sp_listar_categorias();
CREATE OR REPLACE FUNCTION sp_listar_categorias()
RETURNS TABLE (
    id_categoria INT,
    nombre VARCHAR
)
LANGUAGE sql
AS $$
    SELECT c.id_categoria, c.nombre
    FROM categoria c
    ORDER BY c.nombre ASC;
$$;

DROP FUNCTION IF EXISTS sp_registrar_libro(INT, INT, VARCHAR, VARCHAR, TEXT, VARCHAR, VARCHAR, VARCHAR);
CREATE OR REPLACE FUNCTION sp_registrar_libro(
    p_id_usuario INT,
    p_id_categoria INT,
    p_titulo VARCHAR,
    p_autor VARCHAR,
    p_descripcion TEXT,
    p_estado VARCHAR,
    p_ubicacion VARCHAR,
    p_url_imagen VARCHAR
)
RETURNS INT
LANGUAGE plpgsql
AS $$
DECLARE
    v_id_libro INT;
BEGIN
    INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, ubicacion, disponible)
    VALUES (p_id_usuario, p_id_categoria, p_titulo, p_autor, p_descripcion, p_estado, p_ubicacion, TRUE)
    RETURNING id_libro INTO v_id_libro;

    IF p_url_imagen IS NOT NULL AND LENGTH(TRIM(p_url_imagen)) > 0 THEN
        INSERT INTO imagen_libro (id_libro, url_imagen)
        VALUES (v_id_libro, p_url_imagen);
    END IF;

    RETURN v_id_libro;
END;
$$;

DROP FUNCTION IF EXISTS sp_listar_libros_recientes(INT, INT);
CREATE OR REPLACE FUNCTION sp_listar_libros_recientes(
    p_limit INT,
    p_offset INT
)
RETURNS TABLE (
    id_libro INT,
    titulo VARCHAR,
    autor VARCHAR,
    descripcion TEXT,
    nombre_categoria VARCHAR,
    nombre_usuario_propietario TEXT,
    url_portada VARCHAR
)
LANGUAGE sql
AS $$
    SELECT l.id_libro,
           l.titulo,
           l.autor,
           l.descripcion,
           c.nombre AS nombre_categoria,
           CONCAT(u.nombres, ' ', u.apellidos) AS nombre_usuario_propietario,
           img.url_imagen AS url_portada
    FROM libro l
    INNER JOIN usuario u ON u.id_usuario = l.id_usuario
    LEFT JOIN categoria c ON c.id_categoria = l.id_categoria
    LEFT JOIN (
        SELECT i.id_libro, MIN(i.id_imagen) AS id_imagen
        FROM imagen_libro i
        GROUP BY i.id_libro
    ) img_min ON img_min.id_libro = l.id_libro
    LEFT JOIN imagen_libro img ON img.id_imagen = img_min.id_imagen
    ORDER BY l.fecha_registro DESC
    LIMIT p_limit OFFSET p_offset;
$$;

DROP FUNCTION IF EXISTS sp_buscar_libros_filtros(VARCHAR, VARCHAR, INT, VARCHAR, INT, INT);
CREATE OR REPLACE FUNCTION sp_buscar_libros_filtros(
    p_titulo VARCHAR,
    p_autor VARCHAR,
    p_id_categoria INT,
    p_estado VARCHAR,
    p_limit INT,
    p_offset INT
)
RETURNS TABLE (
    id_libro INT,
    titulo VARCHAR,
    autor VARCHAR,
    descripcion TEXT,
    nombre_categoria VARCHAR,
    nombre_usuario_propietario TEXT,
    url_portada VARCHAR
)
LANGUAGE sql
AS $$
    SELECT l.id_libro,
           l.titulo,
           l.autor,
           l.descripcion,
           c.nombre AS nombre_categoria,
           CONCAT(u.nombres, ' ', u.apellidos) AS nombre_usuario_propietario,
           img.url_imagen AS url_portada
    FROM libro l
    INNER JOIN usuario u ON u.id_usuario = l.id_usuario
    LEFT JOIN categoria c ON c.id_categoria = l.id_categoria
    LEFT JOIN (
        SELECT i.id_libro, MIN(i.id_imagen) AS id_imagen
        FROM imagen_libro i
        GROUP BY i.id_libro
    ) img_min ON img_min.id_libro = l.id_libro
    LEFT JOIN imagen_libro img ON img.id_imagen = img_min.id_imagen
    WHERE (p_titulo IS NULL OR l.titulo ILIKE CONCAT('%', p_titulo, '%'))
      AND (p_autor IS NULL OR l.autor ILIKE CONCAT('%', p_autor, '%'))
      AND (p_id_categoria IS NULL OR l.id_categoria = p_id_categoria)
      AND (p_estado IS NULL OR l.estado = p_estado)
    ORDER BY l.fecha_registro DESC
    LIMIT p_limit OFFSET p_offset;
$$;

DROP PROCEDURE IF EXISTS sp_vincular_imagen_libro(INT, VARCHAR);
CREATE OR REPLACE PROCEDURE sp_vincular_imagen_libro(
    p_id_libro INT,
    p_url_imagen VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO imagen_libro (id_libro, url_imagen)
    VALUES (p_id_libro, p_url_imagen);
END;
$$;

DROP FUNCTION IF EXISTS sp_crear_solicitud(INT, INT, VARCHAR);
CREATE OR REPLACE FUNCTION sp_crear_solicitud(
    p_id_solicitante INT,
    p_id_receptor INT,
    p_tipo VARCHAR
)
RETURNS INT
LANGUAGE sql
AS $$
    INSERT INTO solicitud (id_solicitante, id_receptor, tipo, estado)
    VALUES (p_id_solicitante, p_id_receptor, p_tipo, 'pendiente')
    RETURNING solicitud.id_solicitud;
$$;

DROP FUNCTION IF EXISTS sp_crear_solicitud_intercambio(INT, INT, TEXT);
CREATE OR REPLACE FUNCTION sp_crear_solicitud_intercambio(
    p_id_usuario_solicitante INT,
    p_id_libro_interesado INT,
    p_mensaje_propuesta TEXT
)
RETURNS INT
LANGUAGE plpgsql
AS $$
DECLARE
    v_id_receptor INT;
    v_id_solicitud INT;
BEGIN
    SELECT l.id_usuario
    INTO v_id_receptor
    FROM libro l
    WHERE l.id_libro = p_id_libro_interesado;

    INSERT INTO solicitud (id_solicitante, id_receptor, tipo, estado)
    VALUES (p_id_usuario_solicitante, v_id_receptor, 'intercambio', 'pendiente')
    RETURNING id_solicitud INTO v_id_solicitud;

    INSERT INTO detalle_solicitud (id_solicitud, id_libro, propietario)
    VALUES (v_id_solicitud, p_id_libro_interesado, 'receptor');

    IF p_mensaje_propuesta IS NOT NULL AND LENGTH(TRIM(p_mensaje_propuesta)) > 0 THEN
        INSERT INTO mensaje (id_solicitud, id_emisor, contenido)
        VALUES (v_id_solicitud, p_id_usuario_solicitante, p_mensaje_propuesta);
    END IF;

    RETURN v_id_solicitud;
END;
$$;

DROP FUNCTION IF EXISTS sp_listar_solicitudes_usuario(INT, VARCHAR);
CREATE OR REPLACE FUNCTION sp_listar_solicitudes_usuario(
    p_id_usuario INT,
    p_tipo VARCHAR
)
RETURNS TABLE (
    id_solicitud INT,
    titulo_libro VARCHAR,
    nombre_otra_parte TEXT,
    estado VARCHAR,
    fecha_solicitud TIMESTAMP
)
LANGUAGE sql
AS $$
    SELECT s.id_solicitud,
           l.titulo AS titulo_libro,
           CASE
               WHEN p_tipo = 'RECIBIDAS' THEN CONCAT(us.nombres, ' ', us.apellidos)
               ELSE CONCAT(ur.nombres, ' ', ur.apellidos)
           END AS nombre_otra_parte,
           s.estado,
           s.fecha_solicitud
    FROM solicitud s
    INNER JOIN detalle_solicitud ds ON ds.id_solicitud = s.id_solicitud AND ds.propietario = 'receptor'
    INNER JOIN libro l ON l.id_libro = ds.id_libro
    INNER JOIN usuario us ON us.id_usuario = s.id_solicitante
    INNER JOIN usuario ur ON ur.id_usuario = s.id_receptor
    WHERE (p_tipo = 'RECIBIDAS' AND s.id_receptor = p_id_usuario)
       OR (p_tipo = 'ENVIADAS' AND s.id_solicitante = p_id_usuario)
    ORDER BY s.fecha_solicitud DESC;
$$;

DROP PROCEDURE IF EXISTS sp_responder_solicitud(INT, VARCHAR, TEXT);
CREATE OR REPLACE PROCEDURE sp_responder_solicitud(
    p_id_solicitud INT,
    p_nuevo_estado VARCHAR,
    p_comentario TEXT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_estado VARCHAR(20);
    v_id_receptor INT;
BEGIN
    v_estado := CASE
        WHEN UPPER(p_nuevo_estado) = 'ACEPTADA' THEN 'aceptado'
        WHEN UPPER(p_nuevo_estado) = 'RECHAZADA' THEN 'rechazado'
        ELSE 'pendiente'
    END;

    UPDATE solicitud
    SET estado = v_estado
    WHERE id_solicitud = p_id_solicitud;

    IF v_estado = 'aceptado' THEN
        UPDATE libro l
        SET disponible = FALSE
        FROM detalle_solicitud ds
        WHERE ds.id_solicitud = p_id_solicitud
          AND ds.propietario = 'receptor'
          AND ds.id_libro = l.id_libro;
    END IF;

    IF p_comentario IS NOT NULL AND LENGTH(TRIM(p_comentario)) > 0 THEN
        SELECT s.id_receptor
        INTO v_id_receptor
        FROM solicitud s
        WHERE s.id_solicitud = p_id_solicitud;

        INSERT INTO mensaje (id_solicitud, id_emisor, contenido)
        VALUES (p_id_solicitud, v_id_receptor, p_comentario);
    END IF;
END;
$$;

DROP PROCEDURE IF EXISTS sp_actualizar_estado_solicitud(INT, VARCHAR);
CREATE OR REPLACE PROCEDURE sp_actualizar_estado_solicitud(
    p_id_solicitud INT,
    p_estado VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE solicitud
    SET estado = p_estado
    WHERE id_solicitud = p_id_solicitud;
END;
$$;

DROP FUNCTION IF EXISTS sp_obtener_valoracion_usuario(INT);
CREATE OR REPLACE FUNCTION sp_obtener_valoracion_usuario(
    p_id_usuario INT
)
RETURNS DOUBLE PRECISION
LANGUAGE sql
AS $$
    SELECT COALESCE(AVG(r.puntuacion), 0)::DOUBLE PRECISION
    FROM resena r
    WHERE r.id_evaluado = p_id_usuario;
$$;

DROP PROCEDURE IF EXISTS sp_enviar_mensaje(INT, INT, TEXT);
CREATE OR REPLACE PROCEDURE sp_enviar_mensaje(
    p_id_solicitud INT,
    p_id_emisor INT,
    p_contenido TEXT
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO mensaje (id_solicitud, id_emisor, contenido)
    VALUES (p_id_solicitud, p_id_emisor, p_contenido);
END;
$$;

DROP FUNCTION IF EXISTS sp_listar_mensajes_solicitud(INT);
CREATE OR REPLACE FUNCTION sp_listar_mensajes_solicitud(
    p_id_solicitud INT
)
RETURNS TABLE (
    id_mensaje INT,
    id_solicitud INT,
    id_emisor INT,
    contenido TEXT,
    fecha_envio TIMESTAMP,
    leido BOOLEAN,
    nombres VARCHAR,
    apellidos VARCHAR,
    url_foto_perfil VARCHAR
)
LANGUAGE sql
AS $$
    SELECT m.id_mensaje,
           m.id_solicitud,
           m.id_emisor,
           m.contenido,
           m.fecha_envio,
           m.leido,
           u.nombres,
           u.apellidos,
           u.url_foto_perfil
    FROM mensaje m
    INNER JOIN usuario u ON u.id_usuario = m.id_emisor
    WHERE m.id_solicitud = p_id_solicitud
    ORDER BY m.fecha_envio ASC;
$$;

DROP FUNCTION IF EXISTS sp_obtener_mensajes_intercambio(INT, INT);
CREATE OR REPLACE FUNCTION sp_obtener_mensajes_intercambio(
    p_id_solicitud INT,
    p_id_usuario INT
)
RETURNS TABLE (
    emisor_nombre TEXT,
    contenido_mensaje TEXT,
    fecha_envio TIMESTAMP,
    es_mio BOOLEAN
)
LANGUAGE sql
AS $$
    SELECT CONCAT(u.nombres, ' ', u.apellidos) AS emisor_nombre,
           m.contenido AS contenido_mensaje,
           m.fecha_envio,
           (m.id_emisor = p_id_usuario) AS es_mio
    FROM mensaje m
    INNER JOIN usuario u ON u.id_usuario = m.id_emisor
    WHERE m.id_solicitud = p_id_solicitud
    ORDER BY m.fecha_envio ASC;
$$;
