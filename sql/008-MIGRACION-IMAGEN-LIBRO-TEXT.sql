-- ==========================================================
-- MIGRACION: permitir URLs/base64 largas en imagen_libro
-- ==========================================================

ALTER TABLE IF EXISTS imagen_libro
    ALTER COLUMN url_imagen TYPE TEXT;
