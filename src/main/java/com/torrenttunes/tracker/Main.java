package com.torrenttunes.tracker;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.torrenttunes.tracker.db.Actions;
import com.torrenttunes.tracker.db.InitializeTables;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;

public class Main {

	static final Logger log = LoggerFactory.getLogger(Main.class);

	
	Tracker tracker;

	
	public static void main(String[] args) {
		new Main().doMain(args);

	}
	
	public void doMain(String[] args) {
		
		// Initialize
		Tools.setupDirectories();
		
		Tools.copyResourcesToHomeDir(true);
		
		InitializeTables.initializeTables();
		
		
		startup();
		
	}

	public void startup() {

		// Startup the tracker
		createTracker();

		// First find all the torrent files stored on your tracker
		announceTorrentFiles(tracker);	

		// Start the tracker
		tracker.start();
		
		// Scan the folder every 10 minutes for new torrents, and add them to the db
		ScanTrackerService sts = ScanTrackerService.create(tracker);
		sts.startAsync();
		
		
	}

	public void createTracker() {
		try {
			tracker = new Tracker(new InetSocketAddress(6969));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void announceTorrentFiles(Tracker tracker) {
		
		for (File f : new File(DataSources.TORRENTS_DIR()).listFiles(Tools.TORRENT_FILE_FILTER)) {
			Tools.announceAndSaveTorrentFileToDB(tracker, f);
		}
		
	}


}
