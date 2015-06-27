package com.torrenttunes.tracker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.servlet.ServletOutputStream;

import junit.framework.TestCase;

import org.junit.Before;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.torrenttunes.server.DataSources;
import com.torrenttunes.server.Tools;
import com.torrenttunes.server.webservice.API;
import com.turn.ttorrent.common.Torrent;

public class TorrentTest extends TestCase {

	Torrent torrent;

	@Before
	public void setUp() throws IOException {
		//		torrent = Torrent.load(new File(DataSources.SAMPLE_TORRENT_FILE()));

	}

	public void testDerp() throws IOException {
		//		Tools.torrentInfo(torrent);

	}

	public void testWikimediaImageLocation() {

		String ninWmLink = "https://commons.wikimedia.org/wiki/File:Nine_inch_nails_-_Staples_Center_-_11-8-13_(10755555065_16053de956_o).jpg";
		String imageURL = Tools.convertWikimediaCommonsURLToImageUrl(ninWmLink);
		System.out.println(imageURL);
		assertEquals("https://upload.wikimedia.org/wikipedia/commons/5/5b/Nine_inch_nails_-_Staples_Center_-_11-8-13_(10755555065_16053de956_o).jpg", imageURL);


	}
	
	public void testWikiImage() {
		String wikiUrl = "https://en.wikipedia.org/wiki/Sufjan_Stevens";
		String image = Tools.getImageFromWikipedia(wikiUrl);
		System.out.println(image);
	}
	
	public void testWrite() throws IOException {
		int[] fromTo = API.fromTo(new File(DataSources.SAMPLE_SONG), "bytes=0-");
		int length = (int) (fromTo[1] - fromTo[0] + 1);
		final RandomAccessFile raf = new RandomAccessFile(DataSources.SAMPLE_SONG, "r");
		raf.seek(fromTo[0]);
		
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		API.writeAudioToOS(length, raf, bos);
		raf.close();
//		bos.close();
	}


}
