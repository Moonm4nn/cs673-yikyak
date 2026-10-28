-- Harden YikYak client-side authorization.
-- Design:
--   * Clients cannot directly read/write posts, comments, votes, or reports.
--   * Those operations go through the existing authenticated RPC boundary.
--   * Public-safe profile fields are exposed through public_profiles.
--   * A signed-in user retrieves their own private profile fields through get_my_profile().
--   * Banned users cannot create posts/comments, vote, or report content.

BEGIN;

-- ---------------------------------------------------------------------
-- 1. Remove the overbroad and unnecessary client table privileges.
--    Revoke from client roles only; preserve trusted service_role access.
-- ---------------------------------------------------------------------

REVOKE ALL PRIVILEGES ON TABLE
    public.profiles,
    public.posts,
    public.comments,
    public.votes,
    public.reports
FROM anon, authenticated;

-- Also clear any column-level grants that may have been granted separately.
REVOKE SELECT (id) ON TABLE public.profiles FROM anon, authenticated;
REVOKE UPDATE (username, onboarding_completed)
    ON TABLE public.profiles FROM anon, authenticated;

-- Remove the dangerous defaults introduced by earlier remote-schema migration.
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public
    REVOKE MAINTAIN, REFERENCES, TRIGGER, TRUNCATE ON TABLES
    FROM anon, authenticated;

-- ---------------------------------------------------------------------
-- 2. Profile access: private base table, public-safe view, own-profile RPC.
-- ---------------------------------------------------------------------

DROP POLICY IF EXISTS "Allow public read access" ON public.profiles;
DROP POLICY IF EXISTS "Users can read their own profile" ON public.profiles;
DROP POLICY IF EXISTS "Users can update their own profile" ON public.profiles;

CREATE POLICY "Users can read their own profile"
    ON public.profiles
    FOR SELECT
    TO authenticated
    USING (id = (SELECT auth.uid()));

CREATE POLICY "Users can update their own profile"
    ON public.profiles
    FOR UPDATE
    TO authenticated
    USING (id = (SELECT auth.uid()))
    WITH CHECK (
        id = (SELECT auth.uid())
        AND (
            NOT onboarding_completed
            OR NULLIF(BTRIM(username), '') IS NOT NULL
        )
    );

-- The app may use id in its own-row UPDATE filter, but cannot SELECT the
-- full base table. Only the permitted profile columns may be updated.
GRANT SELECT (id) ON TABLE public.profiles TO authenticated;
GRANT UPDATE (username, onboarding_completed)
    ON TABLE public.profiles TO authenticated;

-- Intentionally expose only public-safe columns; is_banned and onboarding
-- state are not exposed to other users.
CREATE OR REPLACE VIEW public.public_profiles
WITH (security_barrier = true)
AS
SELECT p.id, p.username, p.karma, p.created_at
FROM public.profiles AS p
WHERE p.onboarding_completed IS TRUE
  AND NULLIF(BTRIM(p.username), '') IS NOT NULL;

REVOKE ALL PRIVILEGES ON TABLE public.public_profiles FROM PUBLIC, anon;
GRANT SELECT ON TABLE public.public_profiles TO authenticated;

-- Private profile endpoint for the currently authenticated user.
CREATE OR REPLACE FUNCTION public.get_my_profile()
RETURNS TABLE (
    id uuid,
    username text,
    karma integer,
    is_banned boolean,
    created_at timestamptz,
    onboarding_completed boolean
)
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = ''
AS $function$
    SELECT p.id,
           p.username,
           p.karma,
           p.is_banned,
           p.created_at,
           p.onboarding_completed
    FROM public.profiles AS p
    WHERE p.id = auth.uid();
$function$;

REVOKE ALL ON FUNCTION public.get_my_profile() FROM PUBLIC, anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.get_my_profile() TO authenticated;

-- ---------------------------------------------------------------------
-- 3. Defense-in-depth RLS policies for tables that clients do not access
--    directly. No client table grants are made for these tables below.
-- ---------------------------------------------------------------------

DROP POLICY IF EXISTS "Authenticated users can read visible posts" ON public.posts;
CREATE POLICY "Authenticated users can read visible posts"
    ON public.posts
    FOR SELECT
    TO authenticated
    USING (NOT is_removed);

DROP POLICY IF EXISTS "Authenticated users can read visible comments" ON public.comments;
CREATE POLICY "Authenticated users can read visible comments"
    ON public.comments
    FOR SELECT
    TO authenticated
    USING (NOT is_removed);

DROP POLICY IF EXISTS "Users can read their own votes" ON public.votes;
CREATE POLICY "Users can read their own votes"
    ON public.votes
    FOR SELECT
    TO authenticated
    USING (user_id = (SELECT auth.uid()));

DROP POLICY IF EXISTS "Users can read their own reports" ON public.reports;
CREATE POLICY "Users can read their own reports"
    ON public.reports
    FOR SELECT
    TO authenticated
    USING (reporter_id = (SELECT auth.uid()));

-- Deliberately do not grant direct SELECT/INSERT/UPDATE/DELETE on these tables.
REVOKE ALL PRIVILEGES ON TABLE
    public.posts, public.comments, public.votes, public.reports
FROM anon, authenticated;

-- ---------------------------------------------------------------------
-- 4. Tighten RPC execution permissions.
--    These are the intended client-facing write/read boundary.
-- ---------------------------------------------------------------------

REVOKE ALL ON FUNCTION public.create_post(text, double precision, double precision)
    FROM PUBLIC, anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.create_post(text, double precision, double precision)
    TO authenticated;

REVOKE ALL ON FUNCTION public.nearby_posts(double precision, double precision, double precision, text, integer, integer)
    FROM PUBLIC, anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.nearby_posts(double precision, double precision, double precision, text, integer, integer)
    TO authenticated;

REVOKE ALL ON FUNCTION public.get_comments(uuid)
    FROM PUBLIC, anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.get_comments(uuid)
    TO authenticated;

REVOKE ALL ON FUNCTION public.create_comment(uuid, text)
    FROM PUBLIC, anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.create_comment(uuid, text)
    TO authenticated;

REVOKE ALL ON FUNCTION public.cast_vote(text, uuid, smallint)
    FROM PUBLIC, anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.cast_vote(text, uuid, smallint)
    TO authenticated;

REVOKE ALL ON FUNCTION public.report_content(text, uuid, text)
    FROM PUBLIC, anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.report_content(text, uuid, text)
    TO authenticated;

-- ---------------------------------------------------------------------
-- 5. Enforce bans and validate targets in write RPCs.
-- ---------------------------------------------------------------------

CREATE OR REPLACE FUNCTION public.cast_vote(
    p_target_type text,
    p_target_id uuid,
    p_value smallint
)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $function$
BEGIN
    IF auth.uid() IS NULL THEN
        RAISE EXCEPTION 'not authenticated';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.profiles AS pr
        WHERE pr.id = auth.uid()
          AND pr.is_banned IS TRUE
    ) THEN
        RAISE EXCEPTION 'account banned';
    END IF;

    IF p_target_type IS NULL OR p_target_type NOT IN ('post', 'comment') THEN
        RAISE EXCEPTION 'invalid target type';
    END IF;

    IF p_target_id IS NULL THEN
        RAISE EXCEPTION 'invalid target id';
    END IF;

    IF p_value IS NULL OR p_value NOT IN (-1, 0, 1) THEN
        RAISE EXCEPTION 'invalid vote value';
    END IF;

    IF p_target_type = 'post' THEN
        IF NOT EXISTS (
            SELECT 1 FROM public.posts AS p
            WHERE p.id = p_target_id AND NOT p.is_removed
        ) THEN
            RAISE EXCEPTION 'target not found';
        END IF;
    ELSE
        IF NOT EXISTS (
            SELECT 1 FROM public.comments AS c
            JOIN public.posts AS p ON p.id = c.post_id
            WHERE c.id = p_target_id
              AND NOT c.is_removed
              AND NOT p.is_removed
        ) THEN
            RAISE EXCEPTION 'target not found';
        END IF;
    END IF;

    IF p_value = 0 THEN
        DELETE FROM public.votes AS v
        WHERE v.user_id = auth.uid()
          AND v.target_type = p_target_type
          AND v.target_id = p_target_id;
    ELSE
        INSERT INTO public.votes (user_id, target_type, target_id, value)
        VALUES (auth.uid(), p_target_type, p_target_id, p_value)
        ON CONFLICT (user_id, target_type, target_id)
        DO UPDATE SET value = EXCLUDED.value;
    END IF;
END;
$function$;

CREATE OR REPLACE FUNCTION public.report_content(
    p_target_type text,
    p_target_id uuid,
    p_reason text DEFAULT NULL
)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $function$
BEGIN
    IF auth.uid() IS NULL THEN
        RAISE EXCEPTION 'not authenticated';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM public.profiles AS pr
        WHERE pr.id = auth.uid()
          AND pr.is_banned IS TRUE
    ) THEN
        RAISE EXCEPTION 'account banned';
    END IF;

    IF p_target_type IS NULL OR p_target_type NOT IN ('post', 'comment') THEN
        RAISE EXCEPTION 'invalid target type';
    END IF;

    IF p_target_id IS NULL THEN
        RAISE EXCEPTION 'invalid target id';
    END IF;

    IF p_target_type = 'post' THEN
        IF NOT EXISTS (
            SELECT 1 FROM public.posts AS p
            WHERE p.id = p_target_id AND NOT p.is_removed
        ) THEN
            RAISE EXCEPTION 'target not found';
        END IF;
    ELSE
        IF NOT EXISTS (
            SELECT 1
            FROM public.comments AS c
            JOIN public.posts AS p ON p.id = c.post_id
            WHERE c.id = p_target_id
              AND NOT c.is_removed
              AND NOT p.is_removed
        ) THEN
            RAISE EXCEPTION 'target not found';
        END IF;
    END IF;

    INSERT INTO public.reports (reporter_id, target_type, target_id, reason)
    VALUES (auth.uid(), p_target_type, p_target_id, p_reason)
    ON CONFLICT (reporter_id, target_type, target_id) DO NOTHING;
END;
$function$;

-- Reassert the intended grants after replacing the function definitions.
REVOKE ALL ON FUNCTION public.cast_vote(text, uuid, smallint)
    FROM PUBLIC, anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.cast_vote(text, uuid, smallint)
    TO authenticated;

REVOKE ALL ON FUNCTION public.report_content(text, uuid, text)
    FROM PUBLIC, anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.report_content(text, uuid, text)
    TO authenticated;

COMMIT;
