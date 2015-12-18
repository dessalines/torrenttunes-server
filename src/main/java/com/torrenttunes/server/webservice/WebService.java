package com.torrenttunes.server.webservice;

import static spark.Spark.get;
import static spark.Spark.port;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Spark;

import com.torrenttunes.client.webservice.Platform;
import com.torrenttunes.server.DataSources;
import com.torrenttunes.server.tools.Tools;



public class WebService {
	static final Logger log = LoggerFactory.getLogger(WebService.class);

	
	
	public static void start() {
		
		
		
		com.torrenttunes.client.tools.DataSources.MUSIC_STORAGE_PATH = 
				DataSources.HOME_DIR() + "/music";

		if (DataSources.SSL) {
			Spark.secure(DataSources.KEYSTORE(), "changeit",null,null);
		}


		port(DataSources.INTERNAL_SPARK_WEB_PORT);
		
		com.torrenttunes.client.tools.DataSources.APP_NAME = DataSources.APP_NAME;
		Platform.setup();
		API.setup();
		
	
		get("/hello", (req, res) -> {
			Tools.allowOnlyLocalHeaders(req, res);
			return "hi from the torrenttunes-tracker web service";
		});
		
		
		get("/", (req, res) -> {
			Tools.allowAllHeaders(req, res);
			Tools.set15MinuteCache(req, res);
			
			return Tools.readFile(DataSources.BASE_ENDPOINT);
		});
		
		get("/es", (req, res) -> {
			Tools.allowAllHeaders(req, res);
			Tools.set15MinuteCache(req, res);
			
			return Tools.readFile(DataSources.MAIN_PAGE_URL_ES());
		});
		
		
		get("/*", (req, res) -> {
			Tools.allowAllHeaders(req, res);
			Tools.set15MinuteCache(req, res);
			
			String pageName = req.splat()[0];
			
			String webHomePath = DataSources.WEB_HOME() + "/" + pageName;
			
			Tools.setContentTypeFromFileName(pageName, res);
			
			return Tools.writeFileToResponse(webHomePath, res);
			
		});

		
	}
}
