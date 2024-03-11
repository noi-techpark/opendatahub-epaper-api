UPDATE public.templates
SET multiple_room = false
WHERE multiple_room IS NULL;

UPDATE public.templates
SET footer = false
WHERE footer IS NULL;

UPDATE public.templates
SET header = false
WHERE header IS NULL;

UPDATE public.scheduled_content
SET override = false
WHERE override IS NULL;

UPDATE public.image_fields
SET repeat = false
WHERE repeat IS NULL;

UPDATE public.image_fields
SET is_repeated = false
WHERE is_repeated IS NULL;

UPDATE public.image_fields
SET italic = false
WHERE italic IS NULL;

UPDATE public.image_fields
SET bold = false
WHERE bold IS NULL;

UPDATE public.image_fields
SET border = false
WHERE border IS NULL;