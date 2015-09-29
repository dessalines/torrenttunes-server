-- ---
-- Globals
-- ---

-- SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
-- SET FOREIGN_KEY_CHECKS=0;

-- ---
-- Table 'artist'
-- 
-- ---

DROP TABLE IF EXISTS `artist`;
    
CREATE TABLE `artist` (
  `mbid` VARCHAR(40) NOT NULL DEFAULT 'NULL',
  `name` VARCHAR(200) NOT NULL DEFAULT 'NULL',
  `image_url` VARCHAR(512) NULL DEFAULT NULL,
  `wikipedia_link` VARCHAR(512) NULL DEFAULT NULL,
  `allmusic_link` VARCHAR(512) NULL DEFAULT NULL,
  `official_homepage` VARCHAR(512) NULL DEFAULT NULL,
  `imdb` VARCHAR(512) NULL DEFAULT NULL,
  `lyrics` VARCHAR(512) NULL DEFAULT NULL,
  `youtube` VARCHAR(512) NULL DEFAULT NULL,
  `soundcloud` VARCHAR(lastfm) NULL DEFAULT NULL,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`mbid`),
KEY (`name`)
);

-- ---
-- Table 'release_group'
-- 
-- ---

DROP TABLE IF EXISTS `release_group`;
    
CREATE TABLE `release_group` (
  `mbid` VARCHAR(40) NOT NULL DEFAULT 'NULL',
  `title` VARCHAR(512) NOT NULL DEFAULT 'NULL',
  `artist_mbid` VARCHAR(40) NOT NULL DEFAULT 'NULL',
  `year` VARCHAR(6) NULL DEFAULT NULL,
  `wikipedia_link` VARCHAR(512) NULL DEFAULT NULL,
  `allmusic_link` VARCHAR(512) NULL DEFAULT NULL,
  `official_homepage` VARCHAR(512) NULL DEFAULT NULL,
  `lyrics` VARCHAR(512) NULL DEFAULT NULL,
  `album_coverart_url` VARCHAR(512) NULL DEFAULT NULL,
  `album_coverart_thumbnail_large` VARCHAR(512) NULL DEFAULT NULL,
  `album_coverart_thumbnail_small` VARCHAR(512) NULL DEFAULT NULL,
  `primary_type` VARCHAR(100) NULL DEFAULT NULL,
  `secondary_types` VARCHAR(512) NULL DEFAULT NULL,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`mbid`),
KEY (`title`)
);

-- ---
-- Table 'song_release_group'
-- 
-- ---

DROP TABLE IF EXISTS `song_release_group`;
    
CREATE TABLE `song_release_group` (
  `id` INTEGER NULL AUTO_INCREMENT DEFAULT NULL,
  `song_mbid` VARCHAR(40) NOT NULL DEFAULT 'NULL',
  `release_group_mbid` VARCHAR(40) NOT NULL DEFAULT 'NULL',
  `disc_number` INTEGER NULL DEFAULT NULL,
  `track_number` INTEGER NULL DEFAULT NULL,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`song_mbid`, `release_group_mbid`)
);

-- ---
-- Table 'song'
-- 
-- ---

DROP TABLE IF EXISTS `song`;
    
CREATE TABLE `song` (
  `mbid` VARCHAR(40) NOT NULL DEFAULT 'NULL',
  `torrent_path` VARCHAR(512) NOT NULL DEFAULT 'NULL',
  `info_hash` VARCHAR(100) NOT NULL DEFAULT 'NULL',
  `title` VARCHAR(512) NULL DEFAULT NULL,
  `duration_ms` INTEGER NULL DEFAULT NULL,
  `uploader_ip_hash` VARCHAR(100) NOT NULL DEFAULT 'NULL',
  `plays` INTEGER NOT NULL DEFAULT 0,
  `timeouts` INTEGER NOT NULL DEFAULT 0,
  `seeders` VARCHAR(20) NULL DEFAULT NULL,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`mbid`),
  UNIQUE KEY (`torrent_path`),
  UNIQUE KEY (`info_hash`),
KEY (`mbid`, `plays`),
KEY (`title`)
);

-- ---
-- Table 'tag_info'
-- 
-- ---

DROP TABLE IF EXISTS `tag_info`;
    
CREATE TABLE `tag_info` (
  `id` INTEGER NULL AUTO_INCREMENT DEFAULT NULL,
  `artist_mbid` VARCHAR(40) NOT NULL DEFAULT 'NULL',
  `count` INTEGER NOT NULL DEFAULT NULL,
  `tag_id` INTEGER NOT NULL DEFAULT NULL,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`artist_mbid`, `tag_id`)
);

-- ---
-- Table 'tag'
-- 
-- ---

DROP TABLE IF EXISTS `tag`;
    
CREATE TABLE `tag` (
  `id` INTEGER NULL AUTO_INCREMENT DEFAULT NULL,
  `name` VARCHAR(200) NOT NULL DEFAULT 'NULL',
  PRIMARY KEY (`id`)
);

-- ---
-- Foreign Keys 
-- ---

ALTER TABLE `release_group` ADD FOREIGN KEY (artist_mbid) REFERENCES `artist` (`mbid`);
ALTER TABLE `song_release_group` ADD FOREIGN KEY (song_mbid) REFERENCES `song` (`mbid`);
ALTER TABLE `song_release_group` ADD FOREIGN KEY (release_group_mbid) REFERENCES `release_group` (`mbid`);
ALTER TABLE `tag_info` ADD FOREIGN KEY (artist_mbid) REFERENCES `artist` (`mbid`);
ALTER TABLE `tag_info` ADD FOREIGN KEY (tag_id) REFERENCES `tag` (`id`);

-- ---
-- Table Properties
-- ---

-- ALTER TABLE `artist` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `release_group` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `song_release_group` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `song` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `tag_info` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
-- ALTER TABLE `tag` ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ---
-- Test Data
-- ---

-- INSERT INTO `artist` (`mbid`,`name`,`image_url`,`wikipedia_link`,`allmusic_link`,`official_homepage`,`imdb`,`lyrics`,`youtube`,`soundcloud`,`created`) VALUES
-- ('','','','','','','','','','','');
-- INSERT INTO `release_group` (`mbid`,`title`,`artist_mbid`,`year`,`wikipedia_link`,`allmusic_link`,`official_homepage`,`lyrics`,`album_coverart_url`,`album_coverart_thumbnail_large`,`album_coverart_thumbnail_small`,`primary_type`,`secondary_types`,`created`) VALUES
-- ('','','','','','','','','','','','','','');
-- INSERT INTO `song_release_group` (`id`,`song_mbid`,`release_group_mbid`,`disc_number`,`track_number`,`created`) VALUES
-- ('','','','','','');
-- INSERT INTO `song` (`mbid`,`torrent_path`,`info_hash`,`title`,`duration_ms`,`uploader_ip_hash`,`plays`,`timeouts`,`seeders`,`created`) VALUES
-- ('','','','','','','','','','');
-- INSERT INTO `tag_info` (`id`,`artist_mbid`,`count`,`tag_id`,`created`) VALUES
-- ('','','','','');
-- INSERT INTO `tag` (`id`,`name`) VALUES
-- ('','');