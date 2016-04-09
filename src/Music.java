public class Music {
	public static final String s = "#";	//"\u266f";
	public static final String f = "b";	//"\u266d";
	public static final String n = "n";	//"\u266e";
	
	public static String getNoteName(Note nt) {
		int key = nt.key();
		int oct = nt.octave();
		String name = "";
		switch(key) {
			case 0: 	name = "A"; 	break;
			case 1: 	name = "A" + s; break;
			case 2: 	name = "B"; 	break;
			case 3: 	name = "C"; 	break;
			case 4: 	name = "C" + s; break;
			case 5: 	name = "D"; 	break;
			case 6: 	name = "D" + s; break;
			case 7: 	name = "E"; 	break;
			case 8: 	name = "F"; 	break;
			case 9: 	name = "F" + s; break;
			case 10: 	name = "G"; 	break;
			case 11: 	name = "G" + s; break;
		}
		return name + oct;
	}
	
	public static String getNoteName(byte id) {
		int value = (int)id - 20;
		int key = (value - 1)%12;
		int oct = (value + 8)/12;
		String name = "";
		switch(key) {
			case 0: 	name = "A"; 	break;
			case 1: 	name = "A" + s; break;
			case 2: 	name = "B"; 	break;
			case 3: 	name = "C"; 	break;
			case 4: 	name = "C" + s; break;
			case 5: 	name = "D"; 	break;
			case 6: 	name = "D" + s; break;
			case 7: 	name = "E"; 	break;
			case 8: 	name = "F"; 	break;
			case 9: 	name = "F" + s; break;
			case 10: 	name = "G"; 	break;
			case 11: 	name = "G" + s; break;
		}
		return name + oct;
	}
	
	public static String getRoot(int key) {
		String name = "";
		switch(key) {
			case 0: 	name = "A"; 	break;
			case 1: 	name = "A" + s; break;
			case 2: 	name = "B"; 	break;
			case 3: 	name = "C"; 	break;
			case 4: 	name = "C" + s; break;
			case 5: 	name = "D"; 	break;
			case 6: 	name = "D" + s; break;
			case 7: 	name = "E"; 	break;
			case 8: 	name = "F"; 	break;
			case 9: 	name = "F" + s; break;
			case 10: 	name = "G"; 	break;
			case 11: 	name = "G" + s; break;
		}
		return name;
	}
	
	public static String getSimplestKey(int key) {
		String name = "";
		switch(key) {
			case 0: 	name = "A"; 	break;
			case 1: 	name = "B" + f; break;
			case 2: 	name = "B"; 	break;
			case 3: 	name = "C"; 	break;
			case 4: 	name = "D" + f; break;
			case 5: 	name = "D"; 	break;
			case 6: 	name = "E" + f; break;
			case 7: 	name = "E"; 	break;
			case 8: 	name = "F"; 	break;
			case 9: 	name = "F" + s; break;
			case 10: 	name = "G"; 	break;
			case 11: 	name = "A" + f; break;
		}
		return name;
	}
	
	public static int getKey(String name) {
		char k = name.toUpperCase().charAt(0);
		char t = (name.length() > 1 ? name.charAt(1) : 'n');
		int ret = 0;
		switch(k) {
			case 'A': ret = 0; break;
			case 'B': ret = 2; break;
			case 'C': ret = 3; break;
			case 'D': ret = 5; break;
			case 'E': ret = 7; break;
			case 'F': ret = 8; break;
			case 'G': ret = 10; break;
		}
		if (t == 'b') return (ret+11)%12;
		if (t == 'n') return ret;
		if (t == '#') return ret+1;
		return -1;
	}

	public static int keyOf(byte id) {
		return ((int)id - 21) % 12;
	}
	
	public static int majIndexOf(int base) {
		switch (base) {
			case 1: return 0;
			case 2: return 2;
			case 3: return 4;
			case 4: return 5;
			case 5: return 6;
			case 6: return 7;
			case 7: return 8;
			default: return -1;
		}
	}
}