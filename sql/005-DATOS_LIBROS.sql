-- =========================================
-- DATOS DE PRUEBA ADICIONALES (POSTGRESQL)
-- =========================================

-- Categorias base
INSERT INTO categoria (nombre) VALUES
('Terror'),
('Ingenieria'),
('Novela'),
('Ciencia'),
('Historia'),
('Fantasia'),
('Tecnologia'),
('Arte'),
('Negocios'),
('Educacion')
ON CONFLICT (nombre) DO NOTHING;

-- Usuarios base
INSERT INTO usuario (nombres, apellidos, dni, correo, telefono, password, url_foto_perfil)
VALUES
('Ana', 'Martinez', '70000001', 'ana@demo.com', '900000001', 'demo123', NULL),
('Carlos', 'Rojas', '70000002', 'carlos.r@demo.com', '900000002', 'demo123', NULL),
('Luis', 'Paredes', '70000003', 'luis@demo.com', '900000003', 'demo123', NULL),
('Maria', 'Lopez', '70000004', 'maria@demo.com', '900000004', 'demo123', NULL)
ON CONFLICT (correo) DO NOTHING;

-- Libros de prueba
INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT
    (SELECT id_usuario FROM usuario WHERE correo = 'ana@demo.com'),
    (SELECT id_categoria FROM categoria WHERE nombre = 'Terror'),
    'La Casa en la Niebla', 'R. Black', 'Thriller clasico de misterio y suspenso.', 'bueno', TRUE
WHERE NOT EXISTS (SELECT 1 FROM libro WHERE titulo = 'La Casa en la Niebla');

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT
    (SELECT id_usuario FROM usuario WHERE correo = 'carlos.r@demo.com'),
    (SELECT id_categoria FROM categoria WHERE nombre = 'Ingenieria'),
    'Fundamentos de Estructuras', 'J. Moreno', 'Guia practica con ejemplos y ejercicios.', 'muy bueno', TRUE
WHERE NOT EXISTS (SELECT 1 FROM libro WHERE titulo = 'Fundamentos de Estructuras');

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT
    (SELECT id_usuario FROM usuario WHERE correo = 'luis@demo.com'),
    (SELECT id_categoria FROM categoria WHERE nombre = 'Novela'),
    'El Rio de la Memoria', 'S. Alvarez', 'Novela contemporanea de drama familiar.', 'aceptable', TRUE
WHERE NOT EXISTS (SELECT 1 FROM libro WHERE titulo = 'El Rio de la Memoria');

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT
    (SELECT id_usuario FROM usuario WHERE correo = 'maria@demo.com'),
    (SELECT id_categoria FROM categoria WHERE nombre = 'Ciencia'),
    'Fisica en 100 Ideas', 'A. Vega', 'Conceptos esenciales explicados de forma simple.', 'nuevo', TRUE
WHERE NOT EXISTS (SELECT 1 FROM libro WHERE titulo = 'Fisica en 100 Ideas');

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT
    (SELECT id_usuario FROM usuario WHERE correo = 'ana@demo.com'),
    (SELECT id_categoria FROM categoria WHERE nombre = 'Historia'),
    'Imperios del Ande', 'L. Quispe', 'Historia regional con fuentes y cronologias.', 'muy bueno', TRUE
WHERE NOT EXISTS (SELECT 1 FROM libro WHERE titulo = 'Imperios del Ande');

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT
    (SELECT id_usuario FROM usuario WHERE correo = 'carlos.r@demo.com'),
    (SELECT id_categoria FROM categoria WHERE nombre = 'Fantasia'),
    'Las Cronicas de Rhen', 'M. Stone', 'Saga de fantasia con reinos y magia.', 'bueno', TRUE
WHERE NOT EXISTS (SELECT 1 FROM libro WHERE titulo = 'Las Cronicas de Rhen');

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT
    (SELECT id_usuario FROM usuario WHERE correo = 'luis@demo.com'),
    (SELECT id_categoria FROM categoria WHERE nombre = 'Tecnologia'),
    'Arquitectura de Software', 'P. Diaz', 'Patrones y decisiones de diseno para sistemas.', 'nuevo', TRUE
WHERE NOT EXISTS (SELECT 1 FROM libro WHERE titulo = 'Arquitectura de Software');

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT
    (SELECT id_usuario FROM usuario WHERE correo = 'maria@demo.com'),
    (SELECT id_categoria FROM categoria WHERE nombre = 'Arte'),
    'Color y Composicion', 'E. Torres', 'Principios visuales para ilustracion.', 'bueno', TRUE
WHERE NOT EXISTS (SELECT 1 FROM libro WHERE titulo = 'Color y Composicion');

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT
    (SELECT id_usuario FROM usuario WHERE correo = 'ana@demo.com'),
    (SELECT id_categoria FROM categoria WHERE nombre = 'Negocios'),
    'Estrategia en Accion', 'V. Ramos', 'Marco estrategico aplicado a pymes.', 'aceptable', TRUE
WHERE NOT EXISTS (SELECT 1 FROM libro WHERE titulo = 'Estrategia en Accion');

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT
    (SELECT id_usuario FROM usuario WHERE correo = 'carlos.r@demo.com'),
    (SELECT id_categoria FROM categoria WHERE nombre = 'Educacion'),
    'Didactica Moderna', 'C. Herrera', 'Metodos activos y evaluacion formativa.', 'muy bueno', TRUE
WHERE NOT EXISTS (SELECT 1 FROM libro WHERE titulo = 'Didactica Moderna');

-- Imagenes de portada
INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT (SELECT id_libro FROM libro WHERE titulo = 'La Casa en la Niebla'), 'https://picsum.photos/seed/libro1/600/900'
WHERE NOT EXISTS (
    SELECT 1 FROM imagen_libro i
    JOIN libro l ON l.id_libro = i.id_libro
    WHERE l.titulo = 'La Casa en la Niebla' AND i.url_imagen = 'https://picsum.photos/seed/libro1/600/900'
);

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT (SELECT id_libro FROM libro WHERE titulo = 'Fundamentos de Estructuras'), 'https://picsum.photos/seed/libro2/600/900'
WHERE NOT EXISTS (
    SELECT 1 FROM imagen_libro i
    JOIN libro l ON l.id_libro = i.id_libro
    WHERE l.titulo = 'Fundamentos de Estructuras' AND i.url_imagen = 'https://picsum.photos/seed/libro2/600/900'
);

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT (SELECT id_libro FROM libro WHERE titulo = 'El Rio de la Memoria'), 'https://picsum.photos/seed/libro3/600/900'
WHERE NOT EXISTS (
    SELECT 1 FROM imagen_libro i
    JOIN libro l ON l.id_libro = i.id_libro
    WHERE l.titulo = 'El Rio de la Memoria' AND i.url_imagen = 'https://picsum.photos/seed/libro3/600/900'
);

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT (SELECT id_libro FROM libro WHERE titulo = 'Fisica en 100 Ideas'), 'https://picsum.photos/seed/libro4/600/900'
WHERE NOT EXISTS (
    SELECT 1 FROM imagen_libro i
    JOIN libro l ON l.id_libro = i.id_libro
    WHERE l.titulo = 'Fisica en 100 Ideas' AND i.url_imagen = 'https://picsum.photos/seed/libro4/600/900'
);

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT (SELECT id_libro FROM libro WHERE titulo = 'Imperios del Ande'), 'https://picsum.photos/seed/libro5/600/900'
WHERE NOT EXISTS (
    SELECT 1 FROM imagen_libro i
    JOIN libro l ON l.id_libro = i.id_libro
    WHERE l.titulo = 'Imperios del Ande' AND i.url_imagen = 'https://picsum.photos/seed/libro5/600/900'
);

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT (SELECT id_libro FROM libro WHERE titulo = 'Las Cronicas de Rhen'), 'https://picsum.photos/seed/libro6/600/900'
WHERE NOT EXISTS (
    SELECT 1 FROM imagen_libro i
    JOIN libro l ON l.id_libro = i.id_libro
    WHERE l.titulo = 'Las Cronicas de Rhen' AND i.url_imagen = 'https://picsum.photos/seed/libro6/600/900'
);

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT (SELECT id_libro FROM libro WHERE titulo = 'Arquitectura de Software'), 'https://picsum.photos/seed/libro7/600/900'
WHERE NOT EXISTS (
    SELECT 1 FROM imagen_libro i
    JOIN libro l ON l.id_libro = i.id_libro
    WHERE l.titulo = 'Arquitectura de Software' AND i.url_imagen = 'https://picsum.photos/seed/libro7/600/900'
);

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT (SELECT id_libro FROM libro WHERE titulo = 'Color y Composicion'), 'https://picsum.photos/seed/libro8/600/900'
WHERE NOT EXISTS (
    SELECT 1 FROM imagen_libro i
    JOIN libro l ON l.id_libro = i.id_libro
    WHERE l.titulo = 'Color y Composicion' AND i.url_imagen = 'https://picsum.photos/seed/libro8/600/900'
);

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT (SELECT id_libro FROM libro WHERE titulo = 'Estrategia en Accion'), 'https://picsum.photos/seed/libro9/600/900'
WHERE NOT EXISTS (
    SELECT 1 FROM imagen_libro i
    JOIN libro l ON l.id_libro = i.id_libro
    WHERE l.titulo = 'Estrategia en Accion' AND i.url_imagen = 'https://picsum.photos/seed/libro9/600/900'
);

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT (SELECT id_libro FROM libro WHERE titulo = 'Didactica Moderna'), 'https://picsum.photos/seed/libro10/600/900'
WHERE NOT EXISTS (
    SELECT 1 FROM imagen_libro i
    JOIN libro l ON l.id_libro = i.id_libro
    WHERE l.titulo = 'Didactica Moderna' AND i.url_imagen = 'https://picsum.photos/seed/libro10/600/900'
);
