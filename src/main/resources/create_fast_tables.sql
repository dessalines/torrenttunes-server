-- CREATE TABLE `test_test` (
-- 	`id` INTEGER NULL AUTO_INCREMENT DEFAULT NULL,
-- 	PRIMARY KEY (`id`)
-- );

DROP TABLE IF EXISTS `song_view_fast`;

CREATE TABLE `song_view_fast` AS 
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



