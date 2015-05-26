


CREATE TABLE 'song' (
'id' INTEGER DEFAULT NULL PRIMARY KEY AUTOINCREMENT,
'magnet_link' TEXT NOT NULL  DEFAULT 'NULL',
'mbid' TEXT NOT NULL  DEFAULT 'NULL',
'title' TEXT DEFAULT NULL,
'artist' TEXT DEFAULT NULL,
'album' TEXT DEFAULT NULL,
'duration_ms' INTEGER DEFAULT NULL,
'album_coverart_url' TEXT DEFAULT NULL,
'album_coverart_thumbnail_large' TEXT DEFAULT NULL,
'album_coverart_thumbnail_small' TEXT DEFAULT NULL,
UNIQUE (magnet_link)
);


