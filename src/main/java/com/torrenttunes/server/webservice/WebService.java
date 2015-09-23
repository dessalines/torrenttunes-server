package com.torrenttunes.server.webservice;

import static spark.Spark.get;
import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.torrenttunes.client.webservice.Platform;
import com.torrenttunes.server.DataSources;
import com.torrenttunes.server.tools.Tools;



public class WebService {
	static final Logger log = LoggerFactory.getLogger(WebService.class);

	
	
	public static void start() {
		

		com.torrenttunes.client.tools.DataSources.MUSIC_STORAGE_PATH = 
				DataSources.HOME_DIR() + "/music";

//				setupSSL();

		// Add external web service url to beginning of javascript tools
		//		Tools.addExternalWebServiceVarToTools();

		port(DataSources.SPARK_WEB_PORT);
		

		externalStaticFileLocation(DataSources.WEB_HOME());
//		externalStaticFileLocation(com.torrenttunes.client.tools.DataSources.MUSIC_STORAGE_PATH);
		
		com.torrenttunes.client.tools.DataSources.APP_NAME = DataSources.APP_NAME;
		Platform.setup();
		API.setup();
		
	
		get("/hello", (req, res) -> {
			Tools.allowOnlyLocalHeaders(req, res);
			return "hi from the torrenttunes-tracker web service";
		});
		
		get("/:page", (req, res) -> {
			Tools.allowAllHeaders(req, res);
			String pageName = req.params(":page");
			return Tools.readFile(DataSources.PAGES(pageName));
		});
		
		get("/", (req, res) -> {
			Tools.allowAllHeaders(req, res);
			return Tools.readFile(DataSources.PAGES("main"));
		});
	
	}
}
