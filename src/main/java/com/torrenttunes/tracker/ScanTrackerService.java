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
public class ScanTrackerService extends AbstractScheduledService {

	static final Logger log = LoggerFactory.getLogger(ScanTrackerService.class);

	private Tracker tracker;

	public static ScanTrackerService create(Tracker tracker) {
		return new ScanTrackerService(tracker);
	}

	private ScanTrackerService(Tracker tracker) {
		this.tracker = tracker;
	}

	@Override
	protected void runOneIteration() throws Exception {

		Tools.dbInit();
		List<Song> songs = SONG.findAll();
		songs.isEmpty(); // I don't know why this is necessary, but it glitches out when its not there
		Tools.dbClose();

		Map<String, String> torrentNamesToFileMap = getTorrentNamesToFileMap();

		Set<String> torrentDBNames = getTorrentNamesFromDB(songs);

		Set<String> torrentNames = torrentNamesToFileMap.keySet();
		
		// Only add the ones that aren't already in the DB
		torrentNames.removeAll(torrentDBNames);
		
		Integer cnt = torrentNames.size();
		if (cnt > 0) {
			log.info("Found " + cnt + " new torrents, adding them to the tracker and DB");
		} else {
			log.info("No new torrents found.");
		}

		for (String torrentName : torrentNames) {
			File torrentFile = new File(torrentNamesToFileMap.get(torrentName));
			Tools.announceAndSaveTorrentFileToDB(tracker, torrentFile);
		}




		// Save to the DB


	}

	public static Map<String, String> getTorrentNamesToFileMap() {
		Map<String, String> torrentNamesToFileMap = new HashMap<String, String>();

		try {
			File[] files = new File(DataSources.TORRENTS_DIR()).listFiles(Tools.TORRENT_FILE_FILTER);

			for (File file : files) {
				if (file.isFile()) {
					Torrent t = Torrent.load(file);
					torrentNamesToFileMap.put(t.getName(), file.getAbsolutePath());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return torrentNamesToFileMap;
	}

	/**
	 * I figured out this was unusable, since I have to scan the torrent directory(IE, the error that
	 * a torrent must be in the directory, before a peer can even announce.
	 */
	@Deprecated
	private void scanTracker() {
		// Query the DB for new 
		log.info("Scanning tracker for new Torrents...");

		// Do a disjoint between the DB list, and the tracker list (Set<infohashes>)
		Map<String, TrackedTorrent> infoHashToTorrentMap = getInfoHashes(tracker.getTrackedTorrents());

		Tools.dbInit();
		List<Song> songs = SONG.findAll();
		songs.isEmpty(); // I don't know why this is necessary, but it glitches out when its not there
		Tools.dbClose();

		Set<String> dbInfoHashes = getInfoHashesFromDB(songs);

		// Found something thats not in the DB, you need to add it		
		Set<String> missingInfoHashes = infoHashToTorrentMap.keySet();
		missingInfoHashes.removeAll(dbInfoHashes);

		Integer cnt = missingInfoHashes.size();
		if (cnt > 0) {
			log.info("Found " + cnt + " missing torrents, adding them to the DB.");
		} else {
			log.info("No new torrents found.");
		}

		for (String missingInfoHash : missingInfoHashes) {
			Tools.dbInit();
//			Actions.saveTorrentToDB(infoHashToTorrentMap.get(missingInfoHash));
			Tools.dbClose();
		}

	}

	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(3, 10, TimeUnit.SECONDS);
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

	public static Set<String> getTorrentNamesFromDB(List<Song> songs) {
		Set<String> names = new HashSet<String>();

		for (Song song : songs) {

			String magnetLink = song.getString("magnet_link");

			String name = Tools.extractNameFromMagnetLink(magnetLink);
			names.add(name);
		}

		return names;
	}

	public static Map<String, TrackedTorrent> getInfoHashes(Collection<TrackedTorrent> tts) {

		Map<String, TrackedTorrent> infoHashToTorrentMap = 
				new HashMap<String, TrackedTorrent>();

		for (TrackedTorrent tt : tts) {
			infoHashToTorrentMap.put(tt.getHexInfoHash().toLowerCase(), tt);
		}



		return infoHashToTorrentMap;
	}



}
