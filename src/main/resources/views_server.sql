CREATE VIEW song_view as 
select torrent_path,
info_hash, 
song.mbid as song_mbid,
song.title, 
release_group_mbid,
duration_ms, 
plays,
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
inner join release_group
on song.release_group_mbid = release_group.mbid
inner join artist 
on release_group.artist_mbid = artist.mbid;

CREATE VIEW song_search_view AS
select song.mbid as song_mbid,
release_group.mbid as album_mbid,
info_hash,
seeders,
artist.name || ' - ' || release_group.title || ' - ' ||  song.title as search_song
from song
inner join release_group
on song.release_group_mbid = release_group.mbid
inner join artist 
on release_group.artist_mbid = artist.mbid;

CREATE VIEW album_search_view AS
select release_group.mbid as album_mbid,
artist.name || ' - ' || release_group.title as search_album
from release_group
inner join artist 
on release_group.artist_mbid = artist.mbid;

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
count(song.id) as number_of_songs,
sum(plays) as plays
from release_group
left join song
on song.release_group_mbid = release_group.mbid
inner join artist
on release_group.artist_mbid = artist.mbid
group by release_group.mbid;


