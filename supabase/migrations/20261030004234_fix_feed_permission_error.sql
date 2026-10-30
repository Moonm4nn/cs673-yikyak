CREATE OR REPLACE FUNCTION public.get_posts_within_radius(u_lat double precision, u_lng double precision, rad_km double precision, page_offset integer DEFAULT 0, page_limit integer DEFAULT 20, sort_by_hot boolean DEFAULT false)
 RETURNS SETOF public.posts
 LANGUAGE sql
 STABLE
 SECURITY DEFINER
 SET search_path = public, extensions
AS $function$
    SELECT p.*
    FROM public.posts p
    WHERE ST_DWithin(
        p.location,
        ST_SetSRID(
            ST_MakePoint(u_lng, u_lat),
            4326
        )::geography,
        rad_km * 1000.0
    )
    ORDER BY
        CASE
            WHEN sort_by_hot THEN
                COALESCE(p.score, 0) + COALESCE(p.comment_count, 0)
        END DESC NULLS LAST,
        CASE
            WHEN NOT sort_by_hot THEN p.created_at
        END DESC NULLS LAST,
        p.created_at DESC
    LIMIT page_limit
    OFFSET page_offset;
$function$
