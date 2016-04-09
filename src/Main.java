public class Main {
	public static void main (String [] args) {
		Display d = new Display();
		NoteBuffer nb = new NoteBuffer(d);
		d.set_buffer(nb);
		MidiHandler mh = new MidiHandler(nb);
		d.begin();
		mh.imNotUseless();
	}
}