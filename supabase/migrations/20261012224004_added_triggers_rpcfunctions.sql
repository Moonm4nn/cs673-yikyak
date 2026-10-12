-- ---------------------------------------------------------------------
-- Triggers
-- ---------------------------------------------------------------------

-- Create a profile automatically when someone signs in (anonymously)
create function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  insert into public.profiles (id) values (new.id);
  return new;
end;
$$;

-- Calls profile creation once new user signs up and is authenticated
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- Keep scores in sync with the votes table
-- Content is hidden once score hits -5
create function public.apply_vote_delta()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
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
           is_removed = is_removed or (score + v_delta <= -5)
     where id = v_id;
  else
    update comments
       set score = score + v_delta,
           is_removed = is_removed or (score + v_delta <= -5)
     where id = v_id;
  end if;
  return null;
end;
$$;

create trigger on_vote_change
  after insert or update or delete on public.votes
  for each row execute function public.apply_vote_delta();

-- Keep posts.comment_count in sync
create function public.apply_comment_count()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if tg_op = 'INSERT' then
    update posts set comment_count = comment_count + 1 where id = new.post_id;
  else
    update posts set comment_count = greatest(comment_count - 1, 0) where id = old.post_id;
  end if;
  return null;
end;
$$;

create trigger on_comment_change
  after insert or delete on public.comments
  for each row execute function public.apply_comment_count();

-- ---------------------------------------------------------------------
-- RPC functions - what the app calls
-- ---------------------------------------------------------------------

-- Create a post at the user's location
create or replace function public.create_post(
    p_content text,
    p_lat double precision,
    p_lng double precision
)
returns uuid
language plpgsql
security definer
set search_path = ''
as $$
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


    insert into public.posts (user_id, content, location)
    values (
        auth.uid(),
        p_content,
        extensions.st_setsrid(
            extensions.st_makepoint(p_lng, p_lat),
            4326
        )::extensions.geography
    )
    returning id into v_id;

    return v_id;
end;
$$;

-- Feed: posts within p_radius_m meters. p_sort is 'new' or 'hot'
create function public.nearby_posts(
  p_lat double precision,
  p_lng double precision,
  p_radius_m double precision default 8000,
  p_sort text default 'new',
  p_limit int default 30,
  p_offset int default 0
)
returns table (
  id uuid,
  content text,
  score int,
  comment_count int,
  created_at timestamptz,
  my_vote smallint,
  distance_m double precision
)
language sql
stable
security definer
set search_path = public, extensions
as $$
  select p.id,
         p.content,
         p.score,
         p.comment_count,
         p.created_at,
         coalesce(v.value, 0)::smallint,
         st_distance(p.location, st_setsrid(st_makepoint(p_lng, p_lat), 4326)::geography)
  from posts p
  left join votes v
    on v.target_type = 'post' and v.target_id = p.id and v.user_id = auth.uid()
  where not p.is_removed
    and st_dwithin(p.location, st_setsrid(st_makepoint(p_lng, p_lat), 4326)::geography, p_radius_m)
  order by
    case when p_sort = 'hot'
         then p.score / power(extract(epoch from (now() - p.created_at)) / 3600 + 2, 1.5)
    end desc nulls last,
    p.created_at desc
  limit least(p_limit, 100)
  offset p_offset;
$$;


-- Comments on a post, with alias instead of user ids
create function public.get_comments(p_post_id uuid)
returns table (
  id uuid,
  content text,
  score int,
  created_at timestamptz,
  my_vote smallint,
  alias text
)
language sql
stable
security definer
set search_path = public
as $$
  select c.id,
         c.content,
         c.score,
         c.created_at,
         coalesce(v.value, 0)::smallint,
         case when c.user_id = p.user_id
              then 'OP'
              else 'Anon-' || (abs(hashtext(c.user_id::text || c.post_id::text)::bigint) % 10000)::text
         end
  from comments c
  join posts p on p.id = c.post_id
  left join votes v
    on v.target_type = 'comment' and v.target_id = c.id and v.user_id = auth.uid()
  where c.post_id = p_post_id
    and not c.is_removed
    and not p.is_removed
  order by c.created_at;
$$;

create function public.create_comment(p_post_id uuid, p_content text)
returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
  v_id uuid;
begin
  if auth.uid() is null then
    raise exception 'not authenticated';
  end if;
  if exists (select 1 from profiles where id = auth.uid() and is_banned) then
    raise exception 'account banned';
  end if;
  if not exists (select 1 from posts where id = p_post_id and not is_removed) then
    raise exception 'post not found';
  end if;

  insert into comments (post_id, user_id, content)
  values (p_post_id, auth.uid(), p_content)
  returning id into v_id;

  return v_id;
end;
$$;

-- Vote: p_value = 1 (up), -1 (down), 0 (remove vote)
create function public.cast_vote(p_target_type text, p_target_id uuid, p_value smallint)
returns void
language plpgsql
security definer
set search_path = public
as $$
begin
  if auth.uid() is null then
    raise exception 'not authenticated';
  end if;
  if p_target_type not in ('post', 'comment') then
    raise exception 'invalid target type';
  end if;
  if p_value not in (-1, 0, 1) then
    raise exception 'invalid vote value';
  end if;

  if p_value = 0 then
    delete from votes
     where user_id = auth.uid() and target_type = p_target_type and target_id = p_target_id;
  else
    insert into votes (user_id, target_type, target_id, value)
    values (auth.uid(), p_target_type, p_target_id, p_value)
    on conflict (user_id, target_type, target_id)
    do update set value = excluded.value;
  end if;
end;
$$;

create function public.report_content(p_target_type text, p_target_id uuid, p_reason text default null)
returns void
language plpgsql
security definer
set search_path = public
as $$
begin
  if auth.uid() is null then
    raise exception 'not authenticated';
  end if;
  if p_target_type not in ('post', 'comment') then
    raise exception 'invalid target type';
  end if;

  insert into reports (reporter_id, target_type, target_id, reason)
  values (auth.uid(), p_target_type, p_target_id, p_reason)
  on conflict do nothing;
end;
$$;

-- ---------------------------------------------------------------------
-- Function permissions
-- can call the app functions. trigger functions shouldn’t be callable at all
-- ---------------------------------------------------------------------
revoke execute on function public.handle_new_user()      from public, anon, authenticated;
revoke execute on function public.apply_vote_delta()     from public, anon, authenticated;
revoke execute on function public.apply_comment_count()  from public, anon, authenticated;

