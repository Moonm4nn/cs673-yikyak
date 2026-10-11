-- =====================================================================
-- Yik Yak clone: initial schema
-- =====================================================================

create extension if not exists postgis with schema extensions;

-- ---------------------------------------------------------------------
-- Tables
-- ---------------------------------------------------------------------

create table public.profiles (
  id          uuid primary key references auth.users(id) on delete cascade,
  karma       int not null default 0,
  is_banned   boolean not null default false,
  created_at  timestamptz not null default now()
);

create table public.posts (
  id             uuid primary key default gen_random_uuid(),
  user_id        uuid not null references public.profiles(id) on delete cascade,
  content        text not null check (char_length(content) between 1 and 200),
  location       extensions.geography(Point, 4326) not null,
  score          int not null default 0,       
  comment_count  int not null default 0,       
  is_removed     boolean not null default false,
  created_at     timestamptz not null default now()
);

create table public.comments (
  id          uuid primary key default gen_random_uuid(),
  post_id     uuid not null references public.posts(id) on delete cascade,
  user_id     uuid not null references public.profiles(id) on delete cascade,
  content     text not null check (char_length(content) between 1 and 200),
  score       int not null default 0,
  is_removed  boolean not null default false,
  created_at  timestamptz not null default now()
);

create table public.votes (
  user_id      uuid not null references public.profiles(id) on delete cascade,
  target_type  text not null check (target_type in ('post', 'comment')),
  target_id    uuid not null,
  value        smallint not null check (value in (-1, 1)),
  created_at   timestamptz not null default now(),
  primary key (user_id, target_type, target_id)
);

create table public.reports (
  id           uuid primary key default gen_random_uuid(),
  reporter_id  uuid not null references public.profiles(id) on delete cascade,
  target_type  text not null check (target_type in ('post', 'comment')),
  target_id    uuid not null,
  reason       text,
  status       text not null default 'open',
  created_at   timestamptz not null default now(),
  unique (reporter_id, target_type, target_id)
);

-- ---------------------------------------------------------------------
-- Indexes
-- ---------------------------------------------------------------------
create index idx_posts_location on public.posts using gist (location);
create index idx_posts_created  on public.posts (created_at desc);
create index idx_comments_post  on public.comments (post_id, created_at);

