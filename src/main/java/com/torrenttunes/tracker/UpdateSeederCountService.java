package com.torrenttunes.tracker;

import static com.torrenttunes.tracker.db.Tables.SONG;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.torrenttunes.tracker.db.Actions;
import com.torrenttunes.tracker.db.Tables.Song;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;

@Deprecated
public class UpdateSeederCountService extends AbstractScheduledService {

	static final Logger log = LoggerFactory.getLogger(UpdateSeederCountService.class);

	private Tracker tracker;

	public static UpdateSeederCountService create(Tracker tracker) {
		return new UpdateSeederCountService(tracker);
	}

	private UpdateSeederCountService(Tracker tracker) {
		this.tracker = tracker;
	}

	@Override
	protected void runOneIteration() throws Exception {
		
		log.info("Updating torrent seeder counts in database");
		
		// Get a map of the tracked torrents, infoHashToTrackedTorrent
		Map<String, TrackedTorrent> infoHashToTorrentMap = infoHashToTorrentMap();
		log.info(infoHashToTorrentMap.toString());
		
		Tools.dbInit();
		List<Song> songs = SONG.findAll();
		songs.get(0);
		for (Song s : songs) {
			
			// Get the torrent
			TrackedTorrent torrent = infoHashToTorrentMap.get(s.get("info_hash"));
			
			log.info(torrent.getName());
			log.info(String.valueOf(torrent.seeders()));
			
			// Set the seeder count
			s.set("seeders", torrent.seeders());
		
		}
		
		Tools.dbClose();
		
	}



	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(0, 30, TimeUnit.SECONDS);
	}
	
	public Map<String, TrackedTorrent> infoHashToTorrentMap() {
		Map<String, TrackedTorrent> map = new HashMap<String, TrackedTorrent>();
		
		for (TrackedTorrent t : tracker.getTrackedTorrents()) {
			map.put(t.getHexInfoHash().toLowerCase(), t);
		}
		
		return map;
	}

	public static Set<String> getInfoHashesFromDB(List<Song> songs) {
		Set<String> dbInfoHashes = new HashSet<String>();

		for (Song song : songs) {

			String magnetLink = song.getString("magnet_link");

			String infoHash = Tools.extractInfoHashFromMagnetLink(magnetLink);
			dbInfoHashes.add(infoHash);
		}

		return dbInfoHashes;
	}




}
