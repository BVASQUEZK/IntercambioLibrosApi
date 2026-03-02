-- =========================================
-- MIGRACION: libro.estado (activo/inactivo) + libro.condicion + libro.situacion
-- =========================================

ALTER TABLE libro
    ADD COLUMN IF NOT EXISTS condicion VARCHAR(30);

-- 0) Eliminar checks antiguos antes de normalizar datos.
ALTER TABLE libro DROP CONSTRAINT IF EXISTS ck_libro_estado;
ALTER TABLE libro DROP CONSTRAINT IF EXISTS ck_libro_condicion;
ALTER TABLE libro DROP CONSTRAINT IF EXISTS ck_libro_situacion;
ALTER TABLE libro DROP CONSTRAINT IF EXISTS ck_libro_estado_logico;

-- 1) Condicion: conservar valores historicos de "estado" (esquema anterior).
UPDATE libro
SET condicion = CASE
    WHEN LOWER(TRIM(COALESCE(estado, ''))) = 'regular' THEN 'aceptable'
    WHEN LOWER(TRIM(COALESCE(estado, ''))) IN ('nuevo', 'como nuevo', 'muy bueno', 'bueno', 'aceptable')
        THEN LOWER(TRIM(estado))
    ELSE 'bueno'
END
WHERE condicion IS NULL OR TRIM(condicion) = '';

-- 2) Estado: usar estado actual si ya es activo/inactivo; de lo contrario
-- tomar estado_logico cuando exista.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'libro'
          AND column_name = 'estado_logico'
    ) THEN
        UPDATE libro
        SET estado = CASE
            WHEN LOWER(TRIM(COALESCE(estado, ''))) IN ('activo', 'inactivo')
                THEN LOWER(TRIM(estado))
            WHEN LOWER(TRIM(COALESCE(estado_logico, ''))) IN ('activo', 'inactivo')
                THEN LOWER(TRIM(estado_logico))
            ELSE 'activo'
        END
        WHERE estado IS NULL
           OR TRIM(estado) = ''
           OR LOWER(TRIM(estado)) NOT IN ('activo', 'inactivo');
    ELSE
        UPDATE libro
        SET estado = CASE
            WHEN LOWER(TRIM(COALESCE(estado, ''))) IN ('activo', 'inactivo')
                THEN LOWER(TRIM(estado))
            ELSE 'activo'
        END
        WHERE estado IS NULL
           OR TRIM(estado) = ''
           OR LOWER(TRIM(estado)) NOT IN ('activo', 'inactivo');
    END IF;
END $$;

-- 3) Situacion: forzar dominio esperado.
UPDATE libro
SET situacion = CASE
    WHEN LOWER(TRIM(COALESCE(situacion, ''))) IN ('disponible', 'ocupado')
        THEN LOWER(TRIM(situacion))
    WHEN COALESCE(disponible, TRUE) THEN 'disponible'
    ELSE 'ocupado'
END
WHERE situacion IS NULL
   OR TRIM(situacion) = ''
   OR LOWER(TRIM(situacion)) NOT IN ('disponible', 'ocupado');

-- 4) Defaults y nulabilidad.
ALTER TABLE libro
    ALTER COLUMN estado SET DEFAULT 'activo',
    ALTER COLUMN condicion SET DEFAULT 'bueno',
    ALTER COLUMN situacion SET DEFAULT 'disponible';

ALTER TABLE libro
    ALTER COLUMN estado SET NOT NULL,
    ALTER COLUMN condicion SET NOT NULL,
    ALTER COLUMN situacion SET NOT NULL;

-- 5) Constraints correctos.
ALTER TABLE libro
    ADD CONSTRAINT ck_libro_estado CHECK (estado IN ('activo', 'inactivo')),
    ADD CONSTRAINT ck_libro_condicion CHECK (condicion IN ('nuevo', 'como nuevo', 'muy bueno', 'bueno', 'aceptable')),
    ADD CONSTRAINT ck_libro_situacion CHECK (situacion IN ('disponible', 'ocupado'));

-- 6) Columna antigua ya no requerida.
ALTER TABLE libro DROP COLUMN IF EXISTS estado_logico;
