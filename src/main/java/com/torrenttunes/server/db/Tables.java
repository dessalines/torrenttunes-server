package com.torrenttunes.server.db;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

public class Tables {
	
	
	
	@Table("song")
	public static class Song extends Model {}
	public static final Song SONG = new Song();
	
	@Table("song_view")
	public static class SongView extends Model {}
	public static final SongView SONG_VIEW = new SongView();
	
	@Table("artist")
	public static class Artist extends Model {}
	public static final Artist ARTIST = new Artist();
	
	@Table("release_group")
	public static class ReleaseGroup extends Model {}
	public static final ReleaseGroup RELEASE_GROUP = new ReleaseGroup();
	
	@Table("song_release_group")
	public static class SongReleaseGroup extends Model {}
	public static final SongReleaseGroup SONG_RELEASE_GROUP = new SongReleaseGroup();
	
	@Table("album_view")
	public static class AlbumView extends Model {}
	public static final AlbumView ALBUM_VIEW = new AlbumView();
	
	
	@Table("song_search_view")
	public static class SongSearchView extends Model {}
	public static final SongSearchView SONG_SEARCH_VIEW = new SongSearchView();
	
	@Table("album_search_view")
	public static class AlbumSearchView extends Model {}
	public static final AlbumSearchView ALBUM_SEARCH_VIEW = new AlbumSearchView();
	
	@Table("artist_search_view")
	public static class ArtistSearchView extends Model {}
	public static final ArtistSearchView ARTIST_SEARCH_VIEW = new ArtistSearchView();
	
}
