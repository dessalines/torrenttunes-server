CREATE TABLE `test` (
	`name` VARCHAR(200) NOT NULL DEFAULT 'NULL',
	PRIMARY KEY (`name`)
);

DROP TABLE IF EXISTS `song_view_fast`
;

CREATE TABLE `song_view_fast` select * from `song_view`
;

DROP TABLE IF EXISTS `song_view_grouped_fast`
;

CREATE TABLE `song_view_grouped_fast` select * from `song_view_grouped`
;

DROP TABLE IF EXISTS `album_view_fast`
;

CREATE TABLE `album_view_fast` select * from `album_view`
;

DROP TABLE IF EXISTS `artist_search_view_fast`
; 

CREATE TABLE `artist_search_view_fast` select * from `artist_search_view`
;

