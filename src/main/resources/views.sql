CREATE VIEW song_view as 
select torrent_path,
info_hash, 
song.mbid as song_mbid,
song.title, 
release_mbid,
duration_ms, 
plays,
track_number,
release.title as album,
artist_mbid,
artist.name as artist,
year,
album_coverart_url,
album_coverart_thumbnail_large,
album_coverart_thumbnail_small,
seeders
from song
inner join release
on song.release_mbid = release.mbid
inner join artist 
on release.artist_mbid = artist.mbid;

CREATE VIEW song_search_view AS
select song.mbid as song_mbid,
artist.name || ' - ' || release.title || ' - ' ||  song.title as search
from song
inner join release
on song.release_mbid = release.mbid
inner join artist 
on release.artist_mbid = artist.mbid;

CREATE VIEW album_search_view AS
select release.mbid as album_mbid,
artist.name || ' - ' || release.title as search
from release
inner join artist 
on release.artist_mbid = artist.mbid;

CREATE VIEW artist_search_view AS 
select mbid as artist_mbid,
name as search
from artist;

-- this includes song counts and play times
CREATE VIEW album_view AS
select release.mbid,
release.title,
release.artist_mbid,
artist.name as artist_name,
year,
release.wikipedia_link,
release.allmusic_link,
album_coverart_url,
album_coverart_thumbnail_large,
album_coverart_thumbnail_small,
count(song.id) as number_of_songs,
sum(plays) as plays
from release
inner join song
on song.release_mbid = release.mbid
inner join artist
on release.artist_mbid = artist.mbid
group by release.mbid;


