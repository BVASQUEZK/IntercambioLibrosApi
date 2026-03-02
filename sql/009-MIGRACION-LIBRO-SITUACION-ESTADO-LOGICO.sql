-- =========================================
-- MIGRACION: libro.situacion + libro.estado_logico + eliminado logico
-- =========================================

ALTER TABLE libro
    ADD COLUMN IF NOT EXISTS situacion VARCHAR(30),
    ADD COLUMN IF NOT EXISTS estado_logico VARCHAR(20);

UPDATE libro
SET situacion = CASE
    WHEN COALESCE(disponible, TRUE) THEN 'disponible'
    ELSE 'ocupado'
END
WHERE situacion IS NULL OR TRIM(situacion) = '';

UPDATE libro
SET estado_logico = 'activo'
WHERE estado_logico IS NULL OR TRIM(estado_logico) = '';

ALTER TABLE libro
    ALTER COLUMN situacion SET DEFAULT 'disponible',
    ALTER COLUMN estado_logico SET DEFAULT 'activo';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_libro_situacion'
    ) THEN
        ALTER TABLE libro
            ADD CONSTRAINT ck_libro_situacion CHECK (situacion IN ('disponible', 'ocupado'));
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_libro_estado_logico'
    ) THEN
        ALTER TABLE libro
            ADD CONSTRAINT ck_libro_estado_logico CHECK (estado_logico IN ('activo', 'inactivo'));
    END IF;
END $$;

