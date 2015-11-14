package com.torrenttunes.server.tools;

import static com.torrenttunes.server.db.Tables.SONG;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.torrenttunes.client.LibtorrentEngine;
import com.torrenttunes.client.tools.Tools;
import com.torrenttunes.server.DataSources;
import com.torrenttunes.server.db.Tables.Song;

// java -cp target/torrenttunes-server.jar com.torrenttunes.server.tools.ChangeTorrentTrackers
public class ChangeTorrentTrackers {

	static final Logger log = LoggerFactory.getLogger(ChangeTorrentTrackers.class);

	
	public static void main(String[] args) {
		com.torrenttunes.client.tools.DataSources.APP_NAME = DataSources.APP_NAME;
		
		saveTorrents();
		
	}
		
		
		public static void saveTorrents() {
			try {
				LibtorrentEngine lte = LibtorrentEngine.INSTANCE;
				Tools.dbInit();
				List<Song> torrentFiles = SONG.findAll();

				for (Song l : torrentFiles) {
					String torrentPath = l.getString("torrent_path");
					log.info("Editing torrent_path: " + torrentPath);
					File torrentFile = new File(torrentPath);
					com.torrenttunes.client.tools.ChangeTorrentTracker.updateTrackerForTorrent(torrentFile);
				}

			} catch(Exception e) {
				e.printStackTrace();			
			} finally {
				Tools.dbClose();
			}
	}
}
