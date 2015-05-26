package com.torrenttunes.tracker.webservice;

import static spark.Spark.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;





import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;









import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jackson.JsonNode;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;














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



	}

}
