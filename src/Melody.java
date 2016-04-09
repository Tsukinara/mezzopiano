import java.util.ArrayList;

public class Melody {
	private final static int treble_base = 57;
	private final static int bass_base = 33;
	
	public int start_index, start_octave;
	public int next_index;
	public int chord_index;
	
	public char tsig, qual;
	public Type type;
	public boolean seven;
	
	public enum Type { TREBLE, BASS };
	
	public int[] times;
	public int[] notes;
	public int[] lens;
	
	public Melody(String code) {
		try {
			String id = code.substring(0, code.indexOf(":") - 1);
			String rest = code.substring(code.indexOf(":") + 2, code.length());
			int num_notes = num_instances(rest, '-');
			times = new int[num_notes]; notes = new int[num_notes]; lens = new int[num_notes];
			parse_id(id);
			parse_melody(rest);
		} catch (Exception e) {
			System.err.println("Error: Invalid synthesis file:");
			System.err.println(code);
		}
	}
	
	private void parse_id(String id) {
		chord_index = Integer.parseInt(id.substring(0, id.indexOf('-')));
		id = id.substring(id.indexOf('-') + 1, id.length());
		seven = id.charAt(0) == '7';
		tsig = id.charAt(1); qual = id.charAt(3);
		type = (id.charAt(2) == 'T' ? Type.TREBLE : Type.BASS);
		next_index = Integer.parseInt(id.substring(id.indexOf('-') + 1, id.length()));
	}
	
	private void parse_melody(String mel) {
		for (int i = 0; mel.contains("-"); i++) {
			notes[i] = Integer.parseInt(mel.charAt(1) + "") + 12*Integer.parseInt(mel.charAt(2) + "");
			lens[i] = Integer.parseInt(mel.substring(3, mel.indexOf('-')));
			times[i] = Integer.parseInt(mel.substring(mel.indexOf('-') + 1, mel.indexOf(')')));
			mel = mel.substring(mel.indexOf(')') + 1, mel.length());
		}
		start_index = notes[0]%12;
	}
	
	public int[] get_held_notes(double time) {
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		for (int i = 0; i < notes.length; i++) {
			if (time >= times[i] && time < times[i] + lens[i]) tmp.add(i);
		}
		int[] ret = new int[tmp.size()];
		for (int i = 0; i < tmp.size(); i++) ret[i] = notes[tmp.get(i)] + (type == Type.TREBLE ? treble_base : bass_base);
		return ret;
	}
	
	private int num_instances(String s, char c) {
		int total = 0; for (int i = 0; i < s.length(); i++) if (s.charAt(i) == c) total++; return total;
	}
	
	public String toString() {
		String s = "";
		s += "Chord index: " + chord_index + "\n";
		s += "Start index: " + start_index + "\n";
		s += "Next index:  " + next_index + "\n";
		s += "Is 7 chord?: " + seven + "\n";
		s += "TSig. Type:  " + tsig + "\n";
		s += "Chord qual.: " + qual + "\nNOTES:\n";
		for (int i = 0; i < notes.length; i++) s += notes[i] + ":" + lens[i] + ":" + times[i] + " ;; ";
		return s;
	}
}