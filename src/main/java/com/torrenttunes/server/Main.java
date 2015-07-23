package com.torrenttunes.server;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.torrenttunes.client.LibtorrentEngine;
import com.torrenttunes.server.db.InitializeTables;
import com.torrenttunes.server.webservice.WebService;
import com.turn.ttorrent.tracker.Tracker;

public class Main {

	static Logger log = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

	@Option(name="-uninstall",usage="Uninstall torrenttunes-client.(WARNING, this deletes your library)")
	private boolean uninstall;
	
	@Option(name="-loglevel", usage="Sets the log level [INFO, DEBUG, etc.]")     
	private String loglevel = "INFO";
	
	Tracker tracker;

	
	public void doMain(String[] args) {
		
		parseArguments(args);
		
		// See if the user wants to uninstall it
		if (uninstall) {
			Tools.uninstall();
		}
		
		
		log.setLevel(Level.toLevel(loglevel));
		
		// Initialize
		com.torrenttunes.client.tools.DataSources.APP_NAME = DataSources.APP_NAME;
		Tools.setupDirectories();
		
		Tools.copyResourcesToHomeDir(true);
		
		Tools.addExternalWebServiceVarToTools();
		
		
		InitializeTables.initializeTables();
		com.torrenttunes.client.db.InitializeTables.initializeTables();
		
		// Startup the tracker
		createTracker();

		// First find all the torrent files stored on your tracker
		announceTorrentFiles(tracker);	

		// Start the tracker
		tracker.start();
		
		// Update the seeder counts in the db
//		UpdateSeederCountService.create(tracker).startAsync();
		
		// Start up the client(used for caching requests from the web)
//		com.torrenttunes.client.Main.main(new String[]{});
		
		
		// Startup the web service
		WebService.start(tracker);
		
		LibtorrentEngine.INSTANCE.startSeedingLibrary();
		
	
		
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
			Tools.saveTorrentFileToDB(f);

		}
		
	}
	
	private void parseArguments(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);

		try {

			parser.parseArgument(args);

		} catch (CmdLineException e) {
			// if there's a problem in the command line,
			// you'll get this exception. this will report
			// an error message.
			System.err.println(e.getMessage());
			System.err.println("java -jar torrenttunes-client.jar [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();
			System.exit(0);


			return;
		}
	}
	

	public static void main(String[] args) {
		new Main().doMain(args);
	
	}


}
