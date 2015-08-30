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
seeders
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
select artist1.mbid, 
artist1.name, 
artist2.mbid, 
artist2.name, 
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
-- where artist1.name like '%Deftones%'
-- and artist2.name not like '%Deftones%'
group by artist2.mbid
order by 
-- This one sorts by tag.id desc, meaning the weirdest categories
tag_info1.tag_id desc,
-- This one makes it more pertinent(NIN has the most votes for industrial)
tag_info1.count desc, 
-- This one does the second groups votes
tag_info2.count desc
limit 10;

CREATE VIEW artist_tag_view AS 
select artist.mbid, tag_info.count, tag.name, tag.id
from artist
left join tag_info
on artist.mbid = tag_info.artist_mbid
left join tag 
on tag_info.tag_id = tag.id
-- where artist.name like '%Deftones%'
order by artist.mbid, tag_info.count desc, tag.id desc
;




