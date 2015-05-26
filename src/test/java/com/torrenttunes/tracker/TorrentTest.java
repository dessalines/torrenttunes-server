package com.torrenttunes.tracker;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;

import com.torrenttunes.tracker.DataSources;
import com.torrenttunes.tracker.Tools;
import com.turn.ttorrent.common.Torrent;

public class TorrentTest extends TestCase {
	
	Torrent torrent;
	
	@Before
	public void setUp() throws IOException {
		torrent = Torrent.load(new File(DataSources.SAMPLE_TORRENT_FILE()));
		
	}
	
	public void testDerp() throws IOException {
//		Tools.torrentInfo(torrent);
	
	}
	
	public void testConvertToMagnetLink() {
		assertEquals("magnet:?xt=urn:btih:09c17295ccc24af400a2a91495af440b27766b5e&dn=Fugazi+-+Studio+Discography+1989-2001+%5BFLAC%5D", 
				Tools.convertTorrentToMagnetLink(torrent));
	}
	
	public void testSerialization() {
	
		byte[] ser = Tools.serializeTorrentFile(torrent);
		
		System.out.println(ser);
		Torrent deser = Tools.deserializeTorrentFile(ser);
		
		Tools.torrentInfo(deser);
		System.out.println(deser.getName());
		
//		Torrent t = Tools.deserializeTorrentFile(data);
		
//		Tools.saveTorrentToFile(t);
	}
	
	public void testExtractInfoHash() {
		String magnetLink = "magnet:?xt=urn:btih:09c17295ccc24af400a2a91495af440b27766b5e&dn=Fugazi+-+Studio+Discography+1989-2001+%5BFLAC%5D";
		
	
		System.out.println(Tools.extractInfoHashFromMagnetLink(magnetLink));
			
	}

}
