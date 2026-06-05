create table if not exists public.ride_matches (
  ride_request_id uuid not null references public.ride_requests(id) on delete cascade,
  ride_offer_id uuid not null references public.ride_offers(id) on delete cascade,
  primary key (ride_request_id, ride_offer_id)
);

alter table public.ride_matches enable row level security;

drop policy if exists "Users can read their ride matches" on public.ride_matches;

create policy "Users can read their ride matches"
on public.ride_matches
for select
to authenticated
using (
  exists (
    select 1
    from public.ride_requests rr
    where rr.id = ride_request_id
      and rr.passenger_id = auth.uid()
  )
  or exists (
    select 1
    from public.ride_offers ro
    where ro.id = ride_offer_id
      and ro.driver_id = auth.uid()
  )
);

drop function if exists public.match_ride_request(uuid);
drop function if exists public.match_ride_offer(uuid);

create or replace function public.match_ride_request_after_insert()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.ride_matches (ride_request_id, ride_offer_id)
  select rr.id, ro.id
  from public.ride_requests rr
  join public.ride_offers ro on true
  where rr.id = new.id
    and ro.driver_id <> rr.passenger_id
    and ro.recurrence_type = rr.recurrence_type
    and ro.departure_time >= rr.departure_time - make_interval(mins => rr.departure_tolerance_minutes)
    and ro.departure_time <= rr.departure_time + make_interval(mins => rr.departure_tolerance_minutes)
    and ro.departure_latitude >= rr.departure_latitude - (rr.departure_tolerance_radius_meters / 111320.0)
    and ro.departure_latitude <= rr.departure_latitude + (rr.departure_tolerance_radius_meters / 111320.0)
    and ro.departure_longitude >= rr.departure_longitude - (rr.departure_tolerance_radius_meters / greatest(1.0, 111320.0 * cos(radians(rr.departure_latitude))))
    and ro.departure_longitude <= rr.departure_longitude + (rr.departure_tolerance_radius_meters / greatest(1.0, 111320.0 * cos(radians(rr.departure_latitude))))
    and ro.arrival_latitude >= rr.arrival_latitude - (rr.arrival_tolerance_radius_meters / 111320.0)
    and ro.arrival_latitude <= rr.arrival_latitude + (rr.arrival_tolerance_radius_meters / 111320.0)
    and ro.arrival_longitude >= rr.arrival_longitude - (rr.arrival_tolerance_radius_meters / greatest(1.0, 111320.0 * cos(radians(rr.arrival_latitude))))
    and ro.arrival_longitude <= rr.arrival_longitude + (rr.arrival_tolerance_radius_meters / greatest(1.0, 111320.0 * cos(radians(rr.arrival_latitude))))
    and (
      select count(*)
      from public.reservations r
      where r.ride_offer_id = ro.id
    ) < ro.capacity
  on conflict (ride_request_id, ride_offer_id) do nothing;

  return new;
end;
$$;

create or replace function public.match_ride_offer_after_insert()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.ride_matches (ride_request_id, ride_offer_id)
  select rr.id, ro.id
  from public.ride_offers ro
  join public.ride_requests rr on true
  where ro.id = new.id
    and ro.driver_id <> rr.passenger_id
    and ro.recurrence_type = rr.recurrence_type
    and ro.departure_time >= rr.departure_time - make_interval(mins => rr.departure_tolerance_minutes)
    and ro.departure_time <= rr.departure_time + make_interval(mins => rr.departure_tolerance_minutes)
    and ro.departure_latitude >= rr.departure_latitude - (rr.departure_tolerance_radius_meters / 111320.0)
    and ro.departure_latitude <= rr.departure_latitude + (rr.departure_tolerance_radius_meters / 111320.0)
    and ro.departure_longitude >= rr.departure_longitude - (rr.departure_tolerance_radius_meters / greatest(1.0, 111320.0 * cos(radians(rr.departure_latitude))))
    and ro.departure_longitude <= rr.departure_longitude + (rr.departure_tolerance_radius_meters / greatest(1.0, 111320.0 * cos(radians(rr.departure_latitude))))
    and ro.arrival_latitude >= rr.arrival_latitude - (rr.arrival_tolerance_radius_meters / 111320.0)
    and ro.arrival_latitude <= rr.arrival_latitude + (rr.arrival_tolerance_radius_meters / 111320.0)
    and ro.arrival_longitude >= rr.arrival_longitude - (rr.arrival_tolerance_radius_meters / greatest(1.0, 111320.0 * cos(radians(rr.arrival_latitude))))
    and ro.arrival_longitude <= rr.arrival_longitude + (rr.arrival_tolerance_radius_meters / greatest(1.0, 111320.0 * cos(radians(rr.arrival_latitude))))
    and (
      select count(*)
      from public.reservations r
      where r.ride_offer_id = ro.id
    ) < ro.capacity
  on conflict (ride_request_id, ride_offer_id) do nothing;

  return new;
end;
$$;

drop trigger if exists match_ride_request_after_insert on public.ride_requests;
create trigger match_ride_request_after_insert
after insert on public.ride_requests
for each row
execute function public.match_ride_request_after_insert();

drop trigger if exists match_ride_offer_after_insert on public.ride_offers;
create trigger match_ride_offer_after_insert
after insert on public.ride_offers
for each row
execute function public.match_ride_offer_after_insert();
