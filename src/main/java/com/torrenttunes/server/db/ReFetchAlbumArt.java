package com.torrenttunes.server.db;

import com.torrenttunes.server.Tools;

// java -cp target/torrenttunes-server.jar com.torrenttunes.server.db.ReFetchAlbumArt

public class ReFetchAlbumArt {
	public static void main(String[] args) {
		Tools.dbInit();
		Actions.refetchAlbumArt();
		Tools.dbClose();
	}
}
