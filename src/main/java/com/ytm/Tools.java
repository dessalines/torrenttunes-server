package com.ytm;

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
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.io.Files;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.common.Torrent.TorrentFile;

import de.javakaffee.kryoserializers.KryoReflectionFactorySupport;

public class Tools {

	static final Logger log = LoggerFactory.getLogger(Tools.class);

	public static final Kryo KRYO  = new KryoReflectionFactorySupport();
	
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
		} else {
			log.info("Home directory already exists");
		}
	}

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

	public static String serializeTorrentFile(Torrent t) {

		String out = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			t.save(baos);

			out = baos.toString();
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;
	}
	
	

	public static Torrent deserializeTorrentFile(String data) {
		Torrent t = null;
		try {
			byte[] b = data.getBytes();
			t = new Torrent(b, false);
			
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

}
