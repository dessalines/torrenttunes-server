


CREATE TABLE 'song' (
'id' INTEGER DEFAULT NULL PRIMARY KEY AUTOINCREMENT,
'torrent_path' TEXT NOT NULL  DEFAULT 'NULL',
'info_hash' TEXT NOT NULL  DEFAULT 'NULL',
'mbid' TEXT NOT NULL  DEFAULT 'NULL',
'title' TEXT DEFAULT NULL,
'release_mbid' TEXT DEFAULT NULL REFERENCES 'release' ('mbid'),
'duration_ms' INTEGER DEFAULT NULL,
'track_number' INTEGER DEFAULT NULL,
'plays' INTEGER NOT NULL  DEFAULT 0,
UNIQUE (torrent_path),
UNIQUE (info_hash)
);

CREATE TABLE 'release' (
'mbid' TEXT NOT NULL  DEFAULT 'NULL' PRIMARY KEY,
'title' TEXT NOT NULL  DEFAULT 'NULL',
'artist_mbid' TEXT NOT NULL  DEFAULT 'NULL' REFERENCES 'artist' ('mbid'),
'year' TEXT DEFAULT NULL,
'wikipedia_link' TEXT DEFAULT NULL,
'allmusic_link' TEXT DEFAULT NULL,
'album_coverart_url' TEXT DEFAULT NULL,
'album_coverart_thumbnail_large' TEXT DEFAULT NULL,
'album_coverart_thumbnail_small' TEXT DEFAULT NULL
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


