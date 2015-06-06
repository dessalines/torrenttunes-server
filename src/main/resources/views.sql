CREATE VIEW song_view as 
select torrent_path,
info_hash, 
song.mbid as song_mbid,
song.title, 
release_mbid,
duration_ms, 
plays,
release.title as release_title,
artist_mbid,
artist.name as artist_name
from song
inner join release
on song.release_mbid = release.mbid
inner join artist 
on release.artist_mbid = artist.mbid;



CREATE VIEW search_view AS 
select *, artist_name || ' - ' || release_title || ' - ' ||  title as search
from song_view;

