ALTER TABLE IF EXISTS public.scheduled_content
    ADD COLUMN include boolean;

ALTER TABLE IF EXISTS public.scheduled_content ALTER COLUMN include SET DEFAULT true;
