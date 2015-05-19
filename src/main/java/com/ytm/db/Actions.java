package com.ytm.db;

import static com.ytm.db.Tables.SERIALIZED_DATA;
import static com.ytm.db.Tables.SONG;

import java.util.NoSuchElementException;

import org.javalite.activejdbc.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.turn.ttorrent.tracker.TrackedTorrent;
import com.ytm.Tools;
import com.ytm.db.Tables.Song;

public class Actions {
	
	static final Logger log = LoggerFactory.getLogger(Actions.class);

	
	public static void saveTorrentToDB(TrackedTorrent t) {
		
		try {
			
		
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
		
		} catch(DBException e) {		
			if (e.getMessage().contains("[SQLITE_CONSTRAINT]")) {
				log.error("Not adding " + t.getName() + ", Song was already in the DB");
			} else {
				e.printStackTrace();
			}
		}
		
		
		
		
	}

}
