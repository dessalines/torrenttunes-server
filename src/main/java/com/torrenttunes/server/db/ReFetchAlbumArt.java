package com.torrenttunes.server.db;

import com.torrenttunes.server.Tools;

public class ReFetchAlbumArt {
	public static void main(String[] args) {
		Tools.dbInit();
		Actions.refetchAlbumArt();
		Tools.dbClose();
	}
}
