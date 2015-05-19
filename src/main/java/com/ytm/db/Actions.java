package com.ytm.db;

import static com.ytm.db.Tables.SERIALIZED_DATA;
import static com.ytm.db.Tables.SONG;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.turn.ttorrent.tracker.TrackedTorrent;
import com.ytm.Tools;
import com.ytm.db.Tables.Song;

public class Actions {
	
	static final Logger log = LoggerFactory.getLogger(Actions.class);

	
	public static void saveTorrentToDB(TrackedTorrent t) {
		
		log.info("Saving " + t.getName() + " to the DB");
		// First get the MBID
		String mbid = "666";
		
		// Generate the magnet link
		String magnetLink = Tools.convertTorrentToMagnetLink(t);
		
		// Save the song
		Song song = SONG.create("magnet_link", magnetLink,
				"mbid", mbid);
		
		Boolean success = song.saveIt();
		
		// Serialize the torrent
		if (success) {
			String data = Tools.serializeTorrentFile(t);
			
			SERIALIZED_DATA.createIt("data",data);
		}
		
		
		
		
	}

}
