package com.torrenttunes.server.db;

import static com.torrenttunes.server.db.Tables.*;

import java.io.File;
import java.util.NoSuchElementException;

import org.codehaus.jackson.JsonNode;
import org.javalite.activejdbc.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.musicbrainz.mp3.tagger.Tools.CoverArt;
import com.musicbrainz.mp3.tagger.Tools.Song.MusicBrainzRecordingQuery;
import com.torrenttunes.server.Tools;
import com.torrenttunes.server.db.Tables.Song;
import com.turn.ttorrent.tracker.TrackedTorrent;

public class Actions {

	static final Logger log = LoggerFactory.getLogger(Actions.class);


	public static void saveTorrentToDB(File torrentFile, TrackedTorrent t) {

		try {

			log.info("Saving " + torrentFile.getName() + " to the DB");


			// First get the MBID from the filename
			String mbid = torrentFile.getName().split("_")[0];

			// Save the song
			Song song = SONG.create("torrent_path", torrentFile.getAbsolutePath(),
					"info_hash", t.getHexInfoHash().toLowerCase(),
					"mbid", mbid,
					"seeders", t.seeders());

			Boolean success = song.saveIt();

		} catch(DBException e) {		
			if (e.getMessage().contains("[SQLITE_CONSTRAINT]")) {
				log.error("Not adding " + torrentFile.getName() + ", Song was already in the DB");
			} else {
				e.printStackTrace();
			}
		}

	}


	public static void updateSongInfo(JsonNode jsonNode) {

		
		
		// Get the variables
		String songMbid = jsonNode.get("mbid").asText();
		String title = jsonNode.get("title").asText();
		String artist = jsonNode.get("artist").asText();
		String artistMbid = jsonNode.get("artist_mbid").asText();
		String album = jsonNode.get("album").asText();
		String albumMbid = jsonNode.get("album_mbid").asText();
		Long durationMS = jsonNode.get("duration_ms").asLong();
		Integer trackNumber = jsonNode.get("track_number").asInt();
		String year = jsonNode.get("year").asText();
		
		log.info("Updating song info for song: " + title);

		// First, check to see if the album or artist need to be created:
		Artist artistRow = ARTIST.findFirst("mbid = ?", artistMbid);
		if (artistRow == null) {
			log.info("derp1");
			// Fetch some links and images from musicbrainz
			com.musicbrainz.mp3.tagger.Tools.Artist mbInfo = 
					com.musicbrainz.mp3.tagger.Tools.Artist.fetchArtist(artistMbid);
			// Wait 1.1 seconds
			try {
				Thread.sleep(1200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("derp2");

			String imageURL = null;
			if (mbInfo.getWikipedia() != null) {
				imageURL = Tools.getImageFromWikipedia(mbInfo.getWikipedia());
				log.info("found wikipedia image");
			}

			log.info("derp3");
			
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
			log.info("New artist: " + artist + "created");
		}

		// Do the same for album
		ReleaseGroup releaseRow = RELEASE_GROUP.findFirst("mbid = ?" , albumMbid);
		if (releaseRow == null) {
			log.info("derp4");
			// Wait 1.1 seconds
			try {
				Thread.sleep(1200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("derp5");
			// Fetch some links and images from musicbrainz
			com.musicbrainz.mp3.tagger.Tools.ReleaseGroup mbInfo = 
					com.musicbrainz.mp3.tagger.Tools.ReleaseGroup.fetchReleaseGroup(albumMbid);

			log.info("derp6");
			// Wait 1.1 seconds
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("derp7");
			// Fetch the coverart
			String coverArtURL = null, coverArtLargeThumbnail = null, coverArtSmallThumbnail = null;
			try {
				CoverArt coverArt = CoverArt.fetchCoverArt(albumMbid);
				coverArtURL = coverArt.getImageURL();
				coverArtLargeThumbnail = coverArt.getLargeThumbnailURL();
				coverArtSmallThumbnail = coverArt.getSmallThumbnailURL();
			} catch(NoSuchElementException e) {}
			log.info("derp8");
			releaseRow = RELEASE_GROUP.createIt("mbid", albumMbid,
					"title", album,
					"artist_mbid", artistMbid,
					"year", year,
					"wikipedia_link", mbInfo.getWikipedia(),
					"allmusic_link", mbInfo.getAllMusic(),
					"official_homepage", mbInfo.getOfficialHomepage(),
					"lyrics", mbInfo.getLyrics(),
					"album_coverart_url", coverArtURL,
					"album_coverart_thumbnail_large", coverArtLargeThumbnail,
					"album_coverart_thumbnail_small", coverArtSmallThumbnail);
			log.info("New album: " + album + "created");
		}



		// Find it by the MBID
		Song song = SONG.findFirst("mbid = ?", songMbid);

		song.set("title", title,
				"release_group_mbid", albumMbid,
				"duration_ms", durationMS,
				"track_number", trackNumber);
		song.saveIt();
		log.info("New song: " + title + "created");
		


	}


	public static void addToPlayCount(String infoHash) {
		SONG.update("plays = plays + ?", "info_hash = ?", 1, infoHash);

	}


	public static void updateSeeders(String infoHash, String seeders) {
		Song song = SONG.findFirst("info_hash = ?", infoHash);
		song.set("seeders", seeders);
		song.saveIt();

	}

}
