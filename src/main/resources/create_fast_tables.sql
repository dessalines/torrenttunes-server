-- CREATE TABLE `test_test` (
-- 	`id` INTEGER NULL AUTO_INCREMENT DEFAULT NULL,
-- 	PRIMARY KEY (`id`)
-- );

DROP TABLE IF EXISTS `song_view_fast`;

CREATE TABLE `song_view_fast` AS 
select torrent_path,
info_hash, 
song_mbid,
title, 
release_group_mbid,
duration_ms, 
plays,
disc_number,
track_number,
album,
artist_mbid,
artist,
year,
album_coverart_url,
album_coverart_thumbnail_large,
album_coverart_thumbnail_small,
seeders,
search_song,
primary_type,
secondary_types,
is_primary_album
from song_view
;



