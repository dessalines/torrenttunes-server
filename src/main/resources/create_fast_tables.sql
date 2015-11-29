DROP TABLE IF EXISTS `song_view_fast`;

CREATE TABLE `song_view_fast` AS (select * from `song_view`);

DROP TABLE IF EXISTS `song_view_grouped_fast`;

CREATE TABLE `song_view_grouped_fast` AS (select * from `song_view_grouped`);

DROP TABLE IF EXISTS `album_view_fast`;

CREATE TABLE `album_view_fast` AS (select * from `album_view`);