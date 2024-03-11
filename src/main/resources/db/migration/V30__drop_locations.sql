ALTER TABLE IF EXISTS public.displays
    ADD COLUMN room_codes text[];

UPDATE public.displays
    SET room_codes = l.room_codes
    FROM public.locations l
    WHERE l.id = location_id;

ALTER TABLE public.displays DROP CONSTRAINT fk_displays_locations_id;
ALTER TABLE  public.displays DROP COLUMN IF EXISTS location_id;

DROP TABLE IF EXISTS public.display_locations;
DROP TABLE IF EXISTS public.locations;