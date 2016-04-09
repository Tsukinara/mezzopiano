import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public class Harmonizer {	
	private Receiver midiReceiver;
	private static final int std_vel = 127;
	private Melody curr_melody;
	private Melody bass_melody;
	private ArrayList<Integer> curr_held;
	private AppCore parent;
	
	public HashMap<Integer, ArrayList<Melody>> melodies;
	
	public Harmonizer (AppCore parent, String filename) {
		this.parent = parent;
		this.curr_held = new ArrayList<Integer>();
		this.melodies = new HashMap<Integer, ArrayList<Melody>>();
		ArrayList<String> melodies = new ArrayList<String>();
 		try {
			Scanner read = new Scanner(new File(filename));
			while (read.hasNextLine()) { 
				String tmp = read.nextLine();
				if (tmp.length() > 0 && tmp.charAt(0) != '%') melodies.add(tmp);	
			}
			midiReceiver = MidiSystem.getReceiver();
			parse_melodies(melodies);
			read.close();
		} catch (FileNotFoundException e) {
			System.err.println("Unable to find synthesis file at: " + filename);
		} catch (MidiUnavailableException e) {
			System.err.println("Midi Unavailable Exception Thrown");
		}
	}
	
	private void parse_melodies(ArrayList<String> mel) {
		for (String s : mel) {
			Melody tmp = new Melody(s);
			if (!melodies.containsKey(tmp.chord_index)) melodies.put(tmp.chord_index, new ArrayList<Melody>());
			this.melodies.get(tmp.chord_index).add(tmp);
		}
	}	
	
	public void match_melody_to(Chord c, int[]nexts) {
		if (c == null) return;
		int root_ind = c.equvalent_base();
		if (nexts.length == 0 && melodies.containsKey(root_ind)) {
			ArrayList<Melody> t_cs = melodies.get(root_ind);
			ArrayList<Melody> b_cs = new ArrayList<Melody>();
			if (t_cs.size() == 0) { 
				// System.err.println("Unable to find harmony for: " + c.toString()); 
				curr_melody = null;
				bass_melody = null;
				return; 
			}
			for (int i = 0; i < t_cs.size(); i++) 
				if (t_cs.get(i).type == Melody.Type.BASS) b_cs.add(t_cs.remove(i--));
			if (b_cs.size() != 0) this.bass_melody = b_cs.get((int)(Math.random() * b_cs.size()));
			if (t_cs.size() != 0) this.curr_melody = t_cs.get((int)(Math.random() * t_cs.size()));
		}
		else if (melodies.containsKey(root_ind)) {
			ArrayList<Melody> t_cs = melodies.get(root_ind);
			ArrayList<Melody> b_cs = new ArrayList<Melody>();
			
			if (t_cs.size() == 0) { 
				// System.err.println("Unable to find harmony for: " + c.toString()); 
				curr_melody = null;
				bass_melody = null;
				return; 
			}
			for (int i = 0; i < t_cs.size(); i++) 
				if (t_cs.get(i).type == Melody.Type.BASS) b_cs.add(t_cs.remove(i--));
			
			if (b_cs.size() != 0) this.bass_melody = b_cs.get((int)(Math.random() * b_cs.size()));
			ArrayList<Melody> finals = new ArrayList<Melody>();
			for (int i : nexts) {
				for (Melody m : t_cs) if (m.next_index == i) finals.add(m);
			}
			System.out.println("FINALS: " + finals + " : " + finals.size());
			if (finals.size() == 0) {
				System.err.println("Unable to find perfect harmony. Using arbitrary harmony.");
				int index = (int)(Math.random() * t_cs.size());
				this.curr_melody = t_cs.get(index);
			} else {
				System.out.println("here");
				this.curr_melody = finals.get((int)(Math.random() * finals.size()));
				System.out.println(curr_melody);
			}
		}
		else {
			// System.err.println("Unable to find harmony for: " + c.toString());
			bass_melody = null;
			curr_melody = null;
		}
	}
	
	public void play_melody(double time, int kkey) {
		int[] bnotes = (bass_melody != null ? bass_melody.get_held_notes(time) : new int[0] );
		int[] notes = (curr_melody != null ? curr_melody.get_held_notes(time) : new int[0] );
		for (int i : notes)
			if (!curr_held.contains(i)) {
				play_note(i + kkey, std_vel);
				curr_held.add(i);
				parent.h_note_pressed((byte)(i+kkey), (byte)std_vel);
			}
		for (int i : bnotes)
			if (!curr_held.contains(i)) {
				play_note(i + kkey, std_vel);
				curr_held.add(i);
				parent.h_note_pressed((byte)(i+kkey), (byte)std_vel);
			}
		for (int i = 0; i < curr_held.size(); i++) {
			if (!has(bnotes, (int)curr_held.get(i)) && !has(notes, (int)curr_held.get(i))) {
				stop_note(curr_held.get(i) + + kkey, 0);
				curr_held.remove(i);
			}
		}
	}
	
	private static boolean has(int[] in, int k) { boolean f = false; for (int i : in) if (i == k) f = true; return f; }
	
	public void play_note(int note, int velocity) {
		try{
			ShortMessage myMsg = new ShortMessage();
			myMsg.setMessage(ShortMessage.NOTE_ON, 0, note, velocity);
			long timeStamp = System.nanoTime()/1000;
			midiReceiver.send(myMsg, timeStamp);
		} catch (InvalidMidiDataException e) {
			System.err.println("Invalid MIDI Data Exception Thrown");
		}
	}
	
	public void stop_note(int note, int velocity) {
		try{
			ShortMessage myMsg = new ShortMessage();
			myMsg.setMessage(ShortMessage.NOTE_OFF, 0, note, velocity);
			long timeStamp = System.nanoTime()/1000;
			midiReceiver.send(myMsg, timeStamp);
		} catch (InvalidMidiDataException e) {
			System.err.println("Invalid MIDI Data Exception Thrown");
		}
	}
	
	public boolean is_held(int val, int kkey) {
		return curr_held.contains(val + 20 - kkey);
	}
	
	public static void main(String[] args) {
		Harmonizer h = new Harmonizer(null, "resources\\synthesis.dat");
		h.match_melody_to(new Chord("1-100M"), new int[] { 7 });
		System.out.println(h.curr_melody);
		for (int i = 0; i < 60; i++) {
			System.out.println(i);
			try {
				h.play_melody(i, Music.getKey("C"));
				Thread.sleep(50);
			} catch (Exception e) {e.printStackTrace();}
		}
	}
}