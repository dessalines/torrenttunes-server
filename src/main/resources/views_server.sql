CREATE VIEW song_view as 
select torrent_path,
info_hash, 
song.mbid as song_mbid,
song.title, 
release_group_mbid,
duration_ms, 
plays,
disc_number,
track_number,
release_group.title as album,
artist_mbid,
artist.name as artist,
year,
album_coverart_url,
album_coverart_thumbnail_large,
album_coverart_thumbnail_small,
seeders,
release_group.primary_type,
release_group.secondary_types
from song
inner join song_release_group
on song.mbid = song_release_group.song_mbid 
inner join release_group
on release_group.mbid = song_release_group.release_group_mbid
inner join artist 
on release_group.artist_mbid = artist.mbid
;

CREATE VIEW song_view_grouped as 
select * from song_view
group by song_mbid;

CREATE VIEW song_search_view AS
select song.mbid as song_mbid,
album_view.mbid as album_mbid,
info_hash,
seeders,
is_primary_album,
song.plays,
artist_name || ' - ' || album_view.title || ' - ' ||  song.title as search_song
from song
inner join song_release_group
on song.mbid = song_release_group.song_mbid 
inner join album_view
on album_view.mbid = song_release_group.release_group_mbid;

CREATE VIEW album_search_view AS
select album_view.mbid as album_mbid,
is_primary_album,
plays,
artist_name || ' - ' || title as search_album
from album_view;


CREATE VIEW artist_search_view AS 
select mbid as artist_mbid,
name as search_artist
from artist;

-- this includes song counts and play times
CREATE VIEW album_view AS
select release_group.mbid,
release_group.title,
release_group.artist_mbid,
artist.name as artist_name,
year,
release_group.wikipedia_link,
release_group.allmusic_link,
album_coverart_url,
album_coverart_thumbnail_large,
album_coverart_thumbnail_small,
primary_type,
secondary_types,
count(song_release_group.id) as number_of_songs,
sum(plays) as plays,
primary_type = 'Album' AND secondary_types is NULL as is_primary_album
from release_group
inner join song_release_group
on release_group.mbid = song_release_group.release_group_mbid
inner join song 
on song.mbid = song_release_group.song_mbid
inner join artist
on release_group.artist_mbid = artist.mbid
group by release_group.mbid;

CREATE VIEW related_artist_view AS 
select artist1.mbid as artist1_mbid, 
artist1.name as artist1_name, 
artist2.mbid as artist2_mbid, 
artist2.name as artist2_name, 
tag_info1.count, 
tag_info2.count, 
tag.name, 
tag.id,
(tag_info1.tag_id*100/732) as score
from artist as artist1
left join tag_info as tag_info1
on artist1.mbid = tag_info1.artist_mbid
left join tag 
on tag_info1.tag_id = tag.id
left join tag_info as tag_info2
on tag_info2.tag_id = tag.id
left join artist as artist2
on tag_info2.artist_mbid = artist2.mbid
where artist1.mbid = 'c296e10c-110a-4103-9e77-47bfebb7fb2e'
and artist2.mbid != 'c296e10c-110a-4103-9e77-47bfebb7fb2e'
group by artist2.mbid
order by 
-- This one sorts by tag.id desc, meaning the weirdest categories
tag_info1.tag_id desc,
-- This one makes it more pertinent(NIN has the most votes for industrial)
tag_info1.count desc, 
-- This one does the second groups votes
tag_info2.count desc
limit 10;

-- where artist1.name like '%Bob Marley%'

-- and artist2.name not like '%Deftones%'
--where artist1.mbid like 'b7ffd2af-418f-4be2-bdd1-22f8b48613da' 
--and artist2.mbid not like 'b7ffd2af-418f-4be2-bdd1-22f8b48613da' 

select * from related_artist_view where `mbid` like 'b7ffd2af-418f-4be2-bdd1-22f8b48613da';

CREATE VIEW artist_tag_view AS 
select artist.mbid, artist.name as artist_name, tag_info.count, tag.name, tag.id
from artist
left join tag_info
on artist.mbid = tag_info.artist_mbid
left join tag 
on tag_info.tag_id = tag.id
where artist.name like '%Bob Marley%'
order by artist.mbid, tag_info.count desc, tag.id desc
;

CREATE VIEW related_song_view AS 
select artist1.mbid as artist1_mbid, 
artist1.name as artist1_name, 
artist2.mbid as artist2_mbid, 
artist2.name as artist2_name, 
-- rg.mbid,
-- rg.title,
song.mbid,
song.title,
song.info_hash,
tag_info1.count, 
tag_info2.count, 
tag.name, 
tag.id,
(tag_info1.tag_id*100/732) as score,
(
	select mbid from release_group
	where artist2.mbid = release_group.artist_mbid
	order by random()
	limit 1
) as rg_mbid,
(
	select song_mbid from song_release_group
	where song_release_group.release_group_mbid = 
	(
		select mbid from release_group
		where artist2.mbid = release_group.artist_mbid
		order by random()
		limit 1
	)
	order by random()
	limit 1
) as srg_song_mbid,
(
	select id from song
	where song.mbid = 
	(
		select song_mbid from song_release_group
		where song_release_group.release_group_mbid = 
		(
			select mbid from release_group
			where artist2.mbid = release_group.artist_mbid
			order by random()
			limit 1
		)
		order by random()
		limit 1
	)
	order by id, random()
) as song_id
from artist as artist1
left join tag_info as tag_info1
on artist1.mbid = tag_info1.artist_mbid
left join tag 
on tag_info1.tag_id = tag.id
left join tag_info as tag_info2
on tag_info2.tag_id = tag.id
left join artist as artist2
on tag_info2.artist_mbid = artist2.mbid
left join song
on song.id = song_id
-- where artist1.mbid = 'db3c0a20-bf05-4b30-ac22-f294aea24172'

group by artist2.mbid
order by 
-- This one sorts by tag.id desc, meaning the weirdest categories
tag_info1.tag_id desc,
-- This one makes it more pertinent(NIN has the most votes for industrial)
tag_info1.count desc, 
-- This one does the second groups votes
tag_info2.count desc

limit 10;





