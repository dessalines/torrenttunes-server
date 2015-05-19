package com.ytm;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import com.ytm.db.Actions;
import com.ytm.db.InitializeTables;

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
	}

	public void createTracker() {
		try {
			tracker = new Tracker(new InetSocketAddress(6969));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void announceTorrentFiles(Tracker tracker) {
		try {
			for (File f : new File(DataSources.TORRENTS_DIR()).listFiles(Tools.TORRENT_FILE_FILTER)) {
				log.info("Announcing file: " + f.getName());
				TrackedTorrent tt = TrackedTorrent.load(f);
				tracker.announce(tt);
				
				// Save to the DB
				Actions.saveTorrentToDB(tt);
				
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}
