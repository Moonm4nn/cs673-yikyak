-- New table for channels feature

create table public.channels (
  id          uuid primary key default gen_random_uuid(),
  slug        text not null unique check (slug ~ '^[a-z0-9-]{2,30}$'),
  name        text not null check (char_length(name) between 2 and 40),
  created_at  timestamptz not null default now()
);

alter table public.channels enable row level security;
create policy "channels are readable" on public.channels
  for select to authenticated using (true);
grant select on public.channels to authenticated;

insert into public.channels (slug, name) values
  ('housing', 'Housing'), ('basketball', 'Basketball'), ('chess', 'Chess'), ('clubs', 'Clubs');

alter table public.posts
  add column channel_id uuid references public.channels(id) on delete cascade;
create index posts_channel_created_idx on public.posts (channel_id, created_at desc);



-- Function dropped + recreated for signature change: new param

drop function if exists public.get_posts_within_radius(
    double precision, double precision, double precision, integer, integer, boolean
);

create function public.get_posts_within_radius(
    u_lat double precision,
    u_lng double precision,
    rad_km double precision,
    p_channel_id uuid default null,
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
      and (p.channel_id is not distinct from p_channel_id)
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
    double precision, double precision, double precision, uuid, integer, integer, boolean
) from public, anon;

grant execute on function public.get_posts_within_radius(
    double precision, double precision, double precision, uuid, integer, integer, boolean
) to authenticated;