ALTER TABLE ONLY public.locations
    ADD COLUMN description VARCHAR(50),
    ADD COLUMN room_code VARCHAR(150);