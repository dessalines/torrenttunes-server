package com.torrenttunes.server.db;

import static com.torrenttunes.server.db.Tables.*;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

import org.codehaus.jackson.JsonNode;
import org.javalite.activejdbc.DBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.musicbrainz.mp3.tagger.Tools.CoverArt;
import com.musicbrainz.mp3.tagger.Tools.Song.MusicBrainzRecordingQuery;
import com.torrenttunes.server.Tools;
import com.torrenttunes.server.db.Tables.ReleaseGroup;
import com.torrenttunes.server.db.Tables.Song;
import com.turn.ttorrent.tracker.TrackedTorrent;

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
			if (e.getMessage().contains("[SQLITE_CONSTRAINT]")) {
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

		log.info("Updating song info for song: " + title + " , mbid: " + songMbid);

		// Find it by the MBID
		Song song = SONG.findFirst("mbid = ?", songMbid);


		song.set("title", title,
				"duration_ms", durationMS).saveIt();
		log.info("New song: " + title + " updated");



		// First, check to see if the album or artist need to be created:
		Artist artistRow = ARTIST.findFirst("mbid = ?", artistMbid);
		if (artistRow == null) {
			log.info("new artist");
			// Fetch some links and images from musicbrainz
			com.musicbrainz.mp3.tagger.Tools.Artist mbInfo = 
					com.musicbrainz.mp3.tagger.Tools.Artist.fetchArtist(artistMbid);

			String imageURL = null;
			if (mbInfo.getWikipedia() != null) {
				imageURL = Tools.getImageFromWikipedia(mbInfo.getWikipedia());
				log.info("found wikipedia image");
			}



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
			log.info("New artist: " + artist + " created");
		}

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
			
			
			


			ReleaseGroup releaseRow = RELEASE_GROUP.findFirst("mbid = ?" , albumMbid);
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
				log.info("New album: " + mbInfo.getTitle() + " created");
			}

			// Now that both the song and release_group are made, add the song_release_group
			// row that links them together
			try {
				SONG_RELEASE_GROUP.createIt("song_mbid", songMbid,
						"release_group_mbid", albumMbid,
						"disc_number", discNo,
						"track_number", trackNo);
				log.info("Song release group:" + songMbid, " created");
			} catch(DBException e) {
				e.printStackTrace();
				log.error("That song release group row already exists");
			}

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
		SONG.update("plays = plays + ?", "info_hash = ?", 1, infoHash);

	}


	public static void updateSeeders(String infoHash, String seeders) {
		Song song = SONG.findFirst("info_hash = ?", infoHash);
		song.set("seeders", seeders);
		song.saveIt();

	}

}
