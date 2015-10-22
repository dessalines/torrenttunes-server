-- CREATE TABLE `test_test` (
-- 	`id` INTEGER NULL AUTO_INCREMENT DEFAULT NULL,
-- 	PRIMARY KEY (`id`)
-- );

DROP TABLE IF EXISTS `song_view_fast`;

CREATE TABLE `song_view_fast` AS (select * from song_view);



