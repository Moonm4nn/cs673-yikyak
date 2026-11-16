-- Creates notifications table

create table public.notifications (
    id              uuid primary key default gen_random_uuid(),
    recipient_id    uuid not null references public.profiles(id) on delete cascade,
    post_id         uuid not null references public.posts(id) on delete cascade,
    comment_id      uuid not null references public.comments(id) on delete cascade,
    type            text not null check (type in ('post_reply', 'comment_reply')),
    is_read         boolean not null default false,
    created_at      timestamptz not null default now()
);

create index notifications_recipient_id_idx on public.notifications (recipient_id);

revoke all privileges on table public.notifications from public, anon, authenticated;

create function public.notify_on_comment()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_recipient_id uuid;
begin
    if new.parent_id is null then
        select user_id into v_recipient_id
        from posts
        where id = new.post_id;

        if v_recipient_id <> new.user_id then
            insert into notifications (recipient_id, post_id, comment_id, type)
            values (v_recipient_id, new.post_id, new.id, 'post_reply');
        end if;
    else
        select user_id into v_recipient_id
        from comments
        where id = new.parent_id;

        if v_recipient_id <> new.user_id then
            insert into notifications (recipient_id, post_id, comment_id, type)
            values (v_recipient_id, new.post_id, new.id, 'comment_reply');
        end if;
    end if;
    return null;
end;
$$;

create trigger on_comment_created
    after insert on public.comments
    for each row execute function public.notify_on_comment();
