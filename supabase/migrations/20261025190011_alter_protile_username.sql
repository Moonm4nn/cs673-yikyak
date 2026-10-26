-- ------------------------------------------------------------
-- Profile fields for onboarding
-- ------------------------------------------------------------

ALTER TABLE public.profiles
    ADD COLUMN IF NOT EXISTS username text;

ALTER TABLE public.profiles
    ADD COLUMN IF NOT EXISTS onboarding_completed boolean
    NOT NULL DEFAULT false;

-- A completed profile must have a nonblank username.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'profiles_completed_requires_username'
          AND conrelid = 'public.profiles'::regclass
    ) THEN
        ALTER TABLE public.profiles
            ADD CONSTRAINT profiles_completed_requires_username
            CHECK (
                NOT onboarding_completed
                OR NULLIF(BTRIM(username), '') IS NOT NULL
            );
    END IF;
END;
$$;

-- ------------------------------------------------------------
-- Profile permissions
-- A policy does not replace SQL table privileges.
-- ------------------------------------------------------------

GRANT SELECT ON TABLE public.profiles TO authenticated;

GRANT UPDATE (username, onboarding_completed)
    ON TABLE public.profiles TO authenticated;

DROP POLICY IF EXISTS "Users can update their own profile"
    ON public.profiles;

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

-- ------------------------------------------------------------
-- Allow authenticated app users to call the RPC functions
-- ------------------------------------------------------------

REVOKE EXECUTE ON FUNCTION
    public.create_post(text, double precision, double precision)
    FROM PUBLIC, anon;

GRANT EXECUTE ON FUNCTION
    public.create_post(text, double precision, double precision)
    TO authenticated;

REVOKE EXECUTE ON FUNCTION
    public.nearby_posts(double precision, double precision, double precision, text, integer, integer)
    FROM PUBLIC, anon;

GRANT EXECUTE ON FUNCTION
    public.nearby_posts(double precision, double precision, double precision, text, integer, integer)
    TO authenticated;

REVOKE EXECUTE ON FUNCTION public.get_comments(uuid)
    FROM PUBLIC, anon;

GRANT EXECUTE ON FUNCTION public.get_comments(uuid)
    TO authenticated;

REVOKE EXECUTE ON FUNCTION public.create_comment(uuid, text)
    FROM PUBLIC, anon;

GRANT EXECUTE ON FUNCTION public.create_comment(uuid, text)
    TO authenticated;

REVOKE EXECUTE ON FUNCTION public.cast_vote(text, uuid, smallint)
    FROM PUBLIC, anon;

GRANT EXECUTE ON FUNCTION public.cast_vote(text, uuid, smallint)
    TO authenticated;

REVOKE EXECUTE ON FUNCTION public.report_content(text, uuid, text)
    FROM PUBLIC, anon;

GRANT EXECUTE ON FUNCTION public.report_content(text, uuid, text)
    TO authenticated;