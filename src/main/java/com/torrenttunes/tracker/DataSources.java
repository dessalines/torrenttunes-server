package com.torrenttunes.tracker;

public class DataSources {

	public static String APP_NAME = "torrenttunes-tracker";
	
	public static Integer SPARK_WEB_PORT = 4567;
	
	
	// The path to the ytm dir
	public static String HOME_DIR() {
		String userHome = System.getProperty( "user.home" ) + "/." + APP_NAME;
		return userHome;
	}
	
	public static final String TORRENTS_DIR() {return HOME_DIR() + "/torrents";}
	
	public static final String SAMPLE_TORRENT_FILE() {return TORRENTS_DIR() + 
			"/4dc5a38c-e053-4c12-a028-34b1a89075be_f29a2d2fb98be7afc69b0cb562fd5e38472fb44fa4790d1b11ff9565675d0fd6.torrent";
	}
	
	public static final String DB_FILE() {return HOME_DIR() + "/db/db.sqlite";}
	
	
	// This should not be used, other than for unzipping to the home dir
	public static final String CODE_DIR = System.getProperty("user.dir");
	
	public static final String SOURCE_CODE_HOME() {return HOME_DIR() + "/src";}
	
	public static final String SQL_FILE() {return SOURCE_CODE_HOME() + "/ddl.sql";}
	
	public static final String SHADED_JAR_FILE = CODE_DIR + "/target/" + APP_NAME + ".jar";

	public static final String SHADED_JAR_FILE_2 = CODE_DIR + "/" + APP_NAME + ".jar";
	
	public static final String ZIP_FILE() {return HOME_DIR() + "/" + APP_NAME + ".zip";}
	
	// Web pages
	public static final String WEB_HOME() {return SOURCE_CODE_HOME() + "/web";}

	public static final String WEB_HTML() {return WEB_HOME() + "/html";}
	
	
}
