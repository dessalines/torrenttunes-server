package com.torrenttunes.server.tools;

import com.torrenttunes.server.DataSources;

public class ChangeTorrentTrackers {

	
	public static void main(String[] args) {
		com.torrenttunes.client.tools.DataSources.APP_NAME = DataSources.APP_NAME;
		
		com.torrenttunes.client.tools.ChangeTorrentTracker.main(null);
	}
}
