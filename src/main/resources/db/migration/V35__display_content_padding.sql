ALTER TABLE IF EXISTS public.display_content
    ADD COLUMN padding int;

UPDATE public.display_content
    SET padding = 0
    WHERE padding IS NULL;