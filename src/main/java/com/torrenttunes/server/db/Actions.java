package com.torrenttunes.server.db;

import static com.torrenttunes.client.db.Tables.SETTINGS;
import static com.torrenttunes.server.db.Tables.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.codehaus.jackson.JsonNode;
import org.javalite.activejdbc.DBException;
import org.javalite.activejdbc.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.musicbrainz.mp3.tagger.Tools.Artist.Tag;
import com.musicbrainz.mp3.tagger.Tools.CoverArt;
import com.musicbrainz.mp3.tagger.Tools.Song.MusicBrainzRecordingQuery;
import com.torrenttunes.client.LibtorrentEngine;
import com.torrenttunes.server.DataSources;
import com.torrenttunes.server.db.Tables.ReleaseGroup;
import com.torrenttunes.server.db.Tables.Song;
import com.torrenttunes.server.tools.Tools;

public class Actions {

	static final Logger log = LoggerFactory.getLogger(Actions.class);



	public static void saveTorrentToDB(File torrentFile, String infoHash) {

		try {

			log.info("Saving " + torrentFile.getName() + " to the DB");


			// First get the MBID from the filename
			String mbid = torrentFile.getName().split("_sha2")[0].split("mbid-")[1];

			// Save the song
			Song song = SONG.create("torrent_path", torrentFile.getAbsolutePath(),
					"info_hash", infoHash,
					"mbid", mbid);

			Boolean success = song.saveIt();

		} catch(DBException e) {	
			if (e.getMessage().contains("MySQLIntegrityConstraintViolationException")) {
				log.error("Not adding " + torrentFile.getName() + ", Song was already in the DB");
			} else {
				e.printStackTrace();
			}
		} catch(ArrayIndexOutOfBoundsException e2) {
			log.error("Filename was too long");
		}

	}


	public static void updateSongInfo(JsonNode json) {


		// Get the variables
		String songMbid = json.get("recordingMBID").asText();
		String title = json.get("recording").asText();
		String artist = json.get("artist").asText();
		String artistMbid = json.get("artistMBID").asText();
		Long durationMS = json.get("duration").asLong();
		String ipHash = json.get("uploader_ip_hash").asText();

		log.info("Updating song info for song: " + title + " , mbid: " + songMbid);

		// Find it by the MBID
		Tools.dbInit();
		Song song = SONG.findFirst("mbid = ?", songMbid);
		song.set("title", title,
				"duration_ms", durationMS,
				"uploader_ip_hash", ipHash).saveIt();
		Tools.dbClose();
		log.info("New song: " + title + " updated");

		createArtist(artist, artistMbid);

		createSongReleaseGroups(json, songMbid, artistMbid);


	}


	private static void createSongReleaseGroups(JsonNode json, String songMbid,
			String artistMbid) {
		// Loop over every album found, necessary for release_groups and tracks
		int i = 0;
		JsonNode releaseGroupInfos = json.get("releaseGroupInfos");
		while (releaseGroupInfos.has(i)) {
			JsonNode cReleaseGroupInfo = releaseGroupInfos.get(i++);

			String albumMbid = cReleaseGroupInfo.get("mbid").asText();
			Integer discNo = cReleaseGroupInfo.get("discNo").asInt();
			Integer trackNo = cReleaseGroupInfo.get("trackNo").asInt();
			String primaryType = cReleaseGroupInfo.get("primaryType").asText();
			JsonNode secondaryTypesJson = cReleaseGroupInfo.get("secondaryTypes");


			String secondaryTypes = null;
			if (secondaryTypesJson.isArray()) {
				secondaryTypes = "";
				int j = 0;
				while (secondaryTypesJson.has(j)) {
					secondaryTypes += secondaryTypesJson.get(j++).asText();
				}
			}
			log.info("secondary types = " + secondaryTypes);


			createReleaseGroups(artistMbid, albumMbid, primaryType,
					secondaryTypes);

			// Now that both the song and release_group are made, add the song_release_group
			// row that links them together
			try {
				Tools.dbInit();
				SONG_RELEASE_GROUP.createIt("song_mbid", songMbid,
						"release_group_mbid", albumMbid,
						"disc_number", discNo,
						"track_number", trackNo);

				log.info("Song release group:" + songMbid, " created");
			} catch(DBException e) {
				log.error("That song release group row already exists");
			} finally {
				Tools.dbClose();
			}

		}
	}


	private static void createReleaseGroups(String artistMbid,
			String albumMbid, String primaryType, String secondaryTypes) {
		Tools.dbInit();
		ReleaseGroup releaseRow = RELEASE_GROUP.findFirst("mbid = ?" , albumMbid);
		Tools.dbClose();

		// If the album doesn't exist, create the row
		if (releaseRow == null) {
			log.info("new album");
			// Fetch some links and images from musicbrainz
			com.musicbrainz.mp3.tagger.Tools.ReleaseGroup mbInfo = 
					com.musicbrainz.mp3.tagger.Tools.ReleaseGroup.fetchReleaseGroup(albumMbid);

			// Fetch the coverart
			String coverArtURL = null, coverArtLargeThumbnail = null, coverArtSmallThumbnail = null;
			try {
				CoverArt coverArt = CoverArt.fetchCoverArt(albumMbid);
				coverArtURL = coverArt.getImageURL();
				coverArtLargeThumbnail = coverArt.getLargeThumbnailURL();
				coverArtSmallThumbnail = coverArt.getSmallThumbnailURL();
			} catch(NoSuchElementException e) {
				e.printStackTrace();
			}
			Tools.dbInit();
			releaseRow = RELEASE_GROUP.createIt("mbid", albumMbid,
					"title", mbInfo.getTitle(),
					"artist_mbid", artistMbid,
					"year", mbInfo.getYear(),
					"wikipedia_link", mbInfo.getWikipedia(),
					"allmusic_link", mbInfo.getAllMusic(),
					"official_homepage", mbInfo.getOfficialHomepage(),
					"lyrics", mbInfo.getLyrics(),
					"album_coverart_url", coverArtURL,
					"album_coverart_thumbnail_large", coverArtLargeThumbnail,
					"album_coverart_thumbnail_small", coverArtSmallThumbnail,
					"primary_type", primaryType,
					"secondary_types", secondaryTypes);
			Tools.dbClose();
			log.info("New album: " + mbInfo.getTitle() + " created");
		}
	}


	private static void createArtist(String artist, String artistMbid) {
		// First, check to see if the album or artist need to be created:
		Tools.dbInit();
		Artist artistRow = ARTIST.findFirst("mbid = ?", artistMbid);
		Tools.dbClose();

		if (artistRow == null) {
			log.info("new artist");
			// Fetch some links and images from musicbrainz
			com.musicbrainz.mp3.tagger.Tools.Artist mbInfo = 
					com.musicbrainz.mp3.tagger.Tools.Artist.fetchArtist(artistMbid);


			// Fetch the images
			String imageURL = null;
			if (mbInfo.getWikipedia() != null) {
				try {
					imageURL = Tools.getImageFromWikipedia(mbInfo.getWikipedia());
					log.info("found wikipedia image");
				} catch(NullPointerException e) {
					e.printStackTrace();
				}
			}

			// Fetch and create the tags
			// Check to see if there are any tagInfos for that artist in the db, or any from musicBrainz
			Tools.dbInit();


			artistRow = ARTIST.createIt("mbid", artistMbid,
					"name", artist,
					"image_url", imageURL,
					"wikipedia_link", mbInfo.getWikipedia(),
					"allmusic_link", mbInfo.getAllMusic(),
					"official_homepage", mbInfo.getOfficialHomepage(),
					"imdb", mbInfo.getIMDB(),
					"lyrics", mbInfo.getLyrics(),
					"youtube", mbInfo.getYoutube(),
					"soundcloud", mbInfo.getSoundCloud(),
					"lastfm", mbInfo.getLastFM());

			createTags(artistMbid, mbInfo);

			Tools.dbClose();

			log.info("New artist: " + artist + " created");
		}
	}


	private static void createTags(String artistMbid,
			com.musicbrainz.mp3.tagger.Tools.Artist mbInfo) {

		TagInfo ti = TAG_INFO.findFirst("artist_mbid = ?", artistMbid);

		// Delete all of that artists tag infos if they do exist
		if (ti != null) {
			TAG_INFO.delete("artist_mbid = ?", artistMbid);
		}

		if (mbInfo.getTags() != null) {
			for (Tag mbInfoTag : mbInfo.getTags()) {
				com.torrenttunes.server.db.Tables.Tag tagRow = TAG.findFirst("name = ?", mbInfoTag.getName());

				// if that tag doesn't exist, create it
				if (tagRow == null) {
					tagRow = TAG.createIt("name", mbInfoTag.getName());
				}

				// create the tag_info
				ti = TAG_INFO.createIt("artist_mbid", artistMbid,
						"count", mbInfoTag.getCount(),
						"tag_id", tagRow.getInteger("id"));

				log.info("Added tag " + mbInfoTag.getName() + " for artist " + artistMbid);


			}
		}
	}

	public static void refetchTags() {
		List<Artist> artists = ARTIST.findAll();


		for (Artist cArtist : artists) {
			String artistMbid = cArtist.getString("mbid");

			// Fetch some links and images from musicbrainz
			com.musicbrainz.mp3.tagger.Tools.Artist mbInfo = 
					com.musicbrainz.mp3.tagger.Tools.Artist.fetchArtist(artistMbid);

			createTags(artistMbid, mbInfo);

		}
	}

	public static void refetchAlbumArt() {


		List<ReleaseGroup> albums = RELEASE_GROUP.findAll();

		for (ReleaseGroup cAlbum : albums) {
			String albumMbid = cAlbum.getString("mbid");
			String albumTitle = cAlbum.getString("title");

			if (cAlbum.getString("album_coverart_url") == null) {
				log.info("no cover art found, refetching for album: " + albumTitle + " , mbid: " + albumMbid);

				// Fetch the coverart
				String coverArtURL = null, coverArtLargeThumbnail = null, coverArtSmallThumbnail = null;
				try {
					CoverArt coverArt = CoverArt.fetchCoverArt(albumMbid);
					coverArtURL = coverArt.getImageURL();
					coverArtLargeThumbnail = coverArt.getLargeThumbnailURL();
					coverArtSmallThumbnail = coverArt.getSmallThumbnailURL();
				} catch(NoSuchElementException e) {
					e.printStackTrace();
					continue;
				}

				RELEASE_GROUP.update(
						// Updates
						"album_coverart_url = ?, album_coverart_thumbnail_large = ?, album_coverart_thumbnail_small = ?", 
						// Conditions
						"mbid = ?", 
						coverArtURL,
						coverArtLargeThumbnail,
						coverArtSmallThumbnail,
						albumMbid);


			}


		}




	}


	public static void addToPlayCount(String infoHash) {
		//		SONG.update("plays = plays + ?", "info_hash = ?", 1, infoHash);
		Song s = SONG.findFirst("info_hash = ?", infoHash);
		s.set("plays", s.getInteger("plays") + 1);
		s.saveIt();

	}

	public static void addToTimeoutCount(String infoHash) {
		//		SONG.update("plays = plays + ?", "info_hash = ?", 1, infoHash);
		Song s = SONG.findFirst("info_hash = ?", infoHash);
		s.set("timeouts", s.getInteger("timeouts") + 1);
		s.saveIt();

	}


	public static void updateSeeders(String infoHash, String seeders) {
		Song song = SONG.findFirst("info_hash = ?", infoHash);
		song.set("seeders", seeders);
		song.saveIt();

	}

	public static void removeArtist(String artistMBID) {

		// First delete/remove it from the libtorrent session


		// First find everything that needs to be deleted, in the DB
		//		Set<String> songMbids = new HashSet<String>();
		//		List<SongView> svs = SONG_VIEW.find("artist_mbid = ?", artistMBID);
		//		
		//		for (SongView sv : svs) {
		//			songMbids.add(sv.getString("song_mbid"));
		//		}
		//		SONG_VIEW.dele


		// get release groups
		List<ReleaseGroup> rgs = RELEASE_GROUP.find("artist_mbid = ?", artistMBID);


		// Get songs
		Set<SongReleaseGroup> srgs = new HashSet<>();
		for (ReleaseGroup rg : rgs) {
			List<SongReleaseGroup> srg = SONG_RELEASE_GROUP.find("release_group_mbid = ?", 
					rg.getString("mbid"));
			srgs.addAll(srg);
		}
		
		for (ReleaseGroup rg : rgs) {
			SONG_RELEASE_GROUP.delete("release_group_mbid = ?", 
					rg.getString("mbid"));
		}


		// Delete from songs to artist

		for (SongReleaseGroup srg : srgs) {
			log.info(srg.toString());
			Song song = SONG.findFirst("mbid = ?", srg.getString("song_mbid"));

			if (song != null) {
				log.info(song.toString());
				log.info("path name: " + song.getString("torrent_path"));

				// Delete the torrent file from the server:
				File torrentFile = new File(song.getString("torrent_path"));
				if (torrentFile.exists()) torrentFile.delete();

				SONG.delete("mbid = ?", srg.getString("song_mbid"));
			}

		}


		RELEASE_GROUP.delete("artist_mbid = ?", artistMBID);
		
		TAG_INFO.delete("artist_mbid = ?", artistMBID);
		ARTIST.delete("mbid = ?", artistMBID);

		com.torrenttunes.client.tools.Tools.dbInit();
		com.torrenttunes.client.db.Actions.removeArtist(artistMBID);
		com.torrenttunes.client.tools.Tools.dbClose();
	}

	public static void removeSong(String songMBID) {
		
		// Remove the song release groups
		SONG_RELEASE_GROUP.delete("song_mbid = ?", songMBID);
		
		Song song = SONG.findFirst("mbid = ?", songMBID);

		// Delete the torrent file from the server:
		File torrentFile = new File(song.getString("torrent_path"));
		if (torrentFile.exists()) torrentFile.delete();

		SONG.delete("mbid = ?", songMBID);

		com.torrenttunes.client.tools.Tools.dbInit();
		com.torrenttunes.client.db.Actions.removeSong(songMBID);
		com.torrenttunes.client.tools.Tools.dbClose();
	}

	public static void saveTorrentFileToDB(File f) {
		try {
			//			infoHash = Torrent.load(f).getHexInfoHash().toLowerCase();

			byte[] fileBytes = java.nio.file.Files.readAllBytes(Paths.get(f.getAbsolutePath()));
			TorrentInfo ti = TorrentInfo.bdecode(fileBytes);

			String infoHash = ti.getInfoHash().toHex().toLowerCase();


			Tools.dbInit();
			Actions.saveTorrentToDB(f, infoHash);
			Tools.dbClose();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static File createArtistDiscographyZipFile(String artistMbid) {

		// Get the artist songs
	
			
			List<SongViewGrouped> songs = SONG_VIEW_GROUPED.find("artist_mbid = ?", artistMbid).
					orderBy("torrent_path");
	
			String zipFileName = songs.get(0).getString("artist") + "_torrents_discography.ZIP";
			
			log.info("Wrote a discography file for artist_mbid: " + artistMbid);
			
			return createZipOfSongs(songs, zipFileName);
		

	}
	
	public static File createAlbumZipFile(String albumMbid) {

		// Get the artist songs
	
			
			List<SongView> songs = SONG_VIEW.find("release_group_mbid = ?", albumMbid).
					orderBy("torrent_path");
	
			String zipFileName = songs.get(0).getString("album") + "_torrents.ZIP";
			
			log.info("Wrote a Zip file for album mbid: " + albumMbid);
			
			return createZipOfSongs(songs, zipFileName);
		

	}
	
	public static <T extends Model> File createZipOfSongs(List<T> songs, String zipFileName) {
		try {

			File zipFile = new File(DataSources.TORRENTS_DIR() + "/" + zipFileName);

			ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));

			for (Model song : songs) {

				try {
				File torrentFile = new File(song.getString("torrent_path"));
				ZipEntry e = new ZipEntry(torrentFile.getName());
				
				zout.putNextEntry(e);
				
				byte[] torrentBytes = java.nio.file.Files.readAllBytes(Paths.get(torrentFile.getAbsolutePath()));

				zout.write(torrentBytes, 0, torrentBytes.length);
				
				zout.closeEntry();
				} catch(NoSuchFileException e) {
					log.error("Couldn't find torrent file, skipping: " + song.getString("torrent_path"));
				}
				
			}
			
			zout.close();
			
			
			
			return zipFile;
			
			

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return null;
	}
	
	public static String getSongMagnetLink(String songMbid) {
		
		Song song = SONG.findFirst("mbid = ?", songMbid);
		
		String infoHash = song.getString("info_hash");
		
		TorrentHandle th = LibtorrentEngine.INSTANCE.getInfoHashToTorrentMap().get(infoHash);
		
		return th.makeMagnetUri();
	}




}
