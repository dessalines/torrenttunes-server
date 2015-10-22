-- Drop the views

drop view if exists album_view, song_view, song_view_grouped, artist_search_view, related_artist_view, artist_tag_view,
related_song_view;


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
primary_type = 'Album' AND secondary_types is NULL as is_primary_album,
concat(artist.name,' - ',release_group.title) as search_album
from release_group
inner join song_release_group
on release_group.mbid = song_release_group.release_group_mbid
inner join song 
on song.mbid = song_release_group.song_mbid
inner join artist
on release_group.artist_mbid = artist.mbid
group by release_group.mbid;



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
concat(artist.name,' - ',release_group.title,' - ',song.title) as search_song,
release_group.primary_type,
release_group.secondary_types,
release_group.primary_type = 'Album' AND release_group.secondary_types is NULL as is_primary_album
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

-- CREATE VIEW song_search_view AS
-- select song.mbid as song_mbid,
-- album_view.mbid as album_mbid,
-- info_hash,
-- seeders,
-- is_primary_album,
-- song.plays,
-- concat(artist_name,' - ',album_view.title,' - ',song.title) as search_song
-- from song
-- inner join song_release_group
-- on song.mbid = song_release_group.song_mbid 
-- inner join album_view
-- on album_view.mbid = song_release_group.release_group_mbid;


CREATE VIEW artist_search_view AS 
select mbid as artist_mbid,
name as search_artist
from artist;



CREATE VIEW related_artist_view AS 
select artist1.mbid as artist1_mbid, 
artist1.name as artist1_name, 
artist2.mbid as artist2_mbid, 
artist2.name as artist2_name, 
tag_info1.count as count_1, 
tag_info2.count as count_2, 
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
-- where artist1.mbid like 'b7ffd2af-418f-4be2-bdd1-22f8b48613da' 
-- and artist2.mbid not like 'b7ffd2af-418f-4be2-bdd1-22f8b48613da' 

-- select * from related_artist_view where `mbid` like 'b7ffd2af-418f-4be2-bdd1-22f8b48613da';

CREATE VIEW artist_tag_view AS 
select artist.mbid, artist.name as artist_name, tag_info.count, tag.name, tag.id
from artist
left join tag_info
on artist.mbid = tag_info.artist_mbid
left join tag 
on tag_info.tag_id = tag.id
-- where artist.name like '%Bob Marley%'
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
tag_info1.count as count_1, 
tag_info2.count as count_2, 
tag.name, 
tag.id,
(tag_info1.tag_id*100/732) as score,
(
	select mbid from release_group
	where artist2.mbid = release_group.artist_mbid
	order by RAND()
	limit 1
) as rg_mbid,
(
	select song_mbid from song_release_group
	where song_release_group.release_group_mbid = 
	(
		select mbid from release_group
		where artist2.mbid = release_group.artist_mbid
		order by RAND()
		limit 1
	)
	order by RAND()
	limit 1
) as srg_song_mbid

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
on song.mbid = (
	select mbid from song
	where song.mbid = 
	(
		select song_mbid from song_release_group
		where song_release_group.release_group_mbid = 
		(
			select mbid from release_group
			where artist2.mbid = release_group.artist_mbid
			-- order by RAND()
			limit 1
		)
		-- order by RAND()
		limit 1
	)
	-- order by RAND()
	limit 1
)
where artist1.mbid = '9ad6d1e3-427f-4921-b603-7e9eda94a061'
and song.info_hash is not null

group by artist2.mbid
order by 
-- This one sorts by tag.id desc, meaning the weirdest categories
tag_info1.tag_id desc,
-- This one makes it more pertinent(NIN has the most votes for industrial)
tag_info1.count desc, 
-- This one does the second groups votes
tag_info2.count desc

limit 10;


CREATE VIEW related_song_view2 AS 
select artist1.mbid as artist1_mbid, 
artist1.name as artist1_name, 
artist2.mbid as artist2_mbid, 
artist2.name as artist2_name, 
-- rg.mbid as rg_mbid,
-- rg.title,
-- srg.id,
-- song.mbid,
-- song.title,
-- song.info_hash,
tag_info1.count as count_1, 
tag_info2.count as count_2, 
tag.name, 
tag.id as tag_id,
(tag_info1.tag_id*100/732) as score,
-- select random release group
(
	select mbid as rg_mbid
	from release_group
	where artist2_mbid = artist_mbid
	and artist_mbid is not null
	order by RAND()
	limit 1
) as rg_mbid,
(
	select song_mbid as srg_song_mbid
	from song_release_group
	where release_group_mbid = rg_mbid
	order by RAND()
	limit 1
) as srg_song_mbid
from artist as artist1
left join tag_info as tag_info1
on artist1.mbid = tag_info1.artist_mbid
left join tag 
on tag_info1.tag_id = tag.id
left join tag_info as tag_info2
on tag_info2.tag_id = tag.id
left join artist as artist2
on tag_info2.artist_mbid = artist2.mbid

where artist1.mbid = '9ad6d1e3-427f-4921-b603-7e9eda94a061' 
-- and song.info_hash is not null 


group by artist2.mbid
order by 
-- This one sorts by tag.id desc, meaning the weirdest categories
tag_info1.tag_id desc,
-- This one makes it more pertinent(NIN has the most votes for industrial)
tag_info1.count desc, 
-- This one does the second groups votes
tag_info2.count desc

limit 10;


select artist1.mbid as artist1_mbid, 
artist1.name as artist1_name, 
artist2.mbid as artist2_mbid, 
artist2.name as artist2_name, 
release_group.mbid as rg2_mbid,
-- rg.mbid as rg_mbid,
-- rg.title,
-- srg.id,
-- song.mbid,
-- song.title,
-- song.info_hash,
tag_info1.count as count_1, 
tag_info2.count as count_2, 
tag.name, 
tag.id as tag_id,
(tag_info1.tag_id*100/732) as score,
-- select random release group
(
	select mbid as rg_mbid
	from release_group
	where artist2_mbid = artist_mbid
	and artist_mbid is not null
	order by RAND()
	limit 1
) as rg_mbid,
(
	select song_mbid as srg_song_mbid
	from song_release_group
	where release_group_mbid = rg_mbid
	order by RAND()
	limit 1
) as srg_song_mbid
from artist as artist1
left join tag_info as tag_info1
on artist1.mbid = tag_info1.artist_mbid
left join tag 
on tag_info1.tag_id = tag.id
left join tag_info as tag_info2
on tag_info2.tag_id = tag.id
left join artist as artist2
on tag_info2.artist_mbid = artist2.mbid
left join release_group 
on artist2.mbid = release_group.artist_mbid
left join song_release_group

where artist1.mbid = '9ad6d1e3-427f-4921-b603-7e9eda94a061' 
-- and song.info_hash is not null 


group by artist2.mbid, rg_mbid, srg_song_mbid
having rg_mbid = release_group.mbid
and srg_song_mbid = song_release_group.song_mbid
order by 
-- This one sorts by tag.id desc, meaning the weirdest categories
tag_info1.tag_id desc,
-- This one makes it more pertinent(NIN has the most votes for industrial)
tag_info1.count desc, 
-- This one does the second groups votes
tag_info2.count desc

limit 10;







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
tag_info1.count as count_1, 
tag_info2.count as count_2, 
tag.name as tag_name, 
tag.id as tag_id,
(tag_info1.tag_id*100/732) as score,
(
	select mbid from release_group
	where artist2.mbid = release_group.artist_mbid
	-- order by rand()
	limit 1
) as rg_mbid,
(
	select song_mbid from song_release_group
	where song_release_group.release_group_mbid = 
	(
		select mbid from release_group
		where artist2.mbid = release_group.artist_mbid
		-- order by rand()
		limit 1
	)
	-- order by rand()
	limit 1
) as srg_song_mbid
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
on song.id = 
	(
	select id from song
	where song.mbid = 
	(
		select song_mbid from song_release_group
		where song_release_group.release_group_mbid = 
		(
			select mbid from release_group
			where artist2.mbid = release_group.artist_mbid
			-- order by rand()
			limit 1
		)

		-- order by rand()
		limit 1
	)
	-- order by id, rand()
	limit 1
)
where artist1.mbid = '9ad6d1e3-427f-4921-b603-7e9eda94a061' 
and song.info_hash is not null
-- and (tag_info1.tag_id*100/732) > 2
group by artist2.mbid
order by 

rand()

-- This one sorts by tag.id desc, meaning the weirdest categories
-- tag_info1.tag_id desc,
-- This one makes it more pertinent(NIN has the most votes for industrial)
-- tag_info1.count desc, 
-- This one does the second groups votes
-- tag_info2.count desc



limit 10;



-- explain speed up testing
-- album view
explain select release_group.mbid,
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

where artist_mbid = "66c662b6-6e2f-4930-8610-912e24c63ed1" 
group by release_group.mbid
order by year desc;

explain select *
from album_view
order by plays desc 
limit 4;

select * from album_view_fast 
where artist_mbid = "4a76400d-283f-492e-9754-18ef41755f81" 
AND (is_primary_album = true)
order by year desc;

explain select * 
from song_view_grouped where artist_mbid = "4a76400d-283f-492e-9754-18ef41755f81" 
order by plays desc;

explain select * 
from song_search_view 
where search_song like '%Skrillex%'
order by is_primary_album desc, plays desc limit 5;