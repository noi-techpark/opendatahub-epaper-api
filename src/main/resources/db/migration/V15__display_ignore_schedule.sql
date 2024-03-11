ALTER TABLE IF EXISTS public.displays
    ADD COLUMN ignore_scheduled_content boolean NOT NULL DEFAULT false;