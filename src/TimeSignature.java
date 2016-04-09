public class TimeSignature {
	
	public enum Type {
		SIMPLE_DUPLE, SIMPLE_TRIPLE, SIMPLE_QUADRUPLE, COMPOUND_DUPLE, COMPOUND_TRIPLE, COMPOUND_QUADRUPLE, UNKNOWN
	}
	public Type type;
	public short hi;
	public short lo;
	
	public TimeSignature(short top, short bottom) {
		this.hi = top;
		this.lo = bottom;
		
		switch (top) {
			case 2: this.type = Type.SIMPLE_DUPLE; break;
			case 3: this.type = Type.SIMPLE_TRIPLE; break;
			case 4: this.type = Type.SIMPLE_QUADRUPLE; break;
			case 6: this.type = Type.COMPOUND_DUPLE;break;
			case 9: this.type = Type.COMPOUND_TRIPLE; break;
			case 12: this.type = Type.COMPOUND_QUADRUPLE; break;
			default: this.type = Type.UNKNOWN;
		}
	}
	
	public String toString() {
		return hi + " / " + lo;
	}
	
	public String getTS() {
		switch (type) {
			case SIMPLE_DUPLE: return "simple duple";
			case SIMPLE_TRIPLE: return "simple triple";
			case SIMPLE_QUADRUPLE: return "simple quadruple";
			case COMPOUND_DUPLE: return "compound duple";
			case COMPOUND_TRIPLE: return "compound triple";
			case COMPOUND_QUADRUPLE: return "compound quadruple";
			default: return "unknown";
		}
	}
	
	public void increment() {
		switch (type) {
			case SIMPLE_DUPLE: this.type = Type.SIMPLE_TRIPLE; break;
			case SIMPLE_TRIPLE: this.type = Type.SIMPLE_QUADRUPLE; break;
			case SIMPLE_QUADRUPLE: this.type = Type.COMPOUND_DUPLE; break;
			case COMPOUND_DUPLE: this.type = Type.COMPOUND_TRIPLE; break;
			case COMPOUND_TRIPLE: this.type = Type.COMPOUND_QUADRUPLE; break;
			case COMPOUND_QUADRUPLE: this.type = Type.SIMPLE_DUPLE; break;
			default: this.type = Type.UNKNOWN; break;
		}
		update_hi_lo();
	}
	
	public void decrement() {
		switch (type) {
			case SIMPLE_DUPLE: this.type = Type.COMPOUND_QUADRUPLE; break;
			case SIMPLE_TRIPLE: this.type = Type.SIMPLE_DUPLE; break;
			case SIMPLE_QUADRUPLE: this.type = Type.SIMPLE_TRIPLE; break;
			case COMPOUND_DUPLE: this.type = Type.SIMPLE_QUADRUPLE; break;
			case COMPOUND_TRIPLE: this.type = Type.COMPOUND_DUPLE; break;
			case COMPOUND_QUADRUPLE: this.type = Type.COMPOUND_TRIPLE; break;
			default: this.type = Type.UNKNOWN; break;
		}
		update_hi_lo();
	}
	
	private void update_hi_lo() {
		switch (type) {
			case SIMPLE_DUPLE:	hi = 2; lo = 4; break;
			case SIMPLE_TRIPLE: hi = 3; lo = 4; break;
			case SIMPLE_QUADRUPLE: hi = 4; lo = 4; break;
			case COMPOUND_DUPLE: hi = 6; lo = 8; break;
			case COMPOUND_TRIPLE: hi = 9; lo = 8; break;
			case COMPOUND_QUADRUPLE: hi = 12; lo = 8; break;
			default: hi = 0; lo = 0;
		}
	}
	
	public int num_beats() {
		if (this.type.name().contains("SIMPLE")) return hi;
		else if (this.type.name().contains("COMPOUND")) return hi/3;
		else return 1;
	}
}