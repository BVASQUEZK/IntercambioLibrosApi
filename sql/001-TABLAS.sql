-- =========================================
-- BASE DE DATOS: PostgreSQL
-- Ejecutar conectado a la base destino (ej: defaultdb)
-- =========================================

DROP TABLE IF EXISTS mensaje CASCADE;
DROP TABLE IF EXISTS resena CASCADE;
DROP TABLE IF EXISTS encuentro CASCADE;
DROP TABLE IF EXISTS detalle_solicitud CASCADE;
DROP TABLE IF EXISTS solicitud CASCADE;
DROP TABLE IF EXISTS disponibilidad_libro CASCADE;
DROP TABLE IF EXISTS imagen_libro CASCADE;
DROP TABLE IF EXISTS libro CASCADE;
DROP TABLE IF EXISTS categoria CASCADE;
DROP TABLE IF EXISTS ubicacion CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;

-- =========================================
-- TABLA: usuario
-- =========================================
CREATE TABLE usuario (
    id_usuario SERIAL PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    dni VARCHAR(15) NOT NULL UNIQUE,
    correo VARCHAR(120) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    url_foto_perfil TEXT,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) NOT NULL DEFAULT 'activo',
    CONSTRAINT ck_usuario_estado CHECK (estado IN ('activo', 'suspendido'))
);

-- =========================================
-- TABLA: ubicacion
-- =========================================
CREATE TABLE ubicacion (
    id_ubicacion SERIAL PRIMARY KEY,
    id_usuario INT NOT NULL REFERENCES usuario(id_usuario),
    latitud NUMERIC(10, 8),
    longitud NUMERIC(11, 8),
    direccion VARCHAR(255),
    ciudad VARCHAR(100),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================
-- TABLA: categoria
-- =========================================
CREATE TABLE categoria (
    id_categoria SERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE
);

-- =========================================
-- TABLA: libro
-- =========================================
CREATE TABLE libro (
    id_libro SERIAL PRIMARY KEY,
    id_usuario INT NOT NULL REFERENCES usuario(id_usuario),
    id_categoria INT REFERENCES categoria(id_categoria),
    titulo VARCHAR(150) NOT NULL,
    autor VARCHAR(150),
    descripcion TEXT,
    estado VARCHAR(20),
    ubicacion VARCHAR(255),
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_libro_estado CHECK (estado IN ('nuevo', 'muy bueno', 'bueno', 'aceptable'))
);

-- =========================================
-- TABLA: imagen_libro
-- =========================================
CREATE TABLE imagen_libro (
    id_imagen SERIAL PRIMARY KEY,
    id_libro INT NOT NULL REFERENCES libro(id_libro),
    url_imagen VARCHAR(500)
);

-- =========================================
-- TABLA: disponibilidad_libro
-- =========================================
CREATE TABLE disponibilidad_libro (
    id_disponibilidad SERIAL PRIMARY KEY,
    id_libro INT NOT NULL REFERENCES libro(id_libro),
    tipo VARCHAR(20),
    precio_prestamo NUMERIC(8, 2) DEFAULT 0,
    tiempo_max_prestamo INT,
    CONSTRAINT ck_disponibilidad_tipo CHECK (tipo IN ('intercambio', 'prestamo', 'ambos'))
);

-- =========================================
-- TABLA: solicitud
-- =========================================
CREATE TABLE solicitud (
    id_solicitud SERIAL PRIMARY KEY,
    id_solicitante INT NOT NULL REFERENCES usuario(id_usuario),
    id_receptor INT NOT NULL REFERENCES usuario(id_usuario),
    tipo VARCHAR(20),
    estado VARCHAR(20) NOT NULL DEFAULT 'pendiente',
    fecha_solicitud TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_solicitud_tipo CHECK (tipo IN ('intercambio', 'prestamo')),
    CONSTRAINT ck_solicitud_estado CHECK (estado IN ('pendiente', 'aceptado', 'rechazado', 'finalizado', 'cancelado'))
);

-- =========================================
-- TABLA: detalle_solicitud
-- =========================================
CREATE TABLE detalle_solicitud (
    id_detalle SERIAL PRIMARY KEY,
    id_solicitud INT NOT NULL REFERENCES solicitud(id_solicitud),
    id_libro INT NOT NULL REFERENCES libro(id_libro),
    propietario VARCHAR(20),
    CONSTRAINT ck_detalle_propietario CHECK (propietario IN ('solicitante', 'receptor'))
);

-- =========================================
-- TABLA: encuentro
-- =========================================
CREATE TABLE encuentro (
    id_encuentro SERIAL PRIMARY KEY,
    id_solicitud INT NOT NULL REFERENCES solicitud(id_solicitud),
    latitud NUMERIC(10, 8),
    longitud NUMERIC(11, 8),
    direccion VARCHAR(255),
    fecha_hora TIMESTAMP,
    estado VARCHAR(20),
    CONSTRAINT ck_encuentro_estado CHECK (estado IN ('pendiente', 'realizado', 'cancelado'))
);

-- =========================================
-- TABLA: resena
-- =========================================
CREATE TABLE resena (
    id_resena SERIAL PRIMARY KEY,
    id_solicitud INT NOT NULL REFERENCES solicitud(id_solicitud),
    id_evaluador INT NOT NULL REFERENCES usuario(id_usuario),
    id_evaluado INT NOT NULL REFERENCES usuario(id_usuario),
    puntuacion INT CHECK (puntuacion BETWEEN 1 AND 5),
    comentario TEXT,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================
-- TABLA: mensaje
-- =========================================
CREATE TABLE mensaje (
    id_mensaje SERIAL PRIMARY KEY,
    id_solicitud INT NOT NULL REFERENCES solicitud(id_solicitud),
    id_emisor INT NOT NULL REFERENCES usuario(id_usuario),
    contenido TEXT,
    fecha_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    leido BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_mensaje_solicitud ON mensaje(id_solicitud);
CREATE INDEX idx_mensaje_emisor ON mensaje(id_emisor);
