package com.ytm;

import java.io.File;
import java.io.IOException;

import org.junit.Before;

import junit.framework.TestCase;

import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedTorrent;

public class TorrentTest extends TestCase {
	
	Torrent torrent;
	
	@Before
	public void setUp() throws IOException {
		torrent = Torrent.load(new File(DataSources.SAMPLE_TORRENT_FILE()));
		
	}
	
	public void testDerp() throws IOException {
		Tools.torrentInfo(torrent);
	
	}
	
	public void testConvertToMagnetLink() {
		assertEquals("magnet:?xt=urn:btih:09c17295ccc24af400a2a91495af440b27766b5e&dn=Fugazi+-+Studio+Discography+1989-2001+%5BFLAC%5D", 
				Tools.convertTorrentToMagnetLink(torrent));
	}

}
