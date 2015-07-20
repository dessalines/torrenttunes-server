CREATE TABLE 'song' (
'id' INTEGER DEFAULT NULL PRIMARY KEY AUTOINCREMENT,
'torrent_path' TEXT NOT NULL  DEFAULT 'NULL',
'info_hash' TEXT NOT NULL  DEFAULT 'NULL',
'mbid' TEXT NOT NULL  DEFAULT 'NULL',
'title' TEXT DEFAULT NULL,
'duration_ms' INTEGER DEFAULT NULL,
'plays' INTEGER NOT NULL  DEFAULT 0,
'seeders' TEXT DEFAULT NULL,
UNIQUE (torrent_path),
UNIQUE (info_hash),
UNIQUE (mbid)
);

CREATE TABLE 'release_group' (
'mbid' TEXT NOT NULL  DEFAULT 'NULL' PRIMARY KEY REFERENCES 'song_release_group' ('release_group_mbid'),
'title' TEXT NOT NULL  DEFAULT 'NULL',
'artist_mbid' TEXT NOT NULL  DEFAULT 'NULL' REFERENCES 'artist' ('mbid'),
'year' TEXT DEFAULT NULL,
'wikipedia_link' TEXT DEFAULT NULL,
'allmusic_link' TEXT DEFAULT NULL,
'official_homepage' TEXT DEFAULT NULL,
'lyrics' TEXT DEFAULT NULL,
'album_coverart_url' TEXT DEFAULT NULL,
'album_coverart_thumbnail_large' TEXT DEFAULT NULL,
'album_coverart_thumbnail_small' TEXT DEFAULT NULL,
'primary_type' TEXT DEFAULT NULL,
'secondary_types' TEXT DEFAULT NULL
);

CREATE TABLE 'artist' (
'mbid' TEXT NOT NULL  DEFAULT 'NULL' PRIMARY KEY,
'name' TEXT NOT NULL  DEFAULT 'NULL',
'image_url' TEXT DEFAULT NULL,
'wikipedia_link' TEXT DEFAULT NULL,
'allmusic_link' TEXT DEFAULT NULL,
'official_homepage' TEXT DEFAULT NULL,
'imdb' TEXT DEFAULT NULL,
'lyrics' TEXT DEFAULT NULL,
'youtube' TEXT DEFAULT NULL,
'soundcloud' TEXT DEFAULT NULL,
'lastfm' TEXT DEFAULT NULL
);

CREATE TABLE 'song_release_group' (
'id' INTEGER DEFAULT NULL PRIMARY KEY AUTOINCREMENT,
'song_mbid' TEXT DEFAULT NULL REFERENCES 'song' ('mbid'),
'release_group_mbid' TEXT DEFAULT NULL,
'disc_number' INTEGER DEFAULT NULL,
'track_number' INTEGER DEFAULT NULL,
UNIQUE (release_group_mbid, song_mbid)
);

