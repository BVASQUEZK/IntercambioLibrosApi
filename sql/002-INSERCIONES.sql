-- =========================================
-- INSERCIONES BASE (POSTGRESQL)
-- =========================================

-- =========================================
-- CATEGORIAS
-- =========================================
INSERT INTO categoria (nombre) VALUES
('Terror'),
('Ciencia Ficción'),
('Romántico'),
('Fantasía'),
('Historia'),
('Realismo Mágico'),
('Novela')
ON CONFLICT (nombre) DO NOTHING;

-- =========================================
-- USUARIOS
-- =========================================
INSERT INTO usuario (nombres, apellidos, dni, correo, telefono, password, url_foto_perfil) VALUES
('Ana M.', 'García', '90010010', 'ana.m@demo.com', '600111222', 'demo123', 'https://cdn.example.com/perfiles/ana.jpg'),
('Carlos R.', 'López', '90010020', 'carlos.r@demo.com', '600333444', 'demo123', 'https://cdn.example.com/perfiles/carlos.jpg'),
('Lucía P.', 'Martín', '90010030', 'lucia.p@demo.com', '600555666', 'demo123', 'https://cdn.example.com/perfiles/lucia.jpg'),
('Diego S.', 'Ruiz', '90010040', 'diego.s@demo.com', '600777888', 'demo123', 'https://cdn.example.com/perfiles/diego.jpg'),
('María L.', 'Vega', '90010050', 'maria.l@demo.com', '600999000', 'demo123', 'https://cdn.example.com/perfiles/maria.jpg')
ON CONFLICT (correo) DO NOTHING;

-- =========================================
-- UBICACIONES
-- =========================================
INSERT INTO ubicacion (id_usuario, latitud, longitud, direccion, ciudad)
SELECT u.id_usuario, 40.416775, -3.703790, 'Calle Mayor 1', 'Madrid'
FROM usuario u
WHERE u.correo = 'ana.m@demo.com'
  AND NOT EXISTS (SELECT 1 FROM ubicacion x WHERE x.id_usuario = u.id_usuario);

INSERT INTO ubicacion (id_usuario, latitud, longitud, direccion, ciudad)
SELECT u.id_usuario, 41.387015, 2.170047, 'Carrer de la Boqueria 5', 'Barcelona'
FROM usuario u
WHERE u.correo = 'carlos.r@demo.com'
  AND NOT EXISTS (SELECT 1 FROM ubicacion x WHERE x.id_usuario = u.id_usuario);

INSERT INTO ubicacion (id_usuario, latitud, longitud, direccion, ciudad)
SELECT u.id_usuario, 39.469907, -0.376288, 'Carrer de la Pau 12', 'Valencia'
FROM usuario u
WHERE u.correo = 'lucia.p@demo.com'
  AND NOT EXISTS (SELECT 1 FROM ubicacion x WHERE x.id_usuario = u.id_usuario);

INSERT INTO ubicacion (id_usuario, latitud, longitud, direccion, ciudad)
SELECT u.id_usuario, 37.389092, -5.984459, 'Av. de la Constitución 20', 'Sevilla'
FROM usuario u
WHERE u.correo = 'diego.s@demo.com'
  AND NOT EXISTS (SELECT 1 FROM ubicacion x WHERE x.id_usuario = u.id_usuario);

INSERT INTO ubicacion (id_usuario, latitud, longitud, direccion, ciudad)
SELECT u.id_usuario, 43.362343, -8.411540, 'Calle Real 7', 'A Coruña'
FROM usuario u
WHERE u.correo = 'maria.l@demo.com'
  AND NOT EXISTS (SELECT 1 FROM ubicacion x WHERE x.id_usuario = u.id_usuario);

-- =========================================
-- LIBROS
-- =========================================
INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT u.id_usuario, c.id_categoria, 'Cien años de soledad', 'Gabriel García Márquez', 'Edición cuidada.', 'muy bueno', TRUE
FROM usuario u CROSS JOIN categoria c
WHERE u.correo = 'ana.m@demo.com' AND c.nombre = 'Realismo Mágico'
  AND NOT EXISTS (SELECT 1 FROM libro l WHERE l.id_usuario = u.id_usuario AND l.titulo = 'Cien años de soledad');

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT u.id_usuario, c.id_categoria, 'La sombra del viento', 'Carlos Ruiz Zafón', 'Ligeros signos de uso.', 'bueno', TRUE
FROM usuario u CROSS JOIN categoria c
WHERE u.correo = 'carlos.r@demo.com' AND c.nombre = 'Novela'
  AND NOT EXISTS (SELECT 1 FROM libro l WHERE l.id_usuario = u.id_usuario AND l.titulo = 'La sombra del viento');

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT u.id_usuario, c.id_categoria, 'El Alquimista', 'Paulo Coelho', 'Listo para intercambio.', 'bueno', TRUE
FROM usuario u CROSS JOIN categoria c
WHERE u.correo = 'lucia.p@demo.com' AND c.nombre = 'Romántico'
  AND NOT EXISTS (SELECT 1 FROM libro l WHERE l.id_usuario = u.id_usuario AND l.titulo = 'El Alquimista');

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT u.id_usuario, c.id_categoria, 'Rayuela', 'Julio Cortázar', 'Clásico en buen estado.', 'aceptable', TRUE
FROM usuario u CROSS JOIN categoria c
WHERE u.correo = 'lucia.p@demo.com' AND c.nombre = 'Novela'
  AND NOT EXISTS (SELECT 1 FROM libro l WHERE l.id_usuario = u.id_usuario AND l.titulo = 'Rayuela');

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT u.id_usuario, c.id_categoria, 'Sapiens', 'Yuval Noah Harari', 'Lectura imprescindible.', 'muy bueno', TRUE
FROM usuario u CROSS JOIN categoria c
WHERE u.correo = 'diego.s@demo.com' AND c.nombre = 'Historia'
  AND NOT EXISTS (SELECT 1 FROM libro l WHERE l.id_usuario = u.id_usuario AND l.titulo = 'Sapiens');

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT u.id_usuario, c.id_categoria, 'Breve historia del tiempo', 'Stephen Hawking', 'Excelente estado.', 'muy bueno', TRUE
FROM usuario u CROSS JOIN categoria c
WHERE u.correo = 'diego.s@demo.com' AND c.nombre = 'Ciencia Ficción'
  AND NOT EXISTS (SELECT 1 FROM libro l WHERE l.id_usuario = u.id_usuario AND l.titulo = 'Breve historia del tiempo');

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT u.id_usuario, c.id_categoria, 'El nombre del viento', 'Patrick Rothfuss', 'Como nuevo.', 'nuevo', TRUE
FROM usuario u CROSS JOIN categoria c
WHERE u.correo = 'maria.l@demo.com' AND c.nombre = 'Fantasía'
  AND NOT EXISTS (SELECT 1 FROM libro l WHERE l.id_usuario = u.id_usuario AND l.titulo = 'El nombre del viento');

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT u.id_usuario, c.id_categoria, '1984', 'George Orwell', 'Buen estado.', 'bueno', TRUE
FROM usuario u CROSS JOIN categoria c
WHERE u.correo = 'carlos.r@demo.com' AND c.nombre = 'Ciencia Ficción'
  AND NOT EXISTS (SELECT 1 FROM libro l WHERE l.id_usuario = u.id_usuario AND l.titulo = '1984');

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT u.id_usuario, c.id_categoria, 'Drácula', 'Bram Stoker', 'Edición clásica.', 'aceptable', TRUE
FROM usuario u CROSS JOIN categoria c
WHERE u.correo = 'ana.m@demo.com' AND c.nombre = 'Terror'
  AND NOT EXISTS (SELECT 1 FROM libro l WHERE l.id_usuario = u.id_usuario AND l.titulo = 'Drácula');

INSERT INTO libro (id_usuario, id_categoria, titulo, autor, descripcion, estado, disponible)
SELECT u.id_usuario, c.id_categoria, 'El Hobbit', 'J.R.R. Tolkien', 'Portada nueva.', 'muy bueno', TRUE
FROM usuario u CROSS JOIN categoria c
WHERE u.correo = 'maria.l@demo.com' AND c.nombre = 'Fantasía'
  AND NOT EXISTS (SELECT 1 FROM libro l WHERE l.id_usuario = u.id_usuario AND l.titulo = 'El Hobbit');

-- =========================================
-- IMAGENES DE LIBROS
-- =========================================
INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT l.id_libro, 'https://cdn.example.com/libros/cien_anos.jpg'
FROM libro l
WHERE l.titulo = 'Cien años de soledad'
  AND NOT EXISTS (SELECT 1 FROM imagen_libro i WHERE i.id_libro = l.id_libro AND i.url_imagen = 'https://cdn.example.com/libros/cien_anos.jpg');

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT l.id_libro, 'https://cdn.example.com/libros/sombra_viento.jpg'
FROM libro l
WHERE l.titulo = 'La sombra del viento'
  AND NOT EXISTS (SELECT 1 FROM imagen_libro i WHERE i.id_libro = l.id_libro AND i.url_imagen = 'https://cdn.example.com/libros/sombra_viento.jpg');

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT l.id_libro, 'https://cdn.example.com/libros/el_alquimista.jpg'
FROM libro l
WHERE l.titulo = 'El Alquimista'
  AND NOT EXISTS (SELECT 1 FROM imagen_libro i WHERE i.id_libro = l.id_libro AND i.url_imagen = 'https://cdn.example.com/libros/el_alquimista.jpg');

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT l.id_libro, 'https://cdn.example.com/libros/rayuela.jpg'
FROM libro l
WHERE l.titulo = 'Rayuela'
  AND NOT EXISTS (SELECT 1 FROM imagen_libro i WHERE i.id_libro = l.id_libro AND i.url_imagen = 'https://cdn.example.com/libros/rayuela.jpg');

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT l.id_libro, 'https://cdn.example.com/libros/sapiens.jpg'
FROM libro l
WHERE l.titulo = 'Sapiens'
  AND NOT EXISTS (SELECT 1 FROM imagen_libro i WHERE i.id_libro = l.id_libro AND i.url_imagen = 'https://cdn.example.com/libros/sapiens.jpg');

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT l.id_libro, 'https://cdn.example.com/libros/breve_historia_tiempo.jpg'
FROM libro l
WHERE l.titulo = 'Breve historia del tiempo'
  AND NOT EXISTS (SELECT 1 FROM imagen_libro i WHERE i.id_libro = l.id_libro AND i.url_imagen = 'https://cdn.example.com/libros/breve_historia_tiempo.jpg');

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT l.id_libro, 'https://cdn.example.com/libros/nombre_viento.jpg'
FROM libro l
WHERE l.titulo = 'El nombre del viento'
  AND NOT EXISTS (SELECT 1 FROM imagen_libro i WHERE i.id_libro = l.id_libro AND i.url_imagen = 'https://cdn.example.com/libros/nombre_viento.jpg');

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT l.id_libro, 'https://cdn.example.com/libros/1984.jpg'
FROM libro l
WHERE l.titulo = '1984'
  AND NOT EXISTS (SELECT 1 FROM imagen_libro i WHERE i.id_libro = l.id_libro AND i.url_imagen = 'https://cdn.example.com/libros/1984.jpg');

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT l.id_libro, 'https://cdn.example.com/libros/dracula.jpg'
FROM libro l
WHERE l.titulo = 'Drácula'
  AND NOT EXISTS (SELECT 1 FROM imagen_libro i WHERE i.id_libro = l.id_libro AND i.url_imagen = 'https://cdn.example.com/libros/dracula.jpg');

INSERT INTO imagen_libro (id_libro, url_imagen)
SELECT l.id_libro, 'https://cdn.example.com/libros/hobbit.jpg'
FROM libro l
WHERE l.titulo = 'El Hobbit'
  AND NOT EXISTS (SELECT 1 FROM imagen_libro i WHERE i.id_libro = l.id_libro AND i.url_imagen = 'https://cdn.example.com/libros/hobbit.jpg');
