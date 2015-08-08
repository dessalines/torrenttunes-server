package com.torrenttunes.server.webservice;

import static com.torrenttunes.server.db.Tables.ALBUM_SEARCH_VIEW;
import static com.torrenttunes.server.db.Tables.ALBUM_VIEW;
import static com.torrenttunes.server.db.Tables.ARTIST;
import static com.torrenttunes.server.db.Tables.ARTIST_SEARCH_VIEW;
import static com.torrenttunes.server.db.Tables.SONG;
import static com.torrenttunes.server.db.Tables.SONG_SEARCH_VIEW;
import static com.torrenttunes.server.db.Tables.SONG_VIEW;
import static com.torrenttunes.server.db.Tables.SONG_VIEW_GROUPED;
import static spark.Spark.get;
import static spark.Spark.post;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hashing;
import com.torrenttunes.server.DataSources;
import com.torrenttunes.server.Tools;
import com.torrenttunes.server.db.Actions;
import com.torrenttunes.server.db.Tables.SongView;


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


				Tools.saveTorrentFileToDB(torrentFile);

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


				return "Saved info";
			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}
		});

		post("/add_play_count/:infoHash", (req, res) -> {

			try {
				Tools.allowAllHeaders(req, res);
				String infoHash = req.params(":infoHash");

				Tools.dbInit();
				Actions.addToPlayCount(infoHash);



				return "Added play count";
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
				SongView sv = SONG_VIEW.findFirst("info_hash = ?", infoHash);
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
				Tools.logRequestInfo(req);
				Tools.allowAllHeaders(req, res);
				Tools.dbInit();

				String query = req.params(":query");

				String json = null;

				String queryStr = constructQueryString(query, "search_song");
				log.info(queryStr);

				json = SONG_SEARCH_VIEW.find(queryStr.toString()).limit(5).toJson(false);

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
				Tools.logRequestInfo(req);
				Tools.allowAllHeaders(req, res);
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
				Tools.logRequestInfo(req);
				Tools.allowAllHeaders(req, res);
				Tools.dbInit();

				String query = req.params(":query");

				String json = null;

				String queryStr = constructQueryString(query, "search_album");
				log.info(queryStr);

				json = ALBUM_SEARCH_VIEW.find(queryStr.toString()).limit(5).toJson(false);

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
				Tools.logRequestInfo(req);
				Tools.allowAllHeaders(req, res);
				Tools.dbInit();

				String artistMbid = req.params(":artistMbid");

				String json = null;

				json = ALBUM_VIEW.find("artist_mbid = ? AND is_primary_album = ?", artistMbid, true).
						orderBy("plays desc").limit(4).toJson(false);

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
				Tools.logRequestInfo(req);
				Tools.allowAllHeaders(req, res);
				Tools.dbInit();

				String artistMbid = req.params(":artistMbid");

				String json = null;

				json = SONG_VIEW_GROUPED.find("artist_mbid = ?", artistMbid).
						orderBy("plays desc").limit(25).toJson(false);

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
				Tools.logRequestInfo(req);
				Tools.allowAllHeaders(req, res);
				Tools.dbInit();

				String artistMbid = req.params(":artistMbid");

				String json = null;

				json = ALBUM_VIEW.find("artist_mbid = ? AND is_primary_album = ?", 
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
				Tools.logRequestInfo(req);
				Tools.allowAllHeaders(req, res);
				Tools.dbInit();

				String artistMbid = req.params(":artistMbid");

				String json = null;

				json = ALBUM_VIEW.find("artist_mbid = ? AND (primary_type != ? OR secondary_types is not ?)", 
						artistMbid, "Album", null).
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

		get("/get_all_songs/:artistMbid", (req, res) -> {

			try {
				Tools.logRequestInfo(req);
				Tools.allowAllHeaders(req, res);
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
				Tools.logRequestInfo(req);
				Tools.allowAllHeaders(req, res);
				Tools.dbInit();

				String artistMbid = req.params(":artistMbid");

				String json = null;
				json = ARTIST.findFirst("mbid = ?", artistMbid).toJson(false);

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
				Tools.logRequestInfo(req);
				Tools.allowAllHeaders(req, res);
				Tools.dbInit();

				String albumMbid = req.params(":albumMbid");

				String json = null;
				json = ALBUM_VIEW.findFirst("mbid = ?", albumMbid).toJson(false);

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
				Tools.logRequestInfo(req);
				Tools.allowAllHeaders(req, res);
				Tools.dbInit();

				String albumMbid = req.params(":albumMbid");

				String json = null;
				json = SONG_VIEW.find("release_group_mbid = ?", albumMbid).
						orderBy("track_number asc").toJson(false);

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
				Tools.logRequestInfo(req);
				Tools.allowAllHeaders(req, res);
				Tools.dbInit();


				String json = null;
				json = ARTIST.findAll().orderBy("name asc").toJson(false);

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
				Tools.logRequestInfo(req);
				Tools.allowAllHeaders(req, res);
				Tools.dbInit();


				String json = null;
				json = ALBUM_VIEW.find("is_primary_album = ?", true).orderBy("plays desc").limit(4).toJson(false);

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
				Tools.logRequestInfo(req);
				Tools.allowAllHeaders(req, res);
				Tools.dbInit();


				String json = null;
				json = SONG_VIEW_GROUPED.findAll().orderBy("plays desc").limit(40).toJson(false);

				return json;

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
			} finally {
				Tools.dbClose();
			}


		});

		
		
		get("/get_audio_file/:encodedPath", (req, res) -> {

			//			res.header("Content-Disposition", "filename=\"music.mp3\"");

			HttpServletResponse raw = res.raw();

			try {
				Tools.allowAllHeaders(req, res);

				String path = URLDecoder.decode(req.params(":encodedPath"), "UTF-8");


				File mp3 = new File(path);				


				// write out the request headers:
								for (String h : req.headers()) {
									log.info("Header:" + h + " = " + req.headers(h));
								}

				String range = req.headers("Range");

				Boolean nonStreamingBrowser = req.headers("User-Agent").toLowerCase().contains("firefox");
			

				//				res.status(206);

				OutputStream stream = raw.getOutputStream();
			
				
				if (range == null || nonStreamingBrowser) {
					res.header("Content-Length", String.valueOf(mp3.length())); 
					Files.copy(mp3.toPath(), stream);

					return res.raw();

				}

				int[] fromTo = fromTo(mp3, range);

				//					new FileInputStream(mp3).getChannel().transferTo(raw.getOutputStream().get);

				int length = (int) (fromTo[1] - fromTo[0] + 1);

				res.status(206);
				res.type("audio/mpeg");

				res.header("Accept-Ranges",  "bytes");

				//					res.header("Content-Length", String.valueOf(mp3.length())); 
				res.header("Content-Range", contentRangeByteString(fromTo));
				res.header("Content-Length", String.valueOf(length)); 
//				res.header("Content-Length", String.valueOf(mp3.length())); 
				//					res.header("Content-Disposition", "attachment; filename=\"" + mp3.getName() + "\"");
				res.header("Date", new java.util.Date(mp3.lastModified()).toString());
				res.header("Last-Modified", new java.util.Date(mp3.lastModified()).toString());
//				res.header("Server", "Apache");
				//									res.header("X-Content-Duration", "30");
				//									res.header("Content-Duration", "30");
				res.header("Connection", "Keep-Alive");
				//					String etag = com.google.common.io.Files.hash(mp3, Hashing.md5()).toString();
				//					res.header("Etag", etag);
				//					res.header("Cache-Control", "no-cache, private");
				//					res.header("X-Pad","avoid browser bug");
				//					res.header("Expires", "0");
				//					res.header("Pragma", "no-cache");
				res.header("Content-Transfer-Encoding", "binary");
				res.header("Transfer-Encoding", "chunked");
				res.header("Keep-Alive", "timeout=15, max=100");
				//					res.header("If-None-Match", "webkit-no-cache");
				//					res.header("X-Sendfile", path);
				//					res.header("X-Stream", true);

				// This one works, but doesn't stream



				log.info("writing random access file instead");
				final RandomAccessFile raf = new RandomAccessFile(mp3, "r");
				raf.seek(fromTo[0]);
				writeAudioToOS(length, raf, stream);
			
				raf.close();

				stream.flush();
				stream.close();

				return res.raw();

			} catch (Exception e) {
				res.status(666);
				e.printStackTrace();
				return e.getMessage();
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
			String word = columnName + " like '%" + splitWords[i++] + "%'";
			queryStr.append(word);

			if (i < splitWords.length) {
				queryStr.append(" and ");
			} else {
				break;
			}
		}

		return queryStr.toString();

	}

	public static int[] fromTo(File mp3, String range) {
		int[] ret = new int[3];

		if (range == null || range.equals("bytes=0-")) {
			//			ret[0] = 0;
			//			ret[1] = mp3.length() -1;
			//			ret[2] = mp3.length();
			//			
			//			return ret;

			//			range = "bytes=0-";

		}

		String[] ranges = range.split("=")[1].split("-");
		log.info(range);
		log.info("ranges[] = " + Arrays.toString(ranges));

		Integer chunkSize = 512000;
		Integer from = Integer.parseInt(ranges[0]);
		Integer to = chunkSize + from;
		if (to >= mp3.length()) {
			to = (int) (mp3.length() - 1);
		}
		if (ranges.length == 2) {
			to = Integer.parseInt(ranges[1]);
		}

		ret[0] = from;
		ret[1] = to;
		ret[2] = (int) mp3.length();
		//		ret[2] = (int) (ret[1] - ret[0] + 1);

		return ret;

	}

	public static String contentRangeByteString(int[] fromTo) {

		String responseRange = "bytes " + fromTo[0] + "-" + fromTo[1] + "/" + fromTo[2];

		log.info("response range = " + responseRange);
		return responseRange;

	}

	public static void writeAudioToOS(Integer length, RandomAccessFile raf, OutputStream os) throws IOException {

		byte[] buf = new byte[32*1024];
		while(length != 0) {
			int read = raf.read(buf, 0, buf.length > length ? length : buf.length);
			os.write(buf, 0, read);
			length -= read;
		}
		log.info("before closing");
//		
		

		
		

	}


}
