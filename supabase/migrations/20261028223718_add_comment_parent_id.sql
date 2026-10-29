alter table public.comments
  add column parent_id uuid references public.comments(id) on delete set null;

create index comments_parent_id_idx on public.comments (parent_id);

drop function if exists public.create_comment(uuid, text);

CREATE OR REPLACE FUNCTION public.create_comment (
  p_post_id uuid,
  p_content text,
  p_parent_id uuid default null
)
  RETURNS uuid
  LANGUAGE plpgsql
  SECURITY DEFINER
  SET search_path TO 'public'
  AS $function$
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

  if p_parent_id is not null and not exists (
    select 1 from comments
    where id = p_parent_id
      and post_id = p_post_id
      and not is_removed
  ) then
    raise exception 'parent comment not found';
  end if;

  insert into comments (post_id, user_id, content, parent_id)
  values (p_post_id, auth.uid(), p_content, p_parent_id)
  returning id into v_id;

  return v_id;
end;
$function$;

drop function if exists public.get_comments(uuid);

CREATE OR REPLACE FUNCTION public.get_comments (
  p_post_id uuid
)
  RETURNS TABLE (
    id         uuid,
    parent_id  uuid,
    content    text,
    score      integer,
    created_at timestamp with time zone,
    my_vote    smallint,
    alias      text
  )
  LANGUAGE sql
  STABLE
  SECURITY DEFINER
  SET search_path TO 'public'
  AS $function$
  select c.id,
         c.parent_id,
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
$function$;

REVOKE ALL ON FUNCTION public.get_comments(uuid)
    FROM PUBLIC, anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.get_comments(uuid)
    TO authenticated;

REVOKE ALL ON FUNCTION public.create_comment(uuid, text, uuid)
    FROM PUBLIC, anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.create_comment(uuid, text, uuid)
    TO authenticated;
