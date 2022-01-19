ALTER TABLE public.image_fields
    ALTER COLUMN custom_text TYPE character varying(250) COLLATE pg_catalog."default";

ALTER TABLE IF EXISTS public.image_fields
    ADD COLUMN font_size integer;