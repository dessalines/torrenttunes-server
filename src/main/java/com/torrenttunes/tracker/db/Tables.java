package com.torrenttunes.tracker.db;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

public class Tables {
	
	@Table("song")
	public static class Song extends Model {}
	public static final Song SONG = new Song();

	@Table("serialized_data")
	public static class SerializedData extends Model {}
	public static final SerializedData SERIALIZED_DATA = new SerializedData();

	
}
