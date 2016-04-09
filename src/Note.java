import java.util.Timer;
import java.util.TimerTask;

public class Note implements Comparable<Note> {
	
	// time in milliseconds for an A0 to decay * frequency of A0
	// right now is set to 5 seconds
	private static final long decay_factor = (long)(int)(1000 * 3 * 16.352);
	private static final long damp_decay = (long)(int)(1000 * 10 * 16.352);
	
	// value is the index of the note in a regular 88 key piano, where 1 is A0
	// key represents what the note is, where 0 is A, 1 is A#, etc. 
	// octave is the octave of the key in scientific pitch notation
	private int value, key, octave, velocity;
	private NoteBuffer parent;
	
	// frequency is the frequency of the note, assuming A4 is 440Hz
	private double frequency;
	private byte id;
	private boolean damped;
	private Timer timer;
	
	// variables for calculating decay
	private long startTime;
	private long endTime;
	
	/*
	 * Generates a new note object, given the MIDI byte which was received and a
	 * boolean representing whether or not the pedal is down when the note was entered
	 */
	public Note(byte id, byte vel, boolean damped, long time, NoteBuffer parent) {
		this.parent = parent;
		this.id = id;
		this.velocity = vel;
		this.damped = damped;
		this.value = (int)id - 20;
		this.key = (value - 1)%12;
		this.octave = (value + 8)/12;
		this.frequency = get_frequency(value);
		this.startTime = time;
	}
	
	public Note(Note n) {
		this.parent = n.parent;
		this.id = n.id;
		this.velocity = n.velocity;
		this.value = n.value;
		this.key = n.key;
		this.octave = n.octave;
		this.frequency = n.frequency;
	}
	
	/*
	 * Calculates the frequency of the note, assuming scientific pitch (A4 = 440Hz)
	 * This might be replaced with a lookup table if it becomes too slow
	 */
	private double get_frequency(int value) {
		return 55 * Math.pow(2.0, ((double)value - 13.0)/12);
	}
	
	public void release(long time, boolean damped) {
		this.damped = damped;
		this.endTime = time;
		long held = (this.endTime - this.startTime)/1000;
		long decay_time = (long)(int)(1.0/this.frequency * damp_decay);
		long remain_time = decay_time - held;
		if (remain_time < 0) {
			if (damped) { parent.mark_note(this); }
			else { parent.decay_note(this); }
			return;
		}	
		
		if (this.timer == null) { this.timer = new Timer(); }
		if (damped) {
			this.timer.schedule(new ReleaseTask(this), remain_time);
		} else {
			this.timer.schedule(new ReleaseTask(this), remain_time * (decay_factor / damp_decay));
		}
	}
	
	/*
	 * Undamps the note, starting the timer for its decay. There is no damp method, since
	 * you cannot damp a note once it has been played. Should only be called if the note
	 * has already been released.
	 */
	public void undamp(long time) {
		if (damped) {
			this.damped = false;
			long held = (time - this.startTime) / 1000;
			long decay_time = (long)(int)(1.0/this.frequency * decay_factor);
			long remain_time = decay_time - held;
			if (remain_time < 0) {
				parent.decay_note(this);
				return;
			}	
						
			if (this.timer != null) { this.timer.cancel(); }
			this.timer = new Timer();
			this.timer.schedule(new ReleaseTask(this), remain_time * (decay_factor / damp_decay));
		}
	}
		
	/*
	 * Getter methods for note variables: frequency, key, octave, and damped
	 */
	public byte id() { return this.id; }
	public int key() { return this.key; }
	public int vel() { return this.velocity; }
	public int value() { return this.value; }
	public int octave() { return this.octave; }
	public long get_end() { return this.endTime; }
	public long get_start() { return this.startTime; }
	public double freq() { return this.frequency; }
	public boolean is_damped() { return this.damped; }
	public NoteBuffer get_parent() { return this.parent; }

	/*
	 * Compares two notes with each other, returning the difference between them in half steps
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Note n) {
		return this.id - n.id();
	}
	
	public void destroy() {
		if (this.timer != null) {
			this.timer.cancel();
		}
	}
	
	public String toString() { return Music.getNoteName(this); }
	
	class ReleaseTask extends TimerTask {
		private Note pnote;
		public ReleaseTask(Note n) {
			super();
			this.pnote = n;
		}
		public void run() {
			if (damped) { parent.mark_note(pnote); }
			else { parent.decay_note(pnote); }
		}
	}
	
}