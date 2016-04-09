import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

public class SettingsMenu {
	private static final short dA = 5, dL1 = 15;
	private static final float dT = .05f;
	private static final int origX = 960;
	private static final int finXL = 760, finYB = 849, finYT = 553;
	private static final int setXL = 440, setYB = 944, setYT = 136;
	private static final int setTT = 185, sel_h = 70;
	private static final int box_y = 571, box_w = 404, box_h = 253;
	private static final int l_sp = 80, r_al = 696, max_opt = 7;
	private static final int v_h = 34, v_w = 360, opt_h = 64;
	private static final Color main = new Color(26, 47, 72);
	private static final Color side = new Color(152, 182, 201);
	private static final Color shim = new Color(106, 143, 165);
	private static final String[] tits = {"display", "notation" ,"bgm vol", "accomp. vol", "tempo", "key", "meter"};
	private static Display.State next_state = Display.State.MENU;
	
	private Display parent;
	private short curr_state, curr_opt, bar_height, alpha, alpha2;
	private float theta;
	private int sel_y, bar_yc, bar_yc2, bar_xc, tit_y;
	private Font pl_base, opt_base;
	private Settings s;
	
	public SettingsMenu(Display parent) {
		this.parent = parent;
		init_values();
	}
	
	private void init_values() {
		this.curr_state = 0; this.curr_opt = 0;
		this.sel_y = get_opt_y();
		this.bar_height = 50; this.bar_yc = 250;
		this.pl_base = new Font("Plantin MT Std", Font.PLAIN, sH(48));
		this.opt_base = new Font("Plantin MT Std", Font.PLAIN, sH(38));
		this.tit_y = setTT; this.alpha = 255; this.alpha2 = 0;
		this.theta = (float)Math.PI;
		this.s = parent.set.clone();
	}
	
	public void render(Graphics2D g) {
		draw_primary(g);
		switch (curr_state) {
			case 0: transition_in(g); break;
			case 1: set_select(g); break;
			case 2: transition_out(g); break;
		}
	}
	
	private void draw_primary (Graphics2D g) {
		g.drawImage(parent.get_images().get("MENU_BG"), sX(0), sY(0), sW(1920), sH(1080), null);
		g.setColor(Color.WHITE);
		for (Snow s : parent.snow) g.fillOval(sX(s.x), sY(s.y), sW(s.dia), sH(s.dia));
		
		g.setColor(parent.bg_color);
		g.fillRect(sX(0), sY(0), sW(1920), sH(this.bar_height));
		g.fillRect(sX(0), sY(1080-bar_height+1), sW(1920), sH(bar_height));	
	}
	
	private void draw_menu (Graphics2D g) {
		int menu_h = (setYB - setYT) - (finYB - finYT - box_h);
		int menu_y = setYT + (setYB - setYT - menu_h)/2;
			
		g.setColor(new Color(222, 238, 246, 200));
		g.fillRect(sX(setXL), sY(menu_y), sW(2*(origX-setXL)), sH(menu_h));
		
		g.setColor(Color.WHITE);
		g.fillRect(sX(setXL), sY(tit_y), sW(2*(origX-setXL)), sH(sel_h));
		
		int alpha = (bar_yc < sel_y ? 0 : sH(bar_yc - sel_y));
		if (alpha > 255) alpha = 255;
		g.setColor(new Color(152, 189, 209, alpha));
		g.fillRect(sX(setXL), sY(sel_y), sW(1920-(2*setXL)), sH(opt_h));
		
		g.setColor(parent.bg_color);
		g.setStroke(new BasicStroke(sF(8), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.drawLine(sX(setXL), sY(setYB), sX(setXL), sY(setYT));
		g.drawLine(sX(1920-setXL), sY(setYB), sX(1920-setXL), sY(setYT));
		
		g.setFont(pl_base);
		int fw = g.getFontMetrics().stringWidth("settings");
		g.drawString("settings", (sX(1920)-fw)/2, sY(tit_y + 50));
	}
	
	private void transition_in(Graphics2D g) {
		draw_menu(g);
		int alpha;
		for (int i = 0; i < tits.length; i++) {
			if (get_opt_y(i) > bar_yc) alpha = 0;
			else alpha = (sH(bar_yc - get_opt_y(i)) > 255 ? 255 : sH(bar_yc - get_opt_y(i)));
			draw_line(g, i, alpha);
		}
		g.setFont(pl_base);
		g.setColor(parent.bg_color);
		int fw = g.getFontMetrics().stringWidth("save settings");
		g.drawString("save settings", (sX(1920)-fw)/2, sY(873));
	}

	private void set_select(Graphics2D g) { transition_in(g); }
	
	private void transition_out(Graphics2D g) {
		// calculate differences
		if (bar_yc > finYB) bar_yc = setYB - 2 - (int)((setYB-finYB)/2 * (1+Math.cos(theta)));
		else bar_yc = finYB;
		if (bar_yc2 < finYT) bar_yc2 = setYT + 2 + (int)((finYT-setYT)/2 * (1+Math.cos(theta)));
		else bar_yc2 = finYT;
		if (bar_xc < finXL) bar_xc = setXL + 2 + (int)((finXL-setXL)/2 * (1+Math.cos(theta)));
		else bar_xc = finXL;
		if (tit_y < parent.s_mn.get_opt_y(1)) tit_y = setTT + 2 + (int)((parent.s_mn.get_opt_y(1) - setTT)/2 * (1+Math.cos(theta)));
		else tit_y = parent.s_mn.get_opt_y(1);
		int menu_h = (bar_yc - bar_yc2) - (finYB - finYT - box_h);
		int menu_y = bar_yc2 + (bar_yc - bar_yc2 - menu_h)/2;
		g.setColor(new Color(222, 238, 246, 200));
		
		// draw background box
		if (Math.abs(menu_y - box_y) < 8) { g.fillRect(sX(finXL), sY(box_y), sW(box_w), sH(box_h)); }
		else g.fillRect(sX(bar_xc), sY(menu_y), sW(2*(origX-bar_xc)), sH(menu_h));
		
		// draw title box
		g.setColor(Color.WHITE);
		g.fillRect(sX(bar_xc), sY(tit_y), sW(2*(origX-bar_xc)), sH(sel_h));
		
		// draw menu borders
		g.setColor(parent.bg_color);
		g.setStroke(new BasicStroke(sF(8), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.drawLine(sX(bar_xc), sY(bar_yc), sX(bar_xc), sY(bar_yc2));
		g.drawLine(sX(1920-bar_xc), sY(bar_yc), sX(1920-bar_xc), sY(bar_yc2));
		
		// draw title
		g.setFont(pl_base);
		FontMetrics fm = g.getFontMetrics();
		g.setComposite(AlphaComposite.SrcOver.derive(1f));
		int fw =fm.stringWidth("settings");
		g.drawString("settings", (sX(1920)-fw)/2, sY(tit_y + 50));
		
		// fade out settings
		for (int i = 0; i < tits.length; i++) draw_line(g, i, alpha);
		g.setFont(pl_base);
		g.setColor(parent.bg_color);
		fw = g.getFontMetrics().stringWidth("save settings");
		g.drawString("save settings", (sX(1920)-fw)/2, sY(873));
		
		// fade in menu options
		g.setComposite(AlphaComposite.SrcOver.derive((float)(alpha2/255f)));
		fw = fm.stringWidth("start");
		g.drawString("start", (sX(1920)-fw)/2, sY(641));
		fw = fm.stringWidth("exit");
		g.drawString("exit", (sX(1920)-fw)/2, sY(787));
		g.drawImage(parent.get_images().get("LOGO_BK"), sX(571), sY(200), sW(777), sH(258), null);
	}
	
	private int get_opt_y() {
		if (curr_opt < max_opt) return l_sp*curr_opt + 266;
		else return 828;
	}
	
	private int get_opt_y(int num) {
		if (num < max_opt) return l_sp*num + 266;
		else return 828;
	}
	
	private void draw_line(Graphics2D g, int num, int alpha) {
		boolean curr = num == curr_opt;
		int y = l_sp*num + 311;
		g.setFont(opt_base);
		FontMetrics fm = g.getFontMetrics();
		int fw = fm.stringWidth(tits[num]);
		g.setColor(new Color(26, 26, 26, alpha));
		g.setComposite(AlphaComposite.SrcOver.derive(alpha / 255f));
		g.drawString(tits[num], sX(r_al) - fw, sY(y));
		switch (num) {
			case 0: 
				g.setColor(s.window_size < 0? main: (curr ? shim : side));
				g.drawString("fullscreen", sX(782), sY(y));
				g.setColor(s.window_size == 1600? main: (curr ? shim : side));
				g.drawString("1600x900", sX(999), sY(y));
				g.setColor(s.window_size == 1280? main: (curr ? shim : side));
				g.drawString("1280x720", sX(1207), sY(y));
				break;
			case 1:
				g.setColor(s.chord_type == Settings.ChordType.ROMAN? main: (curr ? shim : side));
				g.drawString("roman", sX(782), sY(y));
				g.setColor(s.chord_type != Settings.ChordType.ROMAN? main: (curr ? shim : side));
				g.drawString("symbol", sX(999), sY(y));
				break;
			case 2:
				g.setColor(s.bgm_vol == 0? main: (curr ? shim : side));
				g.drawString("off", sX(782), sY(y));
				g.setColor(curr ? shim : side);
				g.fillRect(sX(884), sY(get_opt_y(num) + (opt_h - v_h)/2), sW((int)((double)s.bgm_vol/100.0 * v_w)), sH(v_h));
				g.setColor(s.bgm_vol != 0? main: (curr ? shim : side));
				g.drawString(s.bgm_vol + "%", sX(1278), sY(y));
				break;
			case 3:
				g.setColor(s.harm_vol == 0? main: (curr ? shim : side));
				g.drawString("off", sX(782), sY(y));
				g.setColor(curr ? shim : side);
				g.fillRect(sX(884), sY(get_opt_y(num) + (opt_h - v_h)/2), sW((int)((double)s.harm_vol/100.0 * v_w)), sH(v_h));
				g.setColor(s.harm_vol != 0? main: (curr ? shim : side));
				g.drawString(s.harm_vol + "%", sX(1278), sY(y));
				break;
			case 4:
				g.setColor(s.tempo == -1? main: (curr ? shim : side));
				g.drawString("auto", sX(782), sY(y));
				g.setColor(curr ? shim : side);
				int temp_o;
				if (s.tempo == -1) temp_o = 40; else temp_o = s.tempo; 
				g.fillRect(sX(884), sY(get_opt_y(num) + (opt_h - v_h)/2), sW((int)((double)(temp_o - 40)/200.0 * v_w)), sH(v_h));
				g.setColor(s.tempo > 39? main: (curr ? shim : side));
				g.drawString(temp_o + "bpm", sX(1278), sY(y));
				break;
			case 5:
				g.setColor(s.ksig == null? main: (curr ? shim : side));
				g.drawString("auto", sX(782), sY(y));
				g.setColor(s.ksig != null? main: (curr ? shim : side));
				if (s.ksig != null) {
					if (s.ksig.get_type().equals("")) 
						g.drawString(s.ksig.get_base() + " " + s.ksig.get_maj_min(), sX(928), sY(y));
					else {
						g.drawString(s.ksig.get_base(), sX(928), sY(y));
						int tmp = 928 + fm.stringWidth(s.ksig.get_base()) + sW(14);
						g.setFont(new Font("Opus Text Std", Font.PLAIN, sH(40)));
						g.drawString(s.ksig.get_type(), sX(tmp), sY(y));
						tmp += g.getFontMetrics().stringWidth(s.ksig.get_type()) + sW(10);
						g.setFont(opt_base);
						g.drawString(" " + s.ksig.get_maj_min(), sX(tmp), sY(y));
					}
				}
				if (curr) {
					g.setColor(shim);
					g.setFont(new Font("Plantin MT Std", Font.PLAIN, sH(28)));
					g.drawString("(play any triad to set)", sX(1082), sY(y));
				}
				break;
			case 6:
				g.setColor(s.tsig == null? main: (curr ? shim : side));
				g.drawString("auto", sX(782), sY(y));
				g.setColor(s.tsig != null? main: (curr ? shim : side));
				if (s.tsig != null) g.drawString(s.tsig.getTS(), sX(928), sY(y));
				else g.drawString("simple duple", sX(928), sY(y));
				break;
		}
	}
	
	public void handle(KeyEvent e) {
		if (curr_state < 2) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE: 
					curr_state = 2;
					bar_xc = setXL; bar_yc = setYB; bar_yc2 = setYT;
					break;
				case KeyEvent.VK_X: curr_opt = max_opt; break;
				case KeyEvent.VK_UP: curr_opt = (short)(curr_opt - 1 < 0 ? 0 : curr_opt - 1); break;
				case KeyEvent.VK_DOWN: curr_opt = (short)(curr_opt + 1 > max_opt? max_opt: curr_opt + 1); break;
				case KeyEvent.VK_R: init_values(); break;
				case KeyEvent.VK_ENTER: case KeyEvent.VK_Z:
					if (curr_opt == max_opt) { 
						curr_state = 2; 
						bar_xc = setXL; bar_yc = setYB; bar_yc2 = setYT;
						next_state = Display.State.MENU;
						s.copy_to(parent.set);
						parent.set.write_settings(Display.settings);
					}
					break;
				case KeyEvent.VK_LEFT:
					switch (curr_opt) {
						case 0: 
							if (s.window_size == 1280) s.window_size = 1600;
							else s.window_size = -1; break;
						case 1:	s.chord_type = Settings.ChordType.ROMAN; break;
						case 2:	s.bgm_vol = (short)(s.bgm_vol - 5 < 0? 0: s.bgm_vol - 5); break;
						case 3:	s.harm_vol = (short)(s.harm_vol - 5 < 0? 0: s.harm_vol - 5); break;
						case 4:
							if (s.tempo <= 40) s.tempo = -1;
							else s.tempo = (short)(s.tempo - 5 < 40? 40: s.tempo - 5);
							break;
						case 5:	if (s.ksig != null) s.ksig = null; break;
						case 6:
							if (s.tsig != null) {
								if (s.tsig.type == TimeSignature.Type.SIMPLE_DUPLE) s.tsig = null;
								else s.tsig.decrement();
							}
							break;
					}
					break;
				case KeyEvent.VK_RIGHT:
					switch (curr_opt) {
						case 0: 
							if (s.window_size == -1) s.window_size = 1600;
							else s.window_size = 1280; break;
						case 1:	s.chord_type = Settings.ChordType.SYMBOL; break;
						case 2:	s.bgm_vol = (short)(s.bgm_vol + 5 > 100? 100: s.bgm_vol + 5); break;
						case 3:	s.harm_vol = (short)(s.harm_vol + 5 > 100? 100: s.harm_vol + 5); break;
						case 4:
							if (s.tempo == -1) s.tempo = 40;
							else s.tempo = (short)(s.tempo + 5 > 240? 240: s.tempo + 5);
							break;
						case 5:	if (s.ksig == null) s.ksig = new KeySignature("C", true); break;
						case 6:
							if (s.tsig == null) s.tsig = new TimeSignature((short)2, (short)4);
							else if (s.tsig.type != TimeSignature.Type.COMPOUND_QUADRUPLE) s.tsig.increment();
							break;					
				}
			}
		}
	}
	
	public void re_init() {
		init_values();
	}
	
	public void step() {
		for (Snow s : parent.snow) s.step();
		if (sel_y < get_opt_y()) {
			int dS = sH(get_opt_y() - sel_y)/sH(5);
			sel_y += (dS > sH(4) ? dS : sH(4));
			if (sel_y > get_opt_y()) sel_y = get_opt_y();
		} else if (sel_y > get_opt_y()) {
			int dS = sH(get_opt_y() - sel_y)/sH(5);
			sel_y += (dS < sH(-4) ? dS : sH(-4));
			if (sel_y < get_opt_y()) sel_y = get_opt_y();
		}
		switch (curr_state) {
		case 0:
			bar_yc += dL1;
			if (bar_yc > 900) curr_state = 1;
			break;
		case 1: // idle
			if (bar_yc < 1280) bar_yc += dL1;
			break;
		case 2: // transition out
			if (alpha > 0) alpha -= dA;
			if (alpha < 0) alpha = 0;
			if (alpha < 2) alpha2 += dA;
			if (alpha2 > 255) alpha2 = 255;
			theta += dT;
			if (theta > 2*Math.PI) theta = (float)(-2*Math.PI);
			if (alpha2 == 255) {
				parent.s_mn.re_init(1, 1);
				parent.set_state(next_state);
			}
		}
	}
	
	public void note_pressed(byte id, byte vel, long timestamp) {
		curr_opt = 5;
		String c = Analyzer.get_chord_context_free(parent.buffer().hold_buffer, 3);
		if (!c.equals("unknown") && !c.contains("dim") && !c.contains("7")) {
			if (s.ksig == null) s.ksig = new KeySignature("C", false);
			s.ksig.key = c.charAt(0);
			s.ksig.type = (c.charAt(1) == '#' || c.charAt(1) == 'b') ? c.charAt(1) : 'n';
			s.ksig.major = c.contains("maj");
		}
	}
	
	public void note_released(byte id, long timestamp) {
		curr_opt = 5;
		String c = Analyzer.get_chord_context_free(parent.buffer().hold_buffer, 3);
		if (!c.equals("unknown") && !c.contains("dim") && !c.contains("7")) {
			s.ksig.key = c.charAt(0);
			s.ksig.type = (c.charAt(1) == '#' || c.charAt(1) == 'b') ? c.charAt(1) : 'n';
			s.ksig.major = c.contains("maj");
		}
	}
	public void damp_pressed(long timestamp) {}
	public void damp_released(long timestamp) {}
	
	private int sX (int x) { return parent.scaleX(x); }
	private int sY (int y) { return parent.scaleY(y); }
	private int sW (int w) { return parent.scaleW(w); }
	private int sH (int h) { return parent.scaleH(h); }
	private float sF (float f) { return parent.scaleF(f); }
}