package com.torrenttunes.tracker.webservice;

import static com.torrenttunes.tracker.db.Tables.*;
import static spark.Spark.get;
import static spark.Spark.post;

import java.io.File;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;

import com.torrenttunes.tracker.DataSources;
import com.torrenttunes.tracker.Tools;
import com.torrenttunes.tracker.db.Actions;
import com.turn.ttorrent.tracker.Tracker;

public class API {

	static final Logger log = LoggerFactory.getLogger(Platform.class);


	public static void setup(Tracker tracker) {

		post("/torrent_upload", (req, res) -> {

			try {

				// apache commons-fileupload to handle file upload
				DiskFileItemFactory factory = new DiskFileItemFactory();
				factory.setRepository(new File(DataSources.TORRENTS_DIR()));
				ServletFileUpload fileUpload = new ServletFileUpload(factory);



				List<FileItem> items = fileUpload.parseRequest(req.raw());

				// image is the field name that we want to save
				FileItem item = items.stream()
						.filter(e -> "torrent".equals(e.getFieldName()))
						.findFirst().get();
				String fileName = item.getName();
				


				File torrentFile = new File(DataSources.TORRENTS_DIR(), fileName);
				item.write(torrentFile);

				
				Tools.announceAndSaveTorrentFileToDB(tracker, torrentFile);
				
				// a first test
				
				log.info(fileName);


				return null;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} 



		});




		post("/torrent_info_upload", (req, res) -> {

			try {
				log.info(req.body());
				log.info(req.params().toString());
				
				String json = req.body();
				
				JsonNode jsonNode = Tools.jsonToNode(json);
				
				Tools.dbInit();
				Actions.updateSongInfo(jsonNode);
				Tools.dbClose();
				
				return "Saved info";
			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} 
		});
		
		get("/get_songs", (req, res) -> {

			try {
	
				Tools.dbInit();
				String json = SONG.findAll().toJson(false);
				Tools.dbClose();
				
				return json;
			
			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} 
		});
		
		
		
		get("/song_search/:query", (req, res) -> {

			try {
				Tools.logRequestInfo(req);
				Tools.allowAllHeaders(req, res);
				Tools.dbInit();

				String query = req.params(":query");
				query = java.net.URLDecoder.decode(query, "UTF-8");
				String json = null;
				
				log.info(query);
				
				String[] splitWords = query.split(" ");
				StringBuilder queryStr = new StringBuilder();
				for(int i = 0;;) {
					String word = "search like '%" + splitWords[i++] + "%'";
					queryStr.append(word);
					
					if (i < splitWords.length) {
						queryStr.append(" and ");
					} else {
						break;
					}
							
				}
				
				log.info(queryStr.toString());

				json = SEARCH_VIEW.find(queryStr.toString()).limit(10).toJson(false);

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});


	}

}
