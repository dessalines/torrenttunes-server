package com.torrenttunes.tracker.db;

import static com.torrenttunes.tracker.db.Tables.SONG;

import java.io.File;

import org.codehaus.jackson.JsonNode;
import org.javalite.activejdbc.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.torrenttunes.tracker.Tools;
import com.torrenttunes.tracker.db.Tables.Song;
import com.turn.ttorrent.tracker.TrackedTorrent;

public class Actions {

	static final Logger log = LoggerFactory.getLogger(Actions.class);


	public static void saveTorrentToDB(File torrentFile, TrackedTorrent t) {

		try {

			log.info("Saving " + torrentFile.getName() + " to the DB");


			// First get the MBID from the filename
			String mbid = torrentFile.getName().split("_")[0];

			// Generate the magnet link
			String magnetLink = Tools.convertTorrentToMagnetLink(t);

			// Save the song
			Song song = SONG.create("magnet_link", magnetLink,
					"mbid", mbid);



			Boolean success = song.saveIt();

		} catch(DBException e) {		
			if (e.getMessage().contains("[SQLITE_CONSTRAINT]")) {
				log.error("Not adding " + torrentFile.getName() + ", Song was already in the DB");
			} else {
				e.printStackTrace();
			}
		}

	}


	public static void updateSongInfo(JsonNode jsonNode) {

		// Get the variables
		String mbid = jsonNode.get("mbid").asText();
		String title = jsonNode.get("title").asText();
		String artist = jsonNode.get("artist").asText();
		String album = jsonNode.get("album").asText();
		Long durationMS = jsonNode.get("duration_ms").asLong();
		String albumArtUrl = jsonNode.get("album_coverart_url").asText();
		if (albumArtUrl.equals("null")) {
			albumArtUrl = null;
		}
		String largeThumbnail = jsonNode.get("album_coverart_thumbnail_large").asText();
		if (largeThumbnail.equals("null")) {
			largeThumbnail = null;
		}
		String smallThumbnail = jsonNode.get("album_coverart_thumbnail_small").asText();
		if (smallThumbnail.equals("null")) {
			smallThumbnail = null;
		}

		// Find it by the MBID
		Song song = SONG.findFirst("mbid = ?", mbid);

		song.set("title", title,
				"artist", artist,
				"album", album,
				"duration_ms", durationMS,
				"album_coverart_url", albumArtUrl,
				"album_coverart_thumbnail_large", largeThumbnail,
				"album_coverart_thumbnail_small", smallThumbnail);
		song.saveIt();


	}

}
