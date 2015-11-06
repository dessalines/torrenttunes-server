package com.torrenttunes.server.db;

import static com.torrenttunes.server.db.Tables.*;

import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.javalite.activejdbc.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.torrenttunes.server.db.Tables.Artist;
import com.torrenttunes.server.db.Tables.ArtistTagView;
import com.torrenttunes.server.tools.Tools;


public class Transformations {
	
	static final Logger log = LoggerFactory.getLogger(Transformations.class);

	

	public static ObjectNode artistViewJson(String artistMbid) {
		
		

		
		Artist artist = ARTIST.findFirst("mbid = ?", artistMbid);
		
		List<ArtistTagView> tags = ARTIST_TAG_VIEW.find("mbid = ?", artistMbid);
		
		
		List<Model> relatedArtists = RELATED_ARTIST_VIEW.findBySQL(RELATED_ARTIST_VIEW_SQL, artistMbid, artistMbid);
//		RELATED_ARTIST_VIEW.find
//		log.info(RELATED_ARTIST_VIEW.find(
//				"mbid like ? and `mbid:1` not like ?", 
//				artistMbid,artistMbid).toSql());
		
		
		
		ObjectNode a = Tools.MAPPER.createObjectNode();

		JsonNode c = Tools.jsonToNode(artist.toJson(false));
		
		ObjectNode on = Tools.MAPPER.valueToTree(c);
		
		a.putAll(on);

		ArrayNode an = a.putArray("related_artists");

		
		for (Model relatedArtist : relatedArtists) {
			an.add(Tools.jsonToNode(relatedArtist.toJson(false)));
		}
		
		ArrayNode ab = a.putArray("tags");
		
		for (ArtistTagView tag : tags) {
			ab.add(Tools.jsonToNode(tag.toJson(false)));
		}


		return a;
		

		
		
	}
	
}
