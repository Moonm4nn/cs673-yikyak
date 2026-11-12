--New RPC function for retrieving channels
create function public.get_channels()
returns table (id uuid, name text)
language sql
stable
security definer
set search_path = public
as $function$
    select c.id, c.name
    from public.channels c
    order by c.name;
$function$;

revoke execute on function public.get_channels() from public, anon;
grant execute on function public.get_channels() to authenticated;


--Updating create post to accomodate for channels; added channel_id param and channel_id validation

drop function if exists public.create_post(text, double precision, double precision);

create function public.create_post (
  p_content    text,
  p_lat        double precision,
  p_lng        double precision,
  p_channel_id uuid default null     -- null = public feed
)
  returns uuid
  language plpgsql
  security definer
  set search_path to ''
as $function$
declare
    v_id uuid;
begin
    if auth.uid() is null then
        raise exception 'not authenticated';
    end if;

    if exists (
        select 1
        from public.profiles
        where id = auth.uid()
          and is_banned is true
    ) then
        raise exception 'account banned';
    end if;

    if p_content is null or btrim(p_content) = '' then
        raise exception 'post content cannot be empty';
    end if;

    if p_lat is null or p_lat < -90 or p_lat > 90 then
        raise exception 'invalid latitude';
    end if;

    if p_lng is null or p_lng < -180 or p_lng > 180 then
        raise exception 'invalid longitude';
    end if;

    if p_channel_id is not null
       and not exists (select 1 from public.channels where id = p_channel_id) then
        raise exception 'channel does not exist';
    end if;

    insert into public.posts (user_id, content, location, channel_id)
    values (
        auth.uid(),
        p_content,
        extensions.st_setsrid(
            extensions.st_makepoint(p_lng, p_lat),
            4326
        )::extensions.geography,
        p_channel_id
    )
    returning id into v_id;

    return v_id;
end;
$function$;

-- Updated grants because function was dropped and recreated
revoke execute on function public.create_post(text, double precision, double precision, uuid)
  from public, anon;

grant execute on function public.create_post(text, double precision, double precision, uuid)
  to authenticated;