import java.io.BufferedInputStream;
import java.io.FileInputStream;

import javazoom.jl.player.Player;


public class MPlayer {
	private String filename;
	private Player player; 

	// constructor that takes the name of an MP3 file
	public MPlayer(String filename) {
		this.filename = filename;
	}

	public void close() { if (player != null) player.close(); }

	// play the MP3 file to the sound card
	public void play() {
		try {
			FileInputStream fis = new FileInputStream(filename);
			BufferedInputStream bis = new BufferedInputStream(fis);
			player = new Player(bis);
		}
		catch (Exception e) {
			System.out.println("Problem playing file " + filename);
			System.out.println(e);
		}

		new Thread() {
			public void run() {
				try { player.play(); }
				catch (Exception e) { System.out.println(e); }
			}
		}.start();
	}
	
	public void loop() {
		try {
			FileInputStream fis = new FileInputStream(filename);
			BufferedInputStream bis = new BufferedInputStream(fis);
			player = new Player(bis);
		}
		catch (Exception e) {
			System.out.println("Problem playing file " + filename);
			System.out.println(e);
		}

		new Thread() {
			public void run() {
				try { 
					while (true) {
						player.play();
						while (!player.isComplete()) {}
						player.close();
						FileInputStream fis = new FileInputStream(filename);
						BufferedInputStream bis = new BufferedInputStream(fis);
						player = new Player(bis);
					}
				}
				catch (Exception e) { System.out.println(e); }
			}
		}.start();
	}
}