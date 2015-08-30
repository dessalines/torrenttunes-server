package com.torrenttunes.server.db;

import static com.torrenttunes.server.db.Tables.*;

import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.torrenttunes.server.db.Tables.Artist;
import com.torrenttunes.server.db.Tables.ArtistTagView;
import com.torrenttunes.server.tools.Tools;


public class Transformations {
	
	
	public static ObjectNode artistViewJson(String artistMbid) {
		
		

		
		Artist artist = ARTIST.findFirst("mbid = ?", artistMbid);
		
		List<ArtistTagView> tags = ARTIST_TAG_VIEW.find("artist.mbid = ?", artistMbid);
		
		List<RelatedArtistView> relatedArtists = RELATED_ARTIST_VIEW.find(
				"mbid = ? and `mbid:1` != ?", 
				artistMbid,artistMbid);
		
		
		
		ObjectNode a = Tools.MAPPER.createObjectNode();

		JsonNode c = Tools.jsonToNode(artist.toJson(false));
		
		ObjectNode on = Tools.MAPPER.valueToTree(c);
		
		a.putAll(on);

		ArrayNode an = a.putArray("related_artists");

		
		for (RelatedArtistView relatedArtist : relatedArtists) {
			an.add(Tools.jsonToNode(relatedArtist.toJson(false)));
		}
		
		ArrayNode ab = a.putArray("tags");
		
		for (ArtistTagView tag : tags) {
			ab.add(Tools.jsonToNode(tag.toJson(false)));
		}


		return a;
		

		
		
	}
	
}
