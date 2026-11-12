-- Create events table subclass of posts table
create table public.events (
  post_id uuid not null,
  event_start_at timestamp with time zone not null,
  event_end_at timestamp with time zone not null,

  constraint events_pkey primary key (post_id),

  constraint events_post_id_fkey
    foreign key (post_id)
    references public.posts (id)
    on delete cascade,

  constraint events_valid_dates_check
    check (event_end_at > event_start_at)
);

-- Add post_type column to posts table
ALTER TABLE public.posts
ADD COLUMN post_type TEXT NOT NULL DEFAULT 'post',
ADD CONSTRAINT posts_post_type_check
CHECK (post_type IN ('post', 'event'));