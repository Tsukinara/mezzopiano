import java.util.ArrayList;

public class NoteBuffer {
	private static final int max_notes = 200;
	private static final int max_chords = 50;
	private static final int key_track = 10;
	public static final int same_thresh = 50000;
	public static final int tril_thresh = 500000;
	public static final int chord_thresh = 700000;
	private static final int poly = 10;
	private static final int bass_dist = 16;
	
	// contains any notes whose frequencies are dominant
	public ArrayList<Note> note_buffer;
	
	// contains the notes that are held at the time
	public ArrayList<Note> hold_buffer;
	
	// contains any notes that are relevant
	public ArrayList<Note> rel_buffer;
	public ArrayList<Note> tempo_buffer;
	public ArrayList<Note> all_buffer;
	public ArrayList<Note> history;
	public ArrayList<Note> key_analysis;
	
	public ArrayList<Integer> tempo_history;
	public int curr_tempo;
	
	//contains all notes within 1.5 octaves of the dominant overtone
	protected ArrayList<Note> bass;
	private ArrayList<Note> marks;
	protected ArrayList<Chord> chord_history;
	protected ArrayList<Long> chord_history_t;
	private int curr_index;
	
	private byte dominant;
	private Display parent;
	
	public boolean damped;
	public Chord curr_chord;
	public KeySignature curr_key;
	
	public AppCore.Mood curr_mood;
	
	/*
	 * Initializes the note buffer, as well as the hold buffer
	 */
	public NoteBuffer(Display parent) {
		this.parent = parent;
		reinit();
	}
	
	public void reinit() {
		this.note_buffer = new ArrayList<Note>();
		this.hold_buffer = new ArrayList<Note>();
		this.rel_buffer = new ArrayList<Note>();
		this.tempo_buffer = new ArrayList<Note>(max_notes);
		this.all_buffer = new ArrayList<Note>();
		this.history = new ArrayList<Note>();
		this.marks = new ArrayList<Note>();
		this.damped = false;
		this.curr_index = -1;
		this.chord_history = new ArrayList<Chord>();
		this.chord_history_t = new ArrayList<Long>();
		this.bass = new ArrayList<Note>();
		this.key_analysis = new ArrayList<Note>();
		this.dominant = -1;
		this.tempo_history = new ArrayList<Integer>();
		this.curr_tempo = -1;
		this.curr_mood = AppCore.Mood.M_NEUTRAL;
	}
	
	/*
	 * Adds a note to the hold buffer as well as the note buffer. Damped should
	 * always be true when this method is called. If the same note is already in the 
	 * note buffer, the most recent one is kept
	 */
	public synchronized void add_note(byte id, boolean damped, byte vel, long time) {
		Note n = new Note(id, vel, damped, time, this);
		Note tmp = null;
		add_circular(tempo_buffer, new Note(id, vel, damped, time, this));
		for (Note nt : note_buffer) {
			if (nt.id() == id) tmp = nt;
		}
		analyze_dominant(n);
		
		if (Math.abs(id - this.dominant) < bass_dist && !bass.contains(n)) { bass.add(n); }
		for (Note nt : bass) {
			if (same_time(nt, n) && !rel_buffer.contains(n)) rel_buffer.add(n);
			for (int i = history.size() - poly; i < history.size(); i++)
				if (i >= 0) {
					Note ntmp = history.get(i);
					if (same_time(nt, ntmp) && !rel_buffer.contains(ntmp)) rel_buffer.add(ntmp);
				}
		}
		note_buffer.remove(tmp);
		note_buffer.add(n);
		hold_buffer.add(n);
		all_buffer.add(n);
		add_history(n);
		add_akey(n);
		
		if (parent != null) parent.note_pressed(id, vel, time);
		recalculate(time);
	}
	
	public synchronized void change_dom(Note nt) {
		if (this.dominant != nt.id()) {
			this.dominant = nt.id();
			ArrayList<Note> tmp = new ArrayList<Note>();
			for (Note n : rel_buffer) if (!hold_buffer.contains(n)) tmp.add(n);
			for (Note n : tmp) rel_buffer.remove(n); tmp.clear();
			for (Note n : all_buffer) if (!hold_buffer.contains(n)) tmp.add(n);
			for (Note n : tmp) all_buffer.remove(n); tmp.clear();
			for (Note n : bass) if (!hold_buffer.contains(n)) tmp.add(n);
			for (Note n : tmp) bass.remove(n); tmp.clear();
			this.bass.add(nt);
			this.rel_buffer.add(nt);
		}
	}
	
	private synchronized void add_circular(ArrayList<Note> arr, Note n){
		if (arr.size() == max_notes) { destroy_note(arr.remove(0)); }
		arr.add(n);
	}
	
	private synchronized void add_circ_ch(ArrayList<Chord> arr, Chord c){
		if (arr.size() == max_chords) { arr.remove(0); }
		arr.add(c);
	}
	
	private synchronized void add_circ_ln(ArrayList<Long> arr, long l){
		if (arr.size() == max_chords) { arr.remove(0); }
		arr.add(l);
	}
	
	private synchronized void add_history(Note n) {
		history.add(n);
		if (history.size() > max_notes) {
			Note tmp = history.remove(0);
			destroy_note(tmp);
		}
	}
	
	private synchronized void analyze_dominant(Note n) {
		if (dominant == -1) change_dom(n);
		else if (n.id() < dominant) change_dom(n);
		else if (Math.abs(n.id() - dominant) < 12) {
			int kkey = Music.getKey(curr_key.key + "" + curr_key.type);
			int po = (n.key() - kkey + 12)%12;
			if (curr_chord == null) change_dom(n);
			else if (!curr_chord.is_chord_tone(po) && n.id() < min_id(note_buffer)) change_dom(n);
		}
	}
	
	private boolean same_time(Note a, Note b) {
		return Math.abs(a.get_start() - b.get_start()) < same_thresh;
	}
	
	private void recalculate(long timeStamp) {
		if (parent.set.ksig == null) {
			KeySignature k = Analyzer.get_key_signature(key_analysis, this.curr_key);
			System.out.println((k!= null ? k.toString() : "unknown"));
			if (k != null) this.curr_key = k;
		} else this.curr_key = parent.set.ksig;
		String prev_chord = (curr_chord != null ? curr_chord.code : "");
		Chord c = Analyzer.get_chord(rel_buffer, all_buffer, dominant, curr_key, curr_chord);
		if (c != null) this.curr_chord = c;
		if (curr_chord != null && !prev_chord.equals(curr_chord.code)) {
			Analyzer.get_mood(this);
			add_circ_ch(chord_history, curr_chord);
			add_circ_ln(chord_history_t, timeStamp);
			parent.s_ac.chord_changed();
		}
		filter_chord_history();
		if (chord_history.size() - 3 >= 0 && chord_history.size() - 3 > curr_index) {
			curr_index = chord_history.size() - 3;
			parent.profile().add_chord(chord_history.get(curr_index), chord_history.get(curr_index+1));
		}
	}
	
	private void filter_chord_history() {
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		for (int i = 1; i < chord_history.size(); i++) {
			long t1 = chord_history_t.get(i-1);
			long t2 = chord_history_t.get(i);
			if (Math.abs(t2 - t1) < chord_thresh) tmp.add(i-1);
		}
		for (Integer i : tmp) {
			chord_history.remove((int)i);
			chord_history_t.remove((int)i);
		}
	}
	
	private synchronized void add_akey(Note n) {
		key_analysis.add(n);
		ArrayList<Note> tmp = new ArrayList<Note>();
		for (int i = 0; i < key_analysis.size(); i++) {
			Note nt = key_analysis.get(i);
			if (n.get_start() - nt.get_start() > key_track*1000000) tmp.add(nt);
			else break;
		}
		for (Note nt : tmp) key_analysis.remove(nt);
	}
	
	private synchronized int min_id(ArrayList<Note> n) {
		int min = 999;
		for (Note nt : n) if (nt.id() < min) min = nt.id();
		return min;
	}
	
	/*
	 * Releases a note from the hold buffer. If the damper is not down, the note is undamped
	 * as well. Otherwise, it remains in the note buffer.
	 */
	public synchronized void release_note(byte id, boolean damped, long time) {
		Note tmp = null;
		for (Note n : hold_buffer) {
			if (n.id() == id) tmp = n;
		}
		if (tmp != null) {
			hold_buffer.remove(tmp);
			tmp.release(time, damped);
		}
		if (parent != null) parent.note_released(id, time);
		recalculate(time);
	}
	
	/*
	 * Undamps all notes in the note buffer that aren't held at the time it is called.
	 * Should be called either when the damper is released or the note is released
	 */
	public synchronized void undamp(long time) {
		this.damped = false;
		ArrayList<Note> tmp = new ArrayList<Note>();
		for (Note n : note_buffer) if (!hold_buffer.contains(n)) tmp.add(n);
		for (Note n : tmp) n.undamp(time); tmp.clear();
		for (Note n : rel_buffer) if (!hold_buffer.contains(n)) tmp.add(n);
		for (Note n : tmp) rel_buffer.remove(n); tmp.clear();
		for (Note n : bass) if (!hold_buffer.contains(n)) tmp.add(n);
		for (Note n : tmp) bass.remove(n); tmp.clear();
		for (Note n : marks) note_buffer.remove(n);
		for (Note n : all_buffer) if (!hold_buffer.contains(n)) tmp.add(n);
		for (Note n : tmp) all_buffer.remove(n); tmp.clear();
		if (parent != null) parent.damp_released(time);
		byte min_id = 127;
		Note new_dom = null;
		for (Note n : hold_buffer) {
			if (n.id() < min_id) {
				min_id = n.id();
				new_dom = n;
			}
		}
		if (new_dom != null && Math.abs(dominant - new_dom.id()) < 12) change_dom(new_dom);
		recalculate(time);
	}
	
	public synchronized void damp(long time) {
		this.damped = true;
		byte min_id = 127;
		Note new_dom = null;
		for (Note n : hold_buffer) {
			if (n.id() < min_id) {
				min_id = n.id();
				new_dom = n;
			}
		}
		if (new_dom != null) change_dom(new_dom);
		if (parent != null) parent.damp_pressed(time);
		recalculate(time);
	}
	
	public byte dom() { return this.dominant; }
	
	public synchronized boolean is_held(int val) {
		for (Note n : hold_buffer) if (n.value() == val) return true;
		return false;
	}
	
	public synchronized boolean is_rel(int val) {
		for (Note n : rel_buffer) if (n.value() == val) return true;
		return false;
	}
	
	public synchronized boolean in_buf(int val) {
		for (Note n : note_buffer) if (n.value() == val) return true;
		return false;
	}
	
	/*
	 * Removes a note from the note buffer, and sets it to null before garbage collection.
	 * Should be called by the note itself when its decay timer runs out
	 */
	public synchronized void decay_note(Note n) {
		note_buffer.remove(n);
	}
	
	public synchronized void destroy_note(Note n) {
		n.destroy();
		n = null;
	}
	
	public synchronized void mark_note(Note n) {
		marks.add(n);
	}
	
	/*
	 * Returns the note buffer
	 */
	public ArrayList<Note> get_buffer() {
		return note_buffer;
	}
	
	/*
	 *  Debug methods below, for printing information about buffers
	 */
	public synchronized void print_holds() {
		System.out.print("CURRENTLY HELD: ");
		for (Note n : hold_buffer) System.out.print(Music.getNoteName(n) + ", ");
		System.out.println();
	}
	
	public synchronized void print_buffer() {
		System.out.print("CURRENTLY IN BUFFER: ");
		for (Note n : note_buffer)	System.out.print(Music.getNoteName(n) + ", ");
		System.out.println();
	}
	
	public synchronized void print_history() {
		System.out.print("HISTORY: ");
		for (Note n : history) System.out.print(Music.getNoteName(n) + ", ");
		System.out.println();
	}
}