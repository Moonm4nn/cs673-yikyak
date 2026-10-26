SET local check_function_bodies = off;

ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" REVOKE ALL ON SEQUENCES FROM "anon";

ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" REVOKE ALL ON SEQUENCES FROM "authenticated";

ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" REVOKE ALL ON SEQUENCES FROM "service_role";

ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" REVOKE ALL ON FUNCTIONS FROM "anon";

ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" REVOKE ALL ON FUNCTIONS FROM "authenticated";

ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" REVOKE ALL ON FUNCTIONS FROM "service_role";

ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" REVOKE ALL ON TABLES FROM "anon";

ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" REVOKE ALL ON TABLES FROM "authenticated";

ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" REVOKE ALL ON TABLES FROM "service_role";

REVOKE ALL ON FUNCTION "public"."apply_comment_count"() FROM "service_role";

REVOKE ALL ON FUNCTION "public"."apply_vote_delta"() FROM "service_role";

REVOKE ALL ON FUNCTION "public"."cast_vote"(text, uuid, smallint) FROM "anon";

REVOKE ALL ON FUNCTION "public"."cast_vote"(text, uuid, smallint) FROM "authenticated";

REVOKE ALL ON FUNCTION "public"."cast_vote"(text, uuid, smallint) FROM "service_role";

REVOKE ALL ON FUNCTION "public"."create_comment"(uuid, text) FROM "anon";

REVOKE ALL ON FUNCTION "public"."create_comment"(uuid, text) FROM "authenticated";

REVOKE ALL ON FUNCTION "public"."create_comment"(uuid, text) FROM "service_role";

REVOKE ALL ON FUNCTION "public"."create_post"(text, double precision, double precision) FROM "anon";

REVOKE ALL ON FUNCTION "public"."create_post"(text, double precision, double precision) FROM "authenticated";

REVOKE ALL ON FUNCTION "public"."create_post"(text, double precision, double precision) FROM "service_role";

REVOKE ALL ON FUNCTION "public"."get_comments"(uuid) FROM "anon";

REVOKE ALL ON FUNCTION "public"."get_comments"(uuid) FROM "authenticated";

REVOKE ALL ON FUNCTION "public"."get_comments"(uuid) FROM "service_role";

REVOKE ALL ON FUNCTION "public"."handle_new_user"() FROM "service_role";

REVOKE ALL ON FUNCTION "public"."nearby_posts"(double precision, double precision, double precision, text, integer, integer) FROM "anon";

REVOKE ALL ON FUNCTION "public"."nearby_posts"(double precision, double precision, double precision, text, integer, integer) FROM "authenticated";

REVOKE ALL ON FUNCTION "public"."nearby_posts"(double precision, double precision, double precision, text, integer, integer) FROM "service_role";

REVOKE ALL ON FUNCTION "public"."report_content"(text, uuid, text) FROM "anon";

REVOKE ALL ON FUNCTION "public"."report_content"(text, uuid, text) FROM "authenticated";

REVOKE ALL ON FUNCTION "public"."report_content"(text, uuid, text) FROM "service_role";

ALTER TABLE "public"."comments"
  ENABLE ROW LEVEL SECURITY;

ALTER TABLE "public"."posts"
  ENABLE ROW LEVEL SECURITY;

ALTER TABLE "public"."profiles"
  ENABLE ROW LEVEL SECURITY;

ALTER TABLE "public"."reports"
  ENABLE ROW LEVEL SECURITY;

ALTER TABLE "public"."votes"
  ENABLE ROW LEVEL SECURITY;

CREATE OR REPLACE FUNCTION public.apply_comment_count()
  RETURNS TRIGGER
  LANGUAGE plpgsql
  SECURITY DEFINER
  SET search_path TO 'public'
  AS $function$
begin
  if tg_op = 'INSERT' then
    update posts set comment_count = comment_count + 1 where id = new.post_id;
  else
    update posts set comment_count = greatest(comment_count - 1, 0) where id = old.post_id;
  end if;
  return null;
end;
$function$;

CREATE OR REPLACE FUNCTION public.apply_vote_delta()
  RETURNS TRIGGER
  LANGUAGE plpgsql
  SECURITY DEFINER
  SET search_path TO 'public'
  AS $function$
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
$function$;

CREATE OR REPLACE FUNCTION public.cast_vote (
  p_target_type text,
  p_target_id   uuid,
  p_value       smallint
)
  RETURNS void
  LANGUAGE plpgsql
  SECURITY DEFINER
  SET search_path TO 'public'
  AS $function$
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
$function$;

CREATE OR REPLACE FUNCTION public.create_comment (
  p_post_id uuid,
  p_content text
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

  insert into comments (post_id, user_id, content)
  values (p_post_id, auth.uid(), p_content)
  returning id into v_id;

  return v_id;
end;
$function$;

CREATE OR REPLACE FUNCTION public.create_post (
  p_content text,
  p_lat     double precision,
  p_lng     double precision
)
  RETURNS uuid
  LANGUAGE plpgsql
  SECURITY DEFINER
  SET search_path TO ''
  AS $function$
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
$function$;

CREATE OR REPLACE FUNCTION public.get_comments (
  p_post_id uuid
)
  RETURNS TABLE (
    id         uuid,
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

CREATE OR REPLACE FUNCTION public.handle_new_user()
  RETURNS TRIGGER
  LANGUAGE plpgsql
  SECURITY DEFINER
  SET search_path TO ''
  AS $function$
begin
  insert into public.profiles (id) values (new.id);
  return new;
end;
$function$;

CREATE OR REPLACE FUNCTION public.nearby_posts (
  p_lat      double precision,
  p_lng      double precision,
  p_radius_m double precision DEFAULT 8000,
  p_sort     text             DEFAULT 'new'::text,
  p_limit    integer          DEFAULT 30,
  p_offset   integer          DEFAULT 0
)
  RETURNS TABLE (
    id            uuid,
    content       text,
    score         integer,
    comment_count integer,
    created_at    timestamp with time zone,
    my_vote       smallint,
    distance_m    double precision
  )
  LANGUAGE sql
  STABLE
  SECURITY DEFINER
  SET search_path TO 'public', 'extensions'
  AS $function$
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
$function$;

CREATE OR REPLACE FUNCTION public.report_content (
  p_target_type text,
  p_target_id   uuid,
  p_reason      text DEFAULT NULL::text
)
  RETURNS void
  LANGUAGE plpgsql
  SECURITY DEFINER
  SET search_path TO 'public'
  AS $function$
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
$function$;

CREATE OR REPLACE FUNCTION public.rls_auto_enable()
  RETURNS event_trigger
  LANGUAGE plpgsql
  SECURITY DEFINER
  SET search_path TO 'pg_catalog'
  AS $function$
DECLARE
  cmd record;
  tbl name;
BEGIN
  FOR cmd IN
    SELECT *
    FROM pg_event_trigger_ddl_commands()
    WHERE command_tag IN ('CREATE TABLE', 'CREATE TABLE AS', 'SELECT INTO')
      AND object_type IN ('table','partitioned table')
  LOOP
     IF cmd.schema_name IS NOT NULL AND cmd.schema_name IN ('public') AND cmd.schema_name NOT IN ('pg_catalog','information_schema') AND cmd.schema_name NOT LIKE 'pg_toast%' AND cmd.schema_name NOT LIKE 'pg_temp%' THEN
      BEGIN
        SELECT c.relname INTO tbl FROM pg_catalog.pg_class c WHERE c.oid = cmd.objid;
        EXECUTE format('alter table if exists %I.%I enable row level security', cmd.schema_name, tbl);
        RAISE LOG 'rls_auto_enable: enabled RLS on %', cmd.object_identity;
      EXCEPTION
        WHEN OTHERS THEN
          RAISE LOG 'rls_auto_enable: failed to enable RLS on %', cmd.object_identity;
      END;
     ELSE
        RAISE LOG 'rls_auto_enable: skip % (either system schema or not in enforced list: %.)', cmd.object_identity, cmd.schema_name;
     END IF;
  END LOOP;
END;
$function$;

REVOKE ALL ON FUNCTION "public"."rls_auto_enable"() FROM "anon", "authenticated", "service_role";

CREATE POLICY "Allow public read access" ON "public"."profiles"
  FOR SELECT
  TO "anon", "authenticated"
  USING (true);

CREATE EVENT TRIGGER "ensure_rls"
  ON ddl_command_end
  WHEN TAG IN ('CREATE TABLE', 'CREATE TABLE AS', 'SELECT INTO')
  EXECUTE FUNCTION "public"."rls_auto_enable"();

GRANT EXECUTE ON FUNCTION "public"."rls_auto_enable"() TO PUBLIC;

REVOKE ALL ON FUNCTION "public"."rls_auto_enable"() FROM "postgres";

GRANT EXECUTE ON FUNCTION "public"."rls_auto_enable"() TO "postgres";

REVOKE ALL ON TABLE "public"."comments" FROM "anon";

GRANT MAINTAIN, REFERENCES, TRIGGER, TRUNCATE ON TABLE "public"."comments" TO "anon";

REVOKE ALL ON TABLE "public"."comments" FROM "authenticated";

GRANT MAINTAIN, REFERENCES, TRIGGER, TRUNCATE ON TABLE "public"."comments" TO "authenticated";

REVOKE ALL ON TABLE "public"."comments" FROM "service_role";

GRANT MAINTAIN, REFERENCES, TRIGGER, TRUNCATE ON TABLE "public"."comments" TO "service_role";

REVOKE ALL ON TABLE "public"."posts" FROM "anon";

GRANT MAINTAIN, REFERENCES, TRIGGER, TRUNCATE ON TABLE "public"."posts" TO "anon";

REVOKE ALL ON TABLE "public"."posts" FROM "authenticated";

GRANT MAINTAIN, REFERENCES, TRIGGER, TRUNCATE ON TABLE "public"."posts" TO "authenticated";

REVOKE ALL ON TABLE "public"."posts" FROM "service_role";

GRANT MAINTAIN, REFERENCES, TRIGGER, TRUNCATE ON TABLE "public"."posts" TO "service_role";

REVOKE ALL ON TABLE "public"."reports" FROM "anon";

GRANT MAINTAIN, REFERENCES, TRIGGER, TRUNCATE ON TABLE "public"."reports" TO "anon";

REVOKE ALL ON TABLE "public"."reports" FROM "authenticated";

GRANT MAINTAIN, REFERENCES, TRIGGER, TRUNCATE ON TABLE "public"."reports" TO "authenticated";

REVOKE ALL ON TABLE "public"."reports" FROM "service_role";

GRANT MAINTAIN, REFERENCES, TRIGGER, TRUNCATE ON TABLE "public"."reports" TO "service_role";

REVOKE ALL ON TABLE "public"."votes" FROM "anon";

GRANT MAINTAIN, REFERENCES, TRIGGER, TRUNCATE ON TABLE "public"."votes" TO "anon";

REVOKE ALL ON TABLE "public"."votes" FROM "authenticated";

GRANT MAINTAIN, REFERENCES, TRIGGER, TRUNCATE ON TABLE "public"."votes" TO "authenticated";

REVOKE ALL ON TABLE "public"."votes" FROM "service_role";

GRANT MAINTAIN, REFERENCES, TRIGGER, TRUNCATE ON TABLE "public"."votes" TO "service_role";

ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT MAINTAIN, REFERENCES, TRIGGER, TRUNCATE ON TABLES TO "anon";

ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT MAINTAIN, REFERENCES, TRIGGER, TRUNCATE ON TABLES TO "authenticated";

ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT MAINTAIN, REFERENCES, TRIGGER, TRUNCATE ON TABLES TO "service_role";

