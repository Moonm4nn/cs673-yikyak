-- New bucket for post images, 5MB file size limit
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
  'post-images', 'post-images', true,
  5242880,
  array['image/jpeg', 'image/png', 'image/webp']
)
on conflict (id) do nothing;

create policy "signed-in users can upload post images"
on storage.objects for insert to authenticated
with check (bucket_id = 'post-images');