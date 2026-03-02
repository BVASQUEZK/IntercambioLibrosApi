-- ==========================================================
-- MIGRACION: libro con multiples categorias (N:N)
-- ==========================================================

CREATE TABLE IF NOT EXISTS libro_categoria (
    id_libro INT NOT NULL REFERENCES libro(id_libro) ON DELETE CASCADE,
    id_categoria INT NOT NULL REFERENCES categoria(id_categoria) ON DELETE CASCADE,
    PRIMARY KEY (id_libro, id_categoria)
);

INSERT INTO libro_categoria (id_libro, id_categoria)
SELECT l.id_libro, l.id_categoria
FROM libro l
WHERE l.id_categoria IS NOT NULL
ON CONFLICT DO NOTHING;
