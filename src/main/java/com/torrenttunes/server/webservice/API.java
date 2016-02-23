package com.torrenttunes.server.webservice;

import static com.torrenttunes.server.db.Tables.*;
import static spark.Spark.get;
import static spark.Spark.post;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.torrenttunes.server.DataSources;
import com.torrenttunes.server.db.Actions;
import com.torrenttunes.server.db.Transformations;
import com.torrenttunes.server.db.Tables.SongView;
import com.torrenttunes.server.tools.Tools;


public class API {

	static final Logger log = LoggerFactory.getLogger(API.class);


	public static void setup() {

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


				Actions.saveTorrentFileToDB(torrentFile);

				// a first test

				log.info(fileName);


				return "success";

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


				Actions.updateSongInfo(jsonNode);


				return "Saved info";
			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			}
		});

		post("/add_play_count/:infoHash", (req, res) -> {

			try {
				Tools.allowAllHeaders(req, res);
				String infoHash = req.params(":infoHash");


				Tools.dbInit();
				Actions.addToPlayCount(infoHash);
				Tools.dbClose();

				log.info("added to play count for infohash: " + infoHash);

				return "Added play count";
			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {

			}
		});

		post("/add_timeout_count/:infoHash", (req, res) -> {

			try {
				Tools.allowAllHeaders(req, res);
				String infoHash = req.params(":infoHash");


				Tools.dbInit();
				Actions.addToTimeoutCount(infoHash);
				Tools.dbClose();

				log.info("added to timeout count for infohash: " + infoHash);

				return "Added timeout count";
			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {

			}
		});

		// Example:
		// curl localhost:8080/remove_artist_server/c2b37a39-c66a-44b2-b190-a69485ae5d95

		get("/remove_artist_server/:artistMBID", (req, res) -> {

			try {
				Tools.allowOnlyLocalHeaders(req, res);
				String artistMBID = req.params(":artistMBID");

				Tools.dbInit();
				Actions.removeArtist(artistMBID);



				return "Removed Artist MBID: " + artistMBID;
			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}
		});


		get("/remove_song_server/:songMBID", (req, res) -> {

			try {
				Tools.allowOnlyLocalHeaders(req, res);
				String songMBID = req.params(":songMBID");

				Tools.dbInit();
				Actions.removeSong(songMBID);



				return "Removed song MBID: " + songMBID;
			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}
		});





		get("/seeder_upload/:infoHash/:seeders", (req, res) -> {

			try {
				Tools.allowAllHeaders(req, res);
				String infoHash = req.params(":infoHash");
				String seeders = req.params(":seeders");


				log.info("Seeder upload received for infohash: " + infoHash);

				Tools.dbInit();
				Actions.updateSeeders(infoHash, seeders);



				return "Set seeders";
			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}
		});

		get("/download_torrent/:infoHash", (req, res) -> {

			try {
				String infoHash = req.params(":infoHash");

				Tools.dbInit();

				// get torrent file location from infoHash

				String torrentPath = SONG.findFirst("info_hash = ?", infoHash).
						getString("torrent_path");

				log.info("torrent downloaded from : " + torrentPath);
				HttpServletResponse raw = res.raw();
				raw.getOutputStream().write(Files.readAllBytes(Paths.get(torrentPath)));
				raw.getOutputStream().flush();
				raw.getOutputStream().close();

				return res.raw();

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}
		});

		get("/download_torrent_info/:infoHash", (req, res) -> {

			try {
				String infoHash = req.params(":infoHash");
				Tools.dbInit();
				List<SongViewFast> svs = SONG_VIEW_FAST.find("info_hash = ?", infoHash).
						orderBy("secondary_types asc");
				SongViewFast sv = svs.get(0);
				String json = sv.toJson(false);


				// Reannounce the torrent:
				// get the torrent
				//				String torrentFile = sv.getString("torrent_path");
				//				TrackedTorrent tt = TrackedTorrent.load(new File(torrentFile));
				//				tracker.announce(tt);

				log.info("torrent json: " + json);
				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}
		});


		get("/get_songs", (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.set15MinuteCache(req, res);
				Tools.dbInit();
				String json = SONG.findAll().toJson(false);


				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}
		});



		get("/song_search/:query", (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.set15MinuteCache(req, res);
				Tools.dbInit();

				String query = req.params(":query");

				String json = null;

				String queryStr = constructQueryString(query, "search_song");
				log.info(queryStr);

				json = SONG_VIEW_FAST.find(queryStr.toString()).limit(5).
						orderBy("is_primary_album desc, plays desc").toJson(false);

				log.info(json);

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});

		get("/artist_search/:query", (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.set15MinuteCache(req, res);
				Tools.dbInit();

				String query = req.params(":query");

				String json = null;

				String queryStr = constructQueryString(query, "search_artist");
				log.info(queryStr);

				json = ARTIST_SEARCH_VIEW.find(queryStr.toString()).limit(5).toJson(false);

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});

		get("/album_search/:query", (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.set15MinuteCache(req, res);
				Tools.dbInit();

				String query = req.params(":query");

				String json = null;

				String queryStr = constructQueryString(query, "search_album");
				log.info(queryStr);

				json = ALBUM_VIEW_FAST.find(queryStr.toString()).limit(5).
						orderBy("is_primary_album desc, plays desc").toJson(false);

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});

		get("/get_top_albums/:artistMbid", (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.set15MinuteCache(req, res);
				Tools.dbInit();

				String artistMbid = req.params(":artistMbid");

				String json = null;

				String query ="select * from (\n"+
						"\tselect * from album_view_fast \n"+
						"\twhere artist_mbid=\"" + artistMbid + "\" \n"+
						"\tand is_primary_album=0 \n"+
						"\tand number_of_songs > 8\n"+
						"\torder by number_of_songs desc limit 1) as a\n"+
						"union all\n"+
						"select * from (\n"+
						"\tselect * from album_view_fast \n"+
						"\twhere artist_mbid=\"" + artistMbid + "\" \n"+
						"\tand is_primary_album=1 \n"+
						"\torder by plays desc) as b\n"+
						"limit 4;";

				json = ALBUM_VIEW_FAST.findBySQL(query).toJson(false);

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});

		get("/get_top_songs/:artistMbid", (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.set15MinuteCache(req, res);
				Tools.dbInit();

				String artistMbid = req.params(":artistMbid");

				String json = null;
				
				json = SONG_VIEW_FAST.find("artist_mbid = ? and is_primary_album = ?", artistMbid, true).
						orderBy("plays desc").limit(15).toJson(false);						
						
						return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});

		get("/get_all_albums/:artistMbid", (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.set15MinuteCache(req, res);
				Tools.dbInit();

				String artistMbid = req.params(":artistMbid");

				String json = null;

				json = ALBUM_VIEW_FAST.find("artist_mbid = ? AND is_primary_album = ?", 
						artistMbid, true).
						orderBy("year desc").toJson(false);

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});

		get("/get_all_compilations/:artistMbid", (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.set15MinuteCache(req, res);
				Tools.dbInit();

				String artistMbid = req.params(":artistMbid");

				String json = null;

				json = ALBUM_VIEW_FAST.find("artist_mbid = ? AND (primary_type != ? OR secondary_types is not ?)", 
						artistMbid, "Album", null).
						orderBy("number_of_songs desc").toJson(false);

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});

		get("/get_all_songs/:artistMbid", (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.set15MinuteCache(req, res);
				Tools.dbInit();

				String artistMbid = req.params(":artistMbid");

				String json = null;

				json = SONG_VIEW_GROUPED.find("artist_mbid = ?", artistMbid).
						orderBy("plays desc").toJson(false);

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});

		get("/get_artist/:artistMbid", (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.set15MinuteCache(req, res);
				Tools.dbInit();

				String artistMbid = req.params(":artistMbid");

				ObjectNode on = Transformations.artistViewJson(artistMbid);
				String json = Tools.nodeToJson(on);

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});

		get("/get_artist_tags/:artistMbid",  (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.set15MinuteCache(req, res);
				Tools.dbInit();

				String artistMbid = req.params(":artistMbid");

				String json = ARTIST_TAG_VIEW.find("mbid = ?", artistMbid).toJson(false);

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});

		get("/get_related_artists/:artistMbid",  (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.set15MinuteCache(req, res);
				Tools.dbInit();

				String artistMbid = req.params(":artistMbid");

				String json = RELATED_ARTIST_VIEW.findBySQL(
						RELATED_ARTIST_VIEW_SQL, artistMbid, artistMbid).toJson(false);

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});

		get("/get_related_songs/:artistMbid", (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.dbInit();

				String artistMbid = req.params(":artistMbid");



				String json = RELATED_SONG_VIEW.findBySQL(
						RELATED_SONG_VIEW_SQL, 
						artistMbid).toJson(false);

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});

		get("/get_album/:albumMbid", (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.set15MinuteCache(req, res);
				Tools.dbInit();

				String albumMbid = req.params(":albumMbid");

				String json = null;
				json = ALBUM_VIEW_FAST.findFirst("mbid = ?", albumMbid).toJson(false);

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});

		get("/get_album_songs/:albumMbid", (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.set15MinuteCache(req, res);
				Tools.dbInit();

				String albumMbid = req.params(":albumMbid");

				String json = null;
				json = SONG_VIEW_FAST.find("release_group_mbid = ?", albumMbid).
						orderBy("disc_number asc, track_number asc").toJson(false);

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});

		get("/get_song/:songMBID", (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.set15MinuteCache(req, res);
				Tools.dbInit();

				String songMbid = req.params(":songMBID");

				String json = null;
				json = SONG_VIEW_FAST.findFirst("song_mbid = ?", songMbid).toJson(false);

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});

		get("/get_artists", (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.set15MinuteCache(req, res);
				Tools.dbInit();
				//				Tools.setJsonContentType(res);

				String json = null;
				//json = ARTIST.findAll().orderBy("name asc").toJson(false);



				json = ARTIST.findAll()
						.orderBy("case when lower(substr(name,1,3))='the' "
								+ "then substr(name,5,length(name)-3) else name end;")
								.toJson(false, "name", "mbid");

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});

		get("/get_trending_albums", (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.set15MinuteCache(req, res);
				Tools.dbInit();


				String json = null;
				json = ALBUM_VIEW_FAST.find("is_primary_album = ? and plays > ?", true, 0).
						orderBy("created desc").limit(4).toJson(false);

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});


		get("/get_trending_songs", (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.set15MinuteCache(req, res);
				Tools.dbInit();


				String json = null;
				json = SONG_VIEW_GROUPED.find("is_primary_album = ? and plays > ?", true, 0).
						orderBy("created desc").limit(15).toJson(false);

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});

		get("/get_artist_discography_zip/:artistMbid", (req, res) -> {

			File zipFile = null;
			try {

				Tools.allowAllHeaders(req, res);
				Tools.dbInit();

				String artistMbid = req.params(":artistMbid");

				zipFile = Actions.createArtistDiscographyZipFile(artistMbid);

				res.type("application/octet-stream");
				res.header("Content-Disposition", "attachment; filename=\"" + zipFile.getName() + "\"");
				res.header("Content-Length", String.valueOf(zipFile.length()));
				res.header("Content-Transfer-Encoding", "binary");

				return Tools.writeFileToResponse(zipFile, req, res);

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				zipFile.delete();
				Tools.dbClose();

			}


		});

		get("/get_album_zip/:albumMbid", (req, res) -> {

			File zipFile = null;
			try {

				Tools.allowAllHeaders(req, res);
				Tools.dbInit();

				String albumMbid = req.params(":albumMbid");

				zipFile = Actions.createAlbumZipFile(albumMbid);


				res.type("application/octet-stream");
				res.header("Content-Disposition", "attachment; filename=\"" + zipFile.getName() + "\"");
				res.header("Content-Length", String.valueOf(zipFile.length()));
				res.header("Content-Transfer-Encoding", "binary");

				return Tools.writeFileToResponse(zipFile, req, res);


			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				zipFile.delete();
				Tools.dbClose();

			}


		});


		get("/get_magnet_link/:songMbid", (req, res) -> {

			try {

				Tools.allowAllHeaders(req, res);
				Tools.dbInit();

				String songMbid = req.params(":songMbid");

				String magnetLink = Actions.getSongMagnetLink(songMbid);

				return magnetLink;



			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});


	}

	public static String constructQueryString(String query, String columnName) {

		try {
			query = java.net.URLDecoder.decode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String[] splitWords = query.split(" ");
		StringBuilder queryStr = new StringBuilder();

		for(int i = 0;;) {
			String word = splitWords[i++].replaceAll("'", "_");

			String likeQuery = columnName + " like '%" + word + "%'";

			queryStr.append(likeQuery);

			if (i < splitWords.length) {
				queryStr.append(" and ");
			} else {
				break;
			}
		}

		return queryStr.toString();

	}

	


}