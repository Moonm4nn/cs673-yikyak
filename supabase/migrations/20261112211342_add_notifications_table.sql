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

create function public.get_my_notifications()
returns table (
    id          uuid,
    post_id     uuid,
    comment_id  uuid,
    type        text,
    is_read     boolean,
    created_at  timestamptz,
    content     text,
    alias       text
)
language sql
stable
security definer
set search_path = public
as $$
    select
        n.id,
        n.post_id,
        n.comment_id,
        n.type,
        n.is_read,
        n.created_at,
        c.content,
        case when c.user_id = p.user_id
            then 'OP'
            else 'Anon-' || (abs(hashtext(c.user_id::text || c.post_id::text)::bigint) % 10000)::text
        end as alias
    from notifications n
    join comments c on c.id = n.comment_id
    join posts p on p.id = n.post_id
    where n.recipient_id = auth.uid()
        and not c.is_removed
        and not p.is_removed
    order by n.created_at desc;
$$;

create function public.mark_notifications_read()
returns void
language sql
security definer
set search_path = public
as $$
    update notifications
    set is_read = true
    where recipient_id = auth.uid()
        and not is_read;
$$;

revoke all on function public.get_my_notifications()
    from public, anon, authenticated, service_role;
grant execute on function public.get_my_notifications()
    to authenticated;

revoke all on function public.mark_notifications_read()
    from public, anon, authenticated, service_role;
grant execute on function public.mark_notifications_read()
    to authenticated;
