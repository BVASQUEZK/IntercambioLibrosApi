-- =========================================
-- MIGRACION: foto de perfil sin limite practico
-- =========================================

ALTER TABLE public.usuario
    ADD COLUMN IF NOT EXISTS url_foto_perfil TEXT;

ALTER TABLE public.usuario
    ALTER COLUMN url_foto_perfil TYPE TEXT;
