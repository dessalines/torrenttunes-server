package com.torrenttunes.tracker.db;

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
	
	@Table("release")
	public static class Release extends Model {}
	public static final Release RELEASE = new Release();
	
	
	@Table("search_view")
	public static class SearchView extends Model {}
	public static final SearchView SEARCH_VIEW = new SearchView();
	
}
