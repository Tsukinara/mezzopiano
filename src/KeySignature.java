public class KeySignature {
	public char key;
	public char type;
	public boolean major;
	public byte[] signature;
	
	public KeySignature(String name, boolean major) {
		this.major = major;

		if (major && key > 90) key = (char)(key-32);
		if (!major && key < 96) key = (char)(key+32);
		if (name.length() > 1)
			this.type = name.charAt(1);
		else this.type = 'n';
	}
	
	public String get_key_name(Note n) {
		return "";
	}
	
	public String get_base() { return Character.toString(key).toUpperCase(); }
	public String get_type() { if (type != 'n') return Character.toString(type); else return ""; }
	public String get_maj_min() { return (major ? "maj" : "min"); }
	
	public String toString() {
		return key + (type != 'n' ? type + "" : "") + (major ? " maj" : " min");
	}
}