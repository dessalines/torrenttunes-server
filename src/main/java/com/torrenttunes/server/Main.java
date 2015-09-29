package com.torrenttunes.server;

import java.io.File;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.torrenttunes.client.LibtorrentEngine;
import com.torrenttunes.server.db.Actions;
import com.torrenttunes.server.tools.Tools;
import com.torrenttunes.server.webservice.WebService;

public class Main {

	static Logger log = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

	@Option(name="-uninstall",usage="Uninstall torrenttunes-client.(WARNING, this deletes your library)")
	private boolean uninstall;

	@Option(name="-loglevel", usage="Sets the log level [INFO, DEBUG, etc.]")
	private String loglevel = "INFO";
	
	@Option(name="-maintenance", usage="Redirects to the maintenance page")
	private boolean maintenanceRedirect;

	public void doMain(String[] args) {

		parseArguments(args);

		// See if the user wants to uninstall it
		if (uninstall) {
			Tools.uninstall();
		}
		
		if (maintenanceRedirect) {
			DataSources.BASE_ENDPOINT = DataSources.PAGES("maintenance");
		}


		log.setLevel(Level.toLevel(loglevel));
		log.getLoggerContext().getLogger("org.eclipse.jetty").setLevel(Level.OFF);
		log.getLoggerContext().getLogger("spark.webserver").setLevel(Level.OFF);


		// Initialize
		com.torrenttunes.client.tools.DataSources.APP_NAME = DataSources.APP_NAME;
		Tools.setupDirectories();

		Tools.copyResourcesToHomeDir(true);

		Tools.addExternalWebServiceVarToTools();

		com.torrenttunes.client.db.InitializeTables.initializeTables();

		// Startup the web service
		WebService.start();

		LibtorrentEngine.INSTANCE.startSeedingLibraryVersion1();

	}


	@Deprecated
	public static void saveTorrentFiles() {
		
		for (File f : new File(DataSources.TORRENTS_DIR()).listFiles(Tools.TORRENT_FILE_FILTER)) {
			Actions.saveTorrentFileToDB(f);
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
