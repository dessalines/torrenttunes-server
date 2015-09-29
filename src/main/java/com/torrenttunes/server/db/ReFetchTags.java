package com.torrenttunes.server.db;

import com.torrenttunes.server.tools.Tools;

//java -cp target/torrenttunes-server.jar com.torrenttunes.server.db.ReFetchTags

public class ReFetchTags {
	public static void main(String[] args) {
		Tools.dbInit();	
		Actions.refetchTags();
		Tools.dbClose();
	}
}

