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
	
	@Table("song_view_grouped")
	public static class SongViewGrouped extends Model {}
	public static final SongViewGrouped SONG_VIEW_GROUPED = new SongViewGrouped();
	
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
	
	@Table("tag_info")
	public static class TagInfo extends Model {}
	public static final TagInfo TAG_INFO = new TagInfo();
	
	@Table("tag")
	public static class Tag extends Model {}
	public static final Tag TAG = new Tag();
	
//	@Table("related_artist_view")
//	public static class RelatedArtistView extends Model {}
//	public static final RelatedArtistView RELATED_ARTIST_VIEW = new RelatedArtistView();
	
	public static final String RELATED_ARTIST_VIEW_SQL =
			"select artist1.mbid, \n"+
				"artist1.name, \n"+
				"artist2.mbid, \n"+
				"artist2.name, \n"+
				"tag_info1.count, \n"+
				"tag_info2.count, \n"+
				"tag.name, \n"+
				"tag.id,\n"+
				"(tag_info1.tag_id*100/732) as score\n"+
				"from artist as artist1\n"+
				"left join tag_info as tag_info1\n"+
				"on artist1.mbid = tag_info1.artist_mbid\n"+
				"left join tag \n"+
				"on tag_info1.tag_id = tag.id\n"+
				"left join tag_info as tag_info2\n"+
				"on tag_info2.tag_id = tag.id\n"+
				"left join artist as artist2\n"+
				"on tag_info2.artist_mbid = artist2.mbid\n"+
				"where artist1.mbid = \'?\'\n"+
				"and artist2.mbid != \'?\'\n"+
				"group by artist2.mbid\n"+
				"order by \n"+
				"-- This one sorts by tag.id desc, meaning the weirdest categories\n"+
				"tag_info1.tag_id desc,\n"+
				"-- This one makes it more pertinent(NIN has the most votes for industrial)\n"+
				"tag_info1.count desc, \n"+
				"-- This one does the second groups votes\n"+
				"tag_info2.count desc\n"+
				"limit 10;";
	
	
	@Table("artist_tag_view")
	public static class ArtistTagView extends Model {}
	public static final ArtistTagView ARTIST_TAG_VIEW = new ArtistTagView();
	
	
}
