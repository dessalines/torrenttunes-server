package com.torrenttunes.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;
import spark.Response;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.torrenttunes.server.db.Actions;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;

import de.javakaffee.kryoserializers.KryoReflectionFactorySupport;

public class Tools {

	static final Logger log = LoggerFactory.getLogger(Tools.class);

	public static final Kryo KRYO  = new KryoReflectionFactorySupport();

	public static final Gson GSON = new Gson();
	public static final Gson GSON2 = new GsonBuilder().setPrettyPrinting().create();
	
	public static final ObjectMapper MAPPER = new ObjectMapper();
	
	
	public static void allowOnlyLocalHeaders(Request req, Response res) {


		log.debug("req ip = " + req.ip());


		//		res.header("Access-Control-Allow-Origin", "http://mozilla.com");
		//		res.header("Access-Control-Allow-Origin", "null");
		//		res.header("Access-Control-Allow-Origin", "*");
		//		res.header("Access-Control-Allow-Credentials", "true");


		if (!isLocalIP(req.ip())) {
			throw new NoSuchElementException("Not a local ip, can't access");
		}
	}

	public static Boolean isLocalIP(String ip) {
		Boolean isLocalIP = (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1"));
		return isLocalIP;
	}

	public static void allowAllHeaders(Request req, Response res) {
		String origin = req.headers("Origin");
		res.header("Access-Control-Allow-Credentials", "true");
		res.header("Access-Control-Allow-Origin", origin);


	}



	public static void logRequestInfo(Request req) {
		String origin = req.headers("Origin");
		String origin2 = req.headers("origin");
		String host = req.headers("Host");


		log.debug("request host: " + host);
		log.debug("request origin: " + origin);
		log.debug("request origin2: " + origin2);


		//		System.out.println("origin = " + origin);
		//		if (DataSources.ALLOW_ACCESS_ADDRESSES.contains(req.headers("Origin"))) {
		//			res.header("Access-Control-Allow-Origin", origin);
		//		}
		for (String header : req.headers()) {
			log.debug("request header | " + header + " : " + req.headers(header));
		}
		log.debug("request ip = " + req.ip());
		log.debug("request pathInfo = " + req.pathInfo());
		log.debug("request host = " + req.host());
		log.debug("request url = " + req.url());
	}

	public static final Map<String, String> createMapFromAjaxPost(String reqBody) {
		log.debug(reqBody);
		Map<String, String> postMap = new HashMap<String, String>();
		String[] split = reqBody.split("&");
		for (int i = 0; i < split.length; i++) {
			String[] keyValue = split[i].split("=");
			try {
				if (keyValue.length > 1) {
					postMap.put(URLDecoder.decode(keyValue[0], "UTF-8"),URLDecoder.decode(keyValue[1], "UTF-8"));
				}
			} catch (UnsupportedEncodingException |ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				throw new NoSuchElementException(e.getMessage());
			}
		}

		log.debug(GSON2.toJson(postMap));

		return postMap;

	}
	
	
	public static FilenameFilter TORRENT_FILE_FILTER = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.endsWith(".torrent");
		}
	};

	public static void setupDirectories() {
		if (!new File(DataSources.HOME_DIR()).exists()) {
			log.info("Setting up ~/." + DataSources.APP_NAME + " dirs");
			new File(DataSources.HOME_DIR()).mkdirs();
			new File(DataSources.TORRENTS_DIR()).mkdirs();
			com.torrenttunes.client.tools.DataSources.APP_NAME = "torrenttunes-server";
			com.torrenttunes.client.tools.DataSources.MUSIC_STORAGE_PATH = 
					DataSources.HOME_DIR() + "/music";
			new File(com.torrenttunes.client.tools.DataSources.MUSIC_STORAGE_PATH).mkdirs();
			new File(com.torrenttunes.client.tools.DataSources.TORRENTS_DIR()).mkdirs();
			
		} else {
			log.info("Home directory already exists");
		}
	}

	@Deprecated
	public static String convertTorrentToMagnetLink(Torrent t) {


		System.out.println();
		//magnet:?xt=urn:btih:09c17295ccc24af400a2a91495af440b27766b5e&dn=Fugazi+-+Studio+Discography+1989-2001+%5BFLAC%5D

		StringBuilder s = new StringBuilder();
		s.append("magnet:?xt=urn:btih:" + t.getHexInfoHash().toLowerCase());
		s.append("&dn=");
		s.append(encodeURL(t.getName()));
		return s.toString();
	}

	public static void torrentInfo(Torrent t) {
		log.info("Name: " + t.getName());
		log.info("Created by: " + t.getCreatedBy());
		log.info("Hex info hash: " + t.getHexInfoHash());
		log.info("Announce List: " + t.getAnnounceList());
		log.info("Filenames: " + t.getFilenames());


	}

	public static String encodeURL(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] serializeTorrentFile(Torrent t) {

		byte[] out = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			t.save(baos);
			
			out = baos.toByteArray();
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;
	}



	public static Torrent deserializeTorrentFile(byte[] data) {
		Torrent t = null;
		try {
			t = new Torrent(data, false);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return t;
	}

	public static String serializeTorrentFile2(Torrent t) {

		String out = null;
		try {
			Output output = new Output(new ByteArrayOutputStream());

			KRYO.writeObject(output, t);
			ByteArrayOutputStream baos = (ByteArrayOutputStream) output.getOutputStream();

			out = baos.toString("UTF-8");

			output.close();
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;
	}



	public static Torrent deserializeTorrentFile2(String data) {
		Torrent t = null;
		try {

			byte[] b = data.getBytes("UTF-8");

			ByteArrayInputStream bais = new ByteArrayInputStream(b);

			Input input = new Input(bais);

			t = KRYO.readObject(input, Torrent.class);			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return t;
	}

	public static File saveTorrentToFile(Torrent t) {
		File file = new File(DataSources.TORRENTS_DIR() + "/" + t.getName());
		try {
			FileOutputStream fos = new FileOutputStream(file);
			t.save(fos);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return file;

	}

	public static void copyResourcesToHomeDir(Boolean copyAnyway) {


		String zipFile = null;

		if (copyAnyway || !new File(DataSources.SOURCE_CODE_HOME()).exists()) {
			log.info("Copying resources to  ~/." + DataSources.APP_NAME + " dirs");

			try {
				if (new File(DataSources.SHADED_JAR_FILE).exists()) {
					java.nio.file.Files.copy(Paths.get(DataSources.SHADED_JAR_FILE), Paths.get(DataSources.ZIP_FILE()), 
							StandardCopyOption.REPLACE_EXISTING);
					zipFile = DataSources.SHADED_JAR_FILE;

				} else if (new File(DataSources.SHADED_JAR_FILE_2).exists()) {
					java.nio.file.Files.copy(Paths.get(DataSources.SHADED_JAR_FILE_2), Paths.get(DataSources.ZIP_FILE()),
							StandardCopyOption.REPLACE_EXISTING);
					zipFile = DataSources.SHADED_JAR_FILE_2;
				} else {
					log.info("you need to build the project first");
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
			Tools.unzip(new File(zipFile), new File(DataSources.SOURCE_CODE_HOME()));
			//		new Tools().copyJarResourcesRecursively("src", configHome);
		} else {
			log.info("The source directory already exists");
		}
	}

	public static void unzip(File zipfile, File directory) {
		try {
			ZipFile zfile = new ZipFile(zipfile);
			Enumeration<? extends ZipEntry> entries = zfile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				File file = new File(directory, entry.getName());
				if (entry.isDirectory()) {
					file.mkdirs();
				} else {
					file.getParentFile().mkdirs();
					InputStream in = zfile.getInputStream(entry);
					try {
						copy(in, file);
					} finally {
						in.close();
					}
				}
			}

			zfile.close();


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void copy(InputStream in, File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		try {
			copy(in, out);
		} finally {
			out.close();
		}
	}

	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		while (true) {
			int readCount = in.read(buffer);
			if (readCount < 0) {
				break;
			}
			out.write(buffer, 0, readCount);
		}
	}

	private static void copy(File file, OutputStream out) throws IOException {
		InputStream in = new FileInputStream(file);
		try {
			copy(in, out);
		} finally {
			in.close();
		}
	}

	public static void runSQLFile(Connection c,File sqlFile) {

		try {
			Statement stmt = null;
			stmt = c.createStatement();
			String sql;

			sql = Files.toString(sqlFile, Charset.defaultCharset());

			stmt.executeUpdate(sql);
			stmt.close();
		} catch (IOException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static final void dbInit() {
		try {
			new DB("default").open("org.sqlite.JDBC", "jdbc:sqlite:" + DataSources.DB_FILE(), "root", "p@ssw0rd");
		} catch (DBException e) {
			e.printStackTrace();
			dbClose();
			dbInit();
		}

	}

	public static final void dbClose() {
		new DB("default").close();
	}

	public static String extractInfoHashFromMagnetLink(String magnetLink) {
		// magnet:?xt=urn:btih:09c17295ccc24af400a2a91495af440b27766b5e&dn=Fugazi+-+Studio+Discography+1989-2001+%5BFLAC%5D

		return magnetLink.split("btih:")[1].split("&dn")[0].toLowerCase();

	}

	public static String extractNameFromMagnetLink(String magnetLink) {
		String encoded = magnetLink.split("&dn=")[1];
		String name = null;
		try {
			name = URLDecoder.decode(encoded, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return name;
	}

	public static void announceAndSaveTorrentFileToDB(Tracker tracker, File f) {
		try {
			log.info("Announcing file: " + f.getName());
			TrackedTorrent tt = TrackedTorrent.load(f);
			
			
			tracker.announce(tt);

			// Save to the DB
			Tools.dbInit();
			Actions.saveTorrentToDB(f, tt);
			Tools.dbClose();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static JsonNode jsonToNode(String json) {

		try {
			JsonNode root = MAPPER.readTree(json);
			return root;
		} catch (Exception e) {
			log.error("json: " + json);
			e.printStackTrace();
		}
		return null;
	}

	public static String nodeToJson(ObjectNode a) {
		try {
			return Tools.MAPPER.writeValueAsString(a);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String nodeToJsonPretty(JsonNode a) {
		try {
			return Tools.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(a);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void uninstall() {

		try {
			FileUtils.deleteDirectory(new File(DataSources.HOME_DIR()));
			log.info("Torrenttunes-tracker uninstalled successfully.");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String convertWikimediaCommonsURLToImageUrl(String wmLink) {	
		
		String fileName = wmLink.split("File:")[1];
		
		String md5 = Hashing.md5().hashString(fileName, Charsets.UTF_8).toString();
		
		String weirdPathString = md5.substring(0, 1) + "/" + md5.substring(0, 2) + "/";
		String imageURL = "https://upload.wikimedia.org/wikipedia/commons/" + weirdPathString + 
				fileName;
		
		return imageURL;
	}

	public static String readFile(String path) {
		String s = null;

		byte[] encoded;
		try {
			encoded = java.nio.file.Files.readAllBytes(Paths.get(path));

			s = new String(encoded, Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s;
	}
	
	public static void addExternalWebServiceVarToTools() {

		log.info("tools.js = " + DataSources.TOOLS_JS());
		try {
			List<String> lines = java.nio.file.Files.readAllLines(Paths.get(DataSources.TOOLS_JS()));

			String interalServiceLine = "var localSparkService = '" + 
					DataSources.WEB_SERVICE_URL + "';";

			String externalServiceLine = "var externalSparkService ='" + 
					DataSources.TRACKER_URL + "';";

			lines.set(0, interalServiceLine);
			lines.set(1, externalServiceLine);

			java.nio.file.Files.write(Paths.get(DataSources.TOOLS_JS()), lines);
			Files.touch(new File(DataSources.TOOLS_JS()));


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
