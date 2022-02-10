ALTER TABLE IF EXISTS public.displays
    ADD CONSTRAINT displays_name_key UNIQUE (name);
    
ALTER TABLE IF EXISTS public.templates
    ADD CONSTRAINT templates_name_key UNIQUE (name);
    
ALTER TABLE IF EXISTS public.locations
    ADD CONSTRAINT locations_name_key UNIQUE (name);
        