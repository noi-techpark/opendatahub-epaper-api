ALTER TABLE ONLY public.displays
    ADD COLUMN template_id integer;

ALTER TABLE ONLY public.resolutions
    ADD CONSTRAINT uk_resolution_width_height UNIQUE (width, height);

ALTER TABLE ONLY public.displays
    ADD CONSTRAINT fk_displays_templates_id FOREIGN KEY (template_id) REFERENCES public.templates(id);
