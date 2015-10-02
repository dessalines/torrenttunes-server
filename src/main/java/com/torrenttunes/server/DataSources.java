package com.torrenttunes.server;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.torrenttunes.server.tools.Tools;


public class DataSources {

	public static String APP_NAME = "torrenttunes-server";
	
	public static Integer EXTERNAL_SPARK_WEB_PORT = 80; // Main is port 80, dev is port 4567
	
	// iptables are used to route all requests to 80 to 8080.
	public static Integer INTERNAL_SPARK_WEB_PORT = 8080;
	
	public static final String WEB_SERVICE_URL = "http://localhost:" + EXTERNAL_SPARK_WEB_PORT + "/";
	
	public static String EXTERNAL_IP = Tools.httpGetString("http://api.ipify.org/").trim();
	
	public static String EXTERNAL_URL = "http://" + EXTERNAL_IP + ":" + EXTERNAL_SPARK_WEB_PORT + "/";	
	
	public static final String TORRENTTUNES_IP = "torrenttunes.ml";
	
	public static final String TORRENTTUNES_PORT = "80";// Main is 80, dev is 4567
	
	public static final String TORRENTTUNES_URL = "http://" + TORRENTTUNES_IP + ":" + TORRENTTUNES_PORT + "/";
	
	public static final String TORRENTTUNES_INTERNAL_URL = "http://" + TORRENTTUNES_IP + ":" + INTERNAL_SPARK_WEB_PORT + "/";
	
	
	// The path to the ytm dir
	public static String HOME_DIR() {
		String userHome = System.getProperty( "user.home" ) + "/." + APP_NAME;
		return userHome;
	}
	
	public static final String TORRENTS_DIR() {return HOME_DIR() + "/torrents_server";}
	
	public static final String SAMPLE_TORRENT_FILE() {return TORRENTS_DIR() + 
			"/4dc5a38c-e053-4c12-a028-34b1a89075be_f29a2d2fb98be7afc69b0cb562fd5e38472fb44fa4790d1b11ff9565675d0fd6.torrent";
	}
	
	public static final String SAMPLE_MUSIC_DIR = "/home/tyler/Downloads";
	
	public static final String SAMPLE_SONG = SAMPLE_MUSIC_DIR + "/04 One Evening.mp3";	
	
	// This should not be used, other than for unzipping to the home dir
	public static final String CODE_DIR = System.getProperty("user.dir");
	
	public static final String SOURCE_CODE_HOME() {return HOME_DIR() + "/src";}
	
	public static final String SQL_FILE() {return SOURCE_CODE_HOME() + "/ddl_server.sql";}
	
	public static final String SQL_VIEWS_FILE() {return SOURCE_CODE_HOME() + "/views_server.sql";}
	
	public static final String SHADED_JAR_FILE = CODE_DIR + "/target/" + APP_NAME + ".jar";

	public static final String SHADED_JAR_FILE_2 = CODE_DIR + "/" + APP_NAME + ".jar";

	public static final String MUSIC_STORAGE_PATH  = HOME_DIR() + "/music";
	
	public static final String ZIP_FILE() {return HOME_DIR() + "/" + APP_NAME + ".zip";}
	
	public static final String TOOLS_JS() {return SOURCE_CODE_HOME() + "/web/js/tools.js";}
	
	// Web pages
	public static final String WEB_HOME() {return SOURCE_CODE_HOME() + "/web";}

	public static final String WEB_HTML() {return WEB_HOME() + "/html";}
	
	public static final String MAIN_PAGE_URL_EN() {return WEB_HTML() + "/main_en.html";}
	
	public static final String MAIN_PAGE_URL_ES() {return WEB_HTML() + "/main_es.html";}
	
	public static final String MAINTENANCE_PAGE_URL() {return WEB_HTML() + "/maintenance.html";}
	
	public static String BASE_ENDPOINT = MAIN_PAGE_URL_EN();
	
	
	public static final Set<String> NON_STREAMING_BROWSERS = new HashSet<String>(Arrays.asList(
			"Firefox", "Android"));
	
	public static final Date APP_START_DATE = new Date();
	
	public static final String DB_PROP_FILE  = System.getProperty("user.home") + "/tt_db.properties";

	public static final Properties DB_PROP = Tools.loadProperties(DB_PROP_FILE);

	
}
