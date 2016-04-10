import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Arrays;

public class Chord {
	private static final String[] roman = {"", "I", "II", "III", "IV", "V", "VI", "VII"};
	private static final String[] types = {"\u00ec  ", "", "\u00ef  "};
	private static final String[] invs3 = {"", "6", "64"};
	private static final String[] invs7 = {"7", "65", "43", "\u00d1"};
	private static final String[] quals = {"\u00ba", "\u00f8", "&"};
	
	public String name;
	// inv: 0, 1, 2, or 3 for each respective inversion
	// type: 0, 1, or 2 for flat, natural, or sharp
	// susp: 0, 2, or 4, depending on suspension
	public byte inv, type, susp;
	
	// qual: M, m, d, h, or a, for Major, minor, diminished, half-diminished, or augmented
	public char qual;
	
	// base: number representing root position, is the chord being tonicized in the case of secondary dom
	public short base;
	
	// true if the chord is a seven chord
	public boolean seven;
	
	public byte[] notes;
	public String code;
	
	/*
	 * FORMATTING FOR CHORD NAMES:
	 * 
	 * STANDARD:
	 * A-BCDX
	 * A: number with value 1-7, indicating position of root
	 * B: 0, 1, or 2, indicating flat, natural, or sharp, respectively
	 * C: 0, 1, 2, or 3, indicating which inversion the chord is in
	 * D: 0 or 7, indicating whether the chord is a triad or a seventh
	 * X: M, m, d, or h, indicating major, minor, diminished, or half-diminished
	 * 
	 * SECONDARY DOMINANT:
	 * FA-BCX
	 * F: literally the character "F"
	 * A: number with 1-7, indicating position of the chord being tonicized
	 * B: 0, 1, 2, or 3, indicating the inversion of the chord
	 * C: 0 or 7, indicating whether the chord is a triad or a seventh
	 * X: M or m, indicating major, minor for the chord being tonicized
	 * 
	 * SUSPENSIONS:
	 * SA-BCD
	 * S: literally the character "S"
	 * A: number 1-7, indicating position of the chord being suspended
	 * B: 2 or 4, for sus2 or sus4
	 * C: 0 or 7, indicating whether the chord is a triad or a seventh
	 * D: M or m, indicating major or minor suspension
	 * 
	 * SPECIAL
	 * CAD64-AB
	 * A: number 1-7, indicating the position of the chord being resolved to
	 * B: M or m, indicating major or minor of the A
	 */
	public Chord (String code) {
		this.code = code;
		if (!code.equals("")) {
			switch (code.charAt(0)) {
				case 'F': 
					this.base = Short.parseShort(code.charAt(1) + "");
					this.inv = Byte.parseByte(code.charAt(3) + "");
					this.type = 1; // assume all secondary dominants are not NSTs
					this.seven = code.charAt(4) == '7';
					this.qual = code.charAt(5); // assume all secondary dominants are major
					this.susp = 0; // assume all secondary dominants aren't suspended
					break;
				case 'S':
					this.base = Short.parseShort(code.charAt(1) + "");
					this.inv = 0; // assume all suspensions are in root position
					this.type = 1; // assume all suspensions are not NSts
					this.seven = code.charAt(4) == '7';
					this.qual = code.charAt(5);
					this.susp = Byte.parseByte(code.charAt(3) + "");
					break;
				case 'C':
					this.base = Short.parseShort(code.charAt(6) + "");
					this.qual = code.charAt(7);
					this.inv = 0; this.type = 1; this.seven = false; // lot of assumptions
					this.susp = 0;// assume cadential 6-4s are not suspended and major
					break;
				default:
					this.base = Short.parseShort(code.charAt(0) + "");
					this.type = Byte.parseByte(code.charAt(2) + "");
					this.inv = Byte.parseByte(code.charAt(3) + "");
					this.seven = code.charAt(4) == '7';
					this.qual = code.charAt(5);
					this.susp = 0;
			}
		}
	}
	
	public String code() { return this.code; }
	
	public String get_roman_name() {
		String ret = "";
		if (code.equals("")) return "";
		switch (code.charAt(0)) {
			case 'F':
				ret += "V";
				if (seven) ret += invs7[inv];
				else ret += invs3[inv];
				ret += "/";
				ret += (qual == 'M' ? roman[base] : roman[base].toLowerCase());
				break;
			case 'S':
				ret += roman[base];
				if (qual == 'm') ret = ret.toLowerCase();
				ret += susp + " _" + (susp-1);
				break;
			case 'C':
				ret += "cadV64";
				if (base != 1) ret += "/" + (qual == 'M' ? roman[base] : roman[base].toLowerCase());
				break;
			default:
				ret += types[type];
				ret += (qual == 'M' ? roman[base] : roman[base].toLowerCase());
				switch (qual) {
					case 'd': ret += quals[0]; break;
					case 'h': ret += quals[1]; break;
					case 'a': ret += quals[2]; break;
				}
				if (seven) ret += invs7[inv];
				else ret += invs3[inv];
		}
		return ret;
	}
	
	public String get_sym_name(KeySignature k) {
		return "";
	}
	
	public byte[] get_notes(KeySignature k) {
		return notes;
	}
	
	public int base_index() {
		if (code.equals("")) return -1;
		else switch (base) {
			case 1: return (type - 1)%12;
			case 2: return (2 + type - 1)%12;
			case 3: return (4 + type - 1)%12;
			case 4: return (5 + type - 1)%12;
			case 5: return (7 + type - 1)%12;
			case 6: return (9 + type - 1)%12;
			case 7: return (11 + type - 1)%12;
			default: return -1;
		}
	}
	
	public char equivalent_qual() {
		switch (code.charAt(0)) {
			case 'C': return 'M';
			case 'F': return 'M';
			case 'S': return qual;
			default: return qual;
		}
	}
	
	public int equvalent_base() {
		switch (code.charAt(0)) {
			case 'C': return Music.majIndexOf(base);
			case 'F': return Music.majIndexOf((base+3)%7+1);
			case 'S': return Music.majIndexOf(base);
			default: return base_index();
		}
	}
	
	public int get_seven() {
		if (code.charAt(0) == 'C') return -1;
		if (!seven) return -1;
		else {
			int root = base;
			if (code.charAt(0) == 'F') root = (root+3)%7 + 1;
			switch (root) {
				case 1: if (code.charAt(0) == 'F') return 10; else return 11;
				case 2: if (qual == 'd') return 11; else return 0;
				case 3: return 2;
				case 4: return 4;
				case 5: return 5;
				case 6: return 7;
				case 7: return 8;
				default: return -1;
			}
		}
	}
	
	public int[] get_chord_tones() {
		if (code.equals("")) return new int[0];
		int ind = base_index();
		switch (code.charAt(0)) {
			case 'F': 
				if (!seven) return new int[] { (ind+7)%12, (ind+2)%12, (ind+11)%12 };
				else return new int[] { (ind+7)%12, (ind+2)%12, (ind+11)%12, get_seven() };
			case 'S':
				if (!seven && susp == 2) return new int[] { ind, Music.majIndexOf(base%7+1), Music.majIndexOf((base+3)%7+1) };
				if (!seven && susp == 4) return new int[] { ind, Music.majIndexOf((base+2)%7+1), Music.majIndexOf((base+4)%7+1) };
				if (seven && susp == 2) return new int[] { ind, Music.majIndexOf(base%7+1), Music.majIndexOf((base+3)%7+1), get_seven() };
				if (seven && susp == 4) return new int[] { ind, Music.majIndexOf((base+2)%7+1), Music.majIndexOf((base+4)%7+1), get_seven() };
				else return new int[0];
			case 'C':
				return new int[] { (ind+7)%12, ind, qual == 'M' ? (ind+4)%12 : (ind+3)%12 };
			default: 
				switch(qual) {
					case 'd': 
						if (!seven) return new int[] { ind, (ind+3)%12, (ind+6)%12 };
						else return new int[] { ind, (ind+3)%12, (ind+6)%12, (ind+9)%12 };
					case 'M':
						if (!seven) return new int[] { ind, (ind+4)%12, (ind+7)%12 };
						else return new int[] { ind, (ind+4)%12, (ind+7)%12, get_seven() };
					case 'm':
						if (!seven) return new int[] { ind, (ind+3)%12, (ind+7)%12 };
						else return new int[] { ind, (ind+3)%12, (ind+7)%12, (ind+10)%12 };
					case 'h': return new int[] { ind, (ind+3)%12, (ind+6)%12, (ind+10)%12 };
					default: return new int[0];
				}
		}
	}
	
	public boolean is_chord_tone(int index) {
		if (code.equals("")) return false;
		int[] cts = get_chord_tones();
		if (has(cts, index)) return true;
		return false;
	}
	
	private static boolean has(int[] in, int k) { boolean f = false; for (int i : in) if (i == k) f = true; return f; }
	
	public void draw_roman(Graphics2D g, int x, int y, int size) {
		g.setFont(new Font("Opus Chords Std", Font.PLAIN, size));
		int fw = g.getFontMetrics().stringWidth(get_roman_name());
		g.drawString(get_roman_name(), x - (fw/2), y);
	}
	
	/* returns an integer 0 through 11, where 0 represents A, assuming the context of
	 * a given key
	 */
	public String get_name_context_free(int context) {
		switch (qual) {
		case 'M': case 'm':
			if (this.code.charAt(0) != 'F') 
				return Music.getRoot(((this.base == 1 ? 0 : (Analyzer.maj_arr[this.base - 2])) + context)%12) + qual + (seven?"7":"");
			return Music.getRoot(((this.base == 1 ? 0 : (Analyzer.maj_arr[this.base - 2])) + 7 + context)%12) + qual;	
		default: return "not maj/min";
		}
	}
	
	public void draw_symbol(Graphics2D g, int x, int y, int size, KeySignature k) {
		
	}
	
	public String toString() { return get_roman_name(); }
	
	public static void main(String [] args) {
		Chord c = new Chord("6-000m");
		System.out.println(c.base);
		System.out.println(c.get_roman_name());
		System.out.println(c.base_index());
		System.out.println(Arrays.toString(c.get_chord_tones()));
		System.out.println(c.is_chord_tone(3));
		System.out.println(c.get_name_context_free(3));
	}
}