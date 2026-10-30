-- function dropped and recreated because needed to change return type.
-- Permissions added again because dropping removes grants
-- also added filter criteria based on is_removed

drop function if exists public.get_posts_within_radius(
    double precision, double precision, double precision, integer, integer, boolean
);

create function public.get_posts_within_radius(
    u_lat double precision,
    u_lng double precision,
    rad_km double precision,
    page_offset integer default 0,
    page_limit integer default 20,
    sort_by_hot boolean default false
)
returns table (
    id uuid,
    user_id uuid,
    content text,
    location extensions.geography,
    score integer,
    comment_count integer,
    is_removed boolean,
    created_at timestamptz,
    my_vote smallint
)
language sql
stable
security definer
set search_path = public, extensions
as $function$
    select
        p.id,
        p.user_id,
        p.content,
        p.location,
        p.score,
        p.comment_count,
        p.is_removed,
        p.created_at,
        coalesce(v.value, 0)::smallint as my_vote
    from public.posts p
    left join public.votes v
           on v.target_type = 'post'
          and v.target_id = p.id
          and v.user_id = auth.uid()
    where not p.is_removed
      and st_dwithin(
            p.location,
            st_setsrid(st_makepoint(u_lng, u_lat), 4326)::geography,
            rad_km * 1000.0
          )
    order by
        case when sort_by_hot then coalesce(p.score, 0) + coalesce(p.comment_count, 0) end desc nulls last,
        case when not sort_by_hot then p.created_at end desc nulls last,
        p.created_at desc
    limit page_limit
    offset page_offset;
$function$;

revoke execute on function public.get_posts_within_radius(
    double precision, double precision, double precision, integer, integer, boolean
) from public, anon;

grant execute on function public.get_posts_within_radius(
    double precision, double precision, double precision, integer, integer, boolean
) to authenticated;


-- Changed is_removed threshold to -3 for testing during development
create or replace function public.apply_vote_delta()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_threshold constant int := -3;
  v_delta int;
  v_type  text;
  v_id    uuid;
begin
  if tg_op = 'INSERT' then
    v_delta := new.value;  v_type := new.target_type;  v_id := new.target_id;
  elsif tg_op = 'UPDATE' then
    v_delta := new.value - old.value;  v_type := new.target_type;  v_id := new.target_id;
  else
    v_delta := -old.value;  v_type := old.target_type;  v_id := old.target_id;
  end if;

  if v_type = 'post' then
    update posts
       set score = score + v_delta,
           is_removed = is_removed or (score + v_delta <= v_threshold)
     where id = v_id;
  else
    update comments
       set score = score + v_delta,
           is_removed = is_removed or (score + v_delta <= v_threshold)
     where id = v_id;
  end if;
  return null;
end;
$$;