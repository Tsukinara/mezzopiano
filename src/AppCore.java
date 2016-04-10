import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

public class AppCore {
	private final static short dA = 5, dL = 4, vel_thresh = 1;
	private final static float dT = 0.05f, dTm = 0.1f, dF = 0.5f;
	private final static double wtx_s = 150.0, wtx_e = 1770.0;
	private final static double wbx_s = 60.0, wbx_e = 1860.0;
	private final static double btx_s = 172.9, btx_e = 1698.8, btx_w = 18.66;
	private final static double bbx_s = 115.7, bbx_e = 1750.8, bbx_w = 20.0;
	private final static int wty = 780, wby = 919, wly = 959, bly = 868, anal_s = 358;
	private final static int r_al = 166;
	private final static Color anal_a = new Color(180, 218, 237);
	private final static Color anal_b = new Color(224, 236, 250);
	private final static Color pian_a = new Color(149, 198, 223);
	private final static Color pian_b = new Color(198, 226, 240);
	private final static Color line_a = new Color(26, 26, 26, 25);
	private final static Color hold_wr = new Color(103, 157, 190);
	private final static Color harm_wr = new Color(125, 187, 130);
	private final static Color hold_wrb = new Color(65, 121, 155);
	private final static Color harm_wrb = new Color(76, 146, 82);
	private final static Color hold_br = new Color(55, 96, 146);
	private final static Color harm_br = new Color(65, 136, 69);
	private final static Color pedal_u = new Color(122, 180, 208);
	private final static Color pedal_d = new Color(88, 141, 167);
	private final static Color ui_icon = new Color(39, 73, 94);
	private final static int w_lookup[] = { 1, 3, 4, 6, 8, 9, 11, 13, 15, 16, 18, 20, 21, 23, 25, 27, 28, 30, 32, 33, 35, 37, 39, 40, 42, 44, 45, 47, 49, 51, 52, 54, 56, 57, 59, 61, 63, 64, 66, 68, 69, 71, 73, 75, 76, 78, 80, 81, 83, 85, 87, 88 };
	private final static int b_lookup[] = { 2, 5, 7, 10, 12, 14, 17, 19, 22, 24, 26, 29, 31, 34, 36, 38, 41, 43, 46, 48, 50, 53, 55, 58, 60, 62, 65, 67, 70, 72, 74, 77, 79, 82, 84, 86 };
	public enum Mood { M_NEUTRAL, M_CHAOTIC, M_TRANQUIL, M_SAD, M_DRAMATIC, M_HAPPY };
	
	private Color p_color;
	private LinearGradientPaint l1, l2, l3;
	private ArrayList<Integer> start, end, key;
	private HashMap<Chord, Double> next_chords;
	private ArrayList<Color> histc;
	private short curr_state, alpha, alpha2, alpha3, alpha_m, bar_h1, bar_h2, bar_max, delay;
	private short num_beats, beat, bcount;
	private float theta, c_temp, t_temp, c_fan, t_fan;;
	private Display parent;
	private boolean flag_analysis;
	private NoteBuffer nb;
	private double[] w_tl, w_tr, w_bl, w_br;
	private double[] b_tl, b_tr, b_bl, b_br;
	private Harmonizer synth;
	private Font anal_base, mood_base, ambi_base;
	private int kkey;
	private Mood mood;
	private String mood_name, color_type, mood_icon, ctype_p, micon_p;
	boolean harm;
	
	public AppCore(Display parent) {
		this.parent = parent;
		this.synth = new Harmonizer(this, "resources\\synthesis.dat");
		init_values();
	}
	
	public void init_values() {
		this.kkey = -999;
		this.harm = false;
		this.num_beats = (short)parent.set.tsig.num_beats();
		this.beat = 0; this.bcount = 0;
		this.start = new ArrayList<Integer>();
		this.end = new ArrayList<Integer>();
		this.key = new ArrayList<Integer>();
		this.histc = new ArrayList<Color>();
		this.bar_h1 = 115; this.bar_h2 = 50; this.bar_max = 115;
		this.delay = 300/20;	this.theta = (float)Math.PI;
		this.curr_state = 0;
		this.flag_analysis = false;
		this.p_color = pedal_u;
		this.mood = Mood.M_NEUTRAL;
		this.mood_name = get_mood_name();
		this.w_tl = new double[52]; this.w_tr = new double[52];
		this.w_bl = new double[52]; this.w_br = new double[52];
		this.b_tl = new double[36]; this.b_tr = new double[36];	
		this.b_bl = new double[36]; this.b_br = new double[36];
		double w1 = (wtx_e - wtx_s) / 52.0, w2 = (wbx_e - wbx_s) / 52.0;
		double w3 = (btx_e - btx_s) / 49.0, w4 = (bbx_e - bbx_s) / 49.0;
		for (int i = 0; i < 52; i++) {
			w_tl[i] = (double)i*w1 + wtx_s;
			w_tr[i] = (double)(i+1)*w1 + wtx_s;
			w_bl[i] = (double)i*w2 + wbx_s;
			w_br[i] = (double)(i+1)*w2 + wbx_s;
		}
		int j = 0;
		for (int i = 0; i < 50; i++) {
			if (i%7 != 1 && i%7 != 4) {
				b_tl[j] = (double)i*w3 + btx_s;
				b_tr[j] = b_tl[j] + btx_w;
				b_bl[j] = (double)i*w4 + bbx_s;
				b_br[j] = b_bl[j] + bbx_w;
				j++;
			}
		}
		this.anal_base = new Font("Plantin MT Std", Font.PLAIN, sH(40));
		this.ambi_base = new Font("Plantin MT Std", Font.PLAIN, sH(45));
		this.mood_base = new Font("Plantin MT Std", Font.PLAIN, sH(54));
		this.alpha = 255;	this.alpha2 = 0; this.alpha3 = 255; this.alpha_m = 255;
		this.c_temp = 70; this.c_fan = 0;
		this.color_type = "clear"; this.mood_icon = "ICON_NA";
		this.ctype_p = "clear"; this.micon_p = "ICON_NA";
		
		Point2D start = new Point2D.Float(sX(0), sY(0));
		Point2D end = new Point2D.Float(sX(0), sY(1080));
		float[] pts1 = {0.41f, 0.89f}; Color[] colors = {pian_a, pian_b};
		this.l1 = new LinearGradientPaint(start, end, pts1, colors);
		Point2D start2 = new Point2D.Float(sX(0), sY(anal_s));
		Point2D end2 = new Point2D.Float(sX(0), sY(869));
		float[] pts2 = {0.137f, 0.776f};
		Color[] colors2 = {new Color(89, 141, 166), new Color(130, 169, 188)};
		this.l2 = new LinearGradientPaint(start2, end2, pts2, colors2);
	}
	
	public void render(Graphics2D g) {
		draw_primary(g);
		draw_history(g);
		draw_piano(g);
		draw_analysis(g);
		draw_overlay(g);
	}
	
	private void draw_overlay(Graphics2D g) {
		g.setComposite(AlphaComposite.SrcOver.derive(1f));
		if (curr_state == 2)
			if (bar_h1 > bar_h2) bar_h1 = (short)(bar_max - 2 - (int)((bar_max - bar_h2)/2 * (1+Math.cos(theta))));
			else bar_h1 = bar_h2;
		g.setColor(new Color(255, 255, 255, alpha));
		g.fillRect(sX(0), sY(0), sW(1920), sH(1080));
		g.setColor(parent.bg_color);
		g.fillRect(sX(0), sY(0), sW(1920), sH(bar_h1));
		g.fillRect(sX(0), sY(1080-bar_h2+1), sW(1920), sH(bar_h2));
		
		g.setComposite(AlphaComposite.SrcOver.derive(1f-(alpha/255f)));
		g.drawImage(parent.get_images().get("LOGO_WH"), sX(825), sY(33), sW(265), sH(62), null);
		
	}
	
	private void draw_primary(Graphics2D g) {
		g.setPaint(l1);
		g.fillRect(sX(0), sY(0), sW(1920), sH(1080));	
	}
	
	private void draw_analysis(Graphics2D g) {
		g.setColor(anal_a);
		g.fillRect(sX(0), sY(0), sW(1920), sH(anal_s));
		g.setColor(anal_b);
		g.fillRect(sX(600), sY(0), sW(720), sH(anal_s));
		
		Point2D start2 = new Point2D.Float(sX(0), sY(0));
		Point2D end2 = new Point2D.Float(sX(1920), sY(0));
		float[] pts2 = {0.0f, 0.12f, 0.88f, 1.0f}; 
		Color[] colors2 = {line_a, parent.bg_color, parent.bg_color, line_a};

		g.setPaint(new LinearGradientPaint(start2, end2, pts2, colors2));
		g.fillRect(sX(0), sY(anal_s-3), sW(1920), sH(6));
		
		Point2D start3 = new Point2D.Float(sX(0), sY(0));
		Point2D end3 = new Point2D.Float(sX(1920), sY(0));
		float[] pts3 = {0.0f, 0.15f, 0.85f, 1.0f};
		Color[] colors3 = {new Color(20, 42, 61, 0), new Color(20, 42, 61, 190), new Color(20, 42, 61, 190), new Color(20, 42, 61, 0)};
		g.setPaint(new LinearGradientPaint(start3, end3, pts3, colors3));
		g.setComposite(AlphaComposite.SrcOver.derive(alpha3/255f));
		g.fillRect(sX(0), sY(165), sW(1920), sH(115));
		g.setColor(Color.WHITE);
		g.setFont(new Font("Plantin MT Std", Font.PLAIN, sH(52)));
		int fw = g.getFontMetrics().stringWidth("awaiting music to begin analysis...");
		g.drawString("awaiting music to begin analysis...", (sX(1920)-fw)/2, sY(240));
				
		g.setColor(parent.bg_color);
		g.setComposite(AlphaComposite.SrcOver.derive(1f - alpha3/255f));
		g.setFont(anal_base);
		FontMetrics fm = g.getFontMetrics();
		fw = fm.stringWidth("key:");
		g.drawString("key:", sX(r_al) - fw, sY(194));
		fw = fm.stringWidth("tempo:");
		g.drawString("tempo:", sX(r_al) - fw, sY(251.3));
		fw = fm.stringWidth("meter:");
		g.drawString("meter:", sX(r_al) - fw, sY(308.3));
		
		g.setColor(new Color(20, 42, 61, 190));
		int w = 720/num_beats; int h = 10;
		g.fillRect(sX(beat*w + 600), sY(anal_s-h), sW(w), sH(h));
		
		g.setColor(parent.bg_color);
		int nx = r_al + 40;
		if (parent.set.ksig != null) draw_ksig(g, parent.set.ksig);
		else if (nb.curr_key != null) draw_ksig(g, nb.curr_key);
		else g.drawString("collecting data...", sX(nx), sY(194));
		
		if (parent.set.tempo != -1) g.drawString(parent.set.tempo + " bpm", sX(nx), sY(251.3));
		else g.drawString(nb.curr_tempo + " bpm", sX(nx), sY(251.3));
		
		if (parent.set.tsig != null) g.drawString(parent.set.tsig.getTS(), sX(nx), sY(308.3));
		
		g.drawImage(parent.get_images().get("ICON_TM"), sX(672), sY(148), sW(65), sH(65), null);
		g.drawImage(parent.get_images().get("ICON_LT"), sX(837), sY(148), sW(65), sH(65), null);
		g.drawImage(parent.get_images().get("ICON_FN"), sX(1002), sY(148), sW(65), sH(65), null);
		g.drawImage(parent.get_images().get("ICON_NS"), sX(1168), sY(148), sW(65), sH(65), null);
		draw_ambiance(g);
		
		g.setColor(new Color(129, 164, 207));
		g.fillRect(sX(1320), sY(140), sW(600), sH(66));
		
		g.setColor(parent.bg_color);
		g.setFont(anal_base);
		fw = g.getFontMetrics().stringWidth("best guess for mood:");
		g.drawString("best guess for mood:", sX(1320) + (sX(600)-fw)/2, sY(186));
		
		g.setFont(mood_base);
		fw = g.getFontMetrics().stringWidth(mood_name);
		g.drawString(mood_name, sX(1320) + (sX(600)-fw)/2, sY(273));
		
		//		g.setFont(new Font("Plantin MT Std", Font.PLAIN, 18));
//		g.drawString("DOM:" + Music.getNoteName(nb.dom()), sX(1320), sY(220));
//		g.drawString(nb.bass.toString(), sX(1320), sY(255));
//		g.drawString(nb.rel_buffer.toString(), sX(1320), sY(290));
//		g.drawString(nb.chord_history.subList(nb.chord_history.size() - 10 < 0? 0: nb.chord_history.size() - 10, nb.chord_history.size()).toString(), sX(1320), sY(325));
	}
	
	private void draw_ksig(Graphics2D g, KeySignature k) {
		int nx = r_al + 40;
		if (k.get_type().equals("")) 
			g.drawString(k.get_base() + " " + k.get_maj_min(), sX(nx), sY(194));
		else {
			g.drawString(k.get_base(), sX(nx), sY(194));
			int tmp = nx + g.getFontMetrics().stringWidth(k.get_base()) + sW(14);
			g.setFont(new Font("Opus Text Std", Font.PLAIN, sH(45)));
			g.drawString(k.get_type(), sX(tmp), sY(194));
			tmp += g.getFontMetrics().stringWidth(k.get_type()) + sW(10);
			g.setFont(anal_base);
			g.drawString(" " + k.get_maj_min(), sX(tmp), sY(194));
		}
	}
	
	private void draw_ambiance(Graphics2D g) {
		g.setFont(ambi_base);
		int fw = g.getFontMetrics().stringWidth((int)c_temp + "\u00b0");
		g.drawString((int)c_temp + "\u00b0", sX(711)-fw/2, sY(304));
		fw = g.getFontMetrics().stringWidth((int)c_fan + "%");
		g.drawString((int)c_fan + "%", sX(1043)-fw/2, sY(304));
		if (alpha3 == 0) {
			g.setComposite(AlphaComposite.SrcOver.derive(1f - alpha_m/255f));
			fw = g.getFontMetrics().stringWidth(ctype_p);
			g.drawString(ctype_p, sX(877)-fw/2, sY(304));
			g.drawImage(parent.get_images().get(micon_p), sX(1176), sY(253), sH(65), sW(65), null);
			g.setComposite(AlphaComposite.SrcOver.derive(alpha_m/255f));
			fw = g.getFontMetrics().stringWidth(color_type);
			g.drawString(color_type, sX(877)-fw/2, sY(304));
			g.drawImage(parent.get_images().get(mood_icon), sX(1176), sY(253), sH(65), sW(65), null);
		}
	}
	
	private void draw_history(Graphics2D g) {
		g.setPaint(l2);
		g.fillRect(sX(104), sY(anal_s), sW(1712), sH(950-anal_s));
		for (int i = 0; i < key.size(); i++) {
			int k = key.get(i);
			if (has(w_lookup, k)) {
				int wi = i_of(w_lookup, k);
				double[] x = { w_tl[wi], w_tr[wi], w_tr[wi], w_tl[wi] };	
				double[] y = { end.get(i), end.get(i), start.get(i), start.get(i)};
				g.setColor(histc.get(i));
				g.fillPolygon(sX(x), sY(y), 4);
			}
		} for (int i = 0; i < key.size(); i++) {
			int k = key.get(i);
			if (has(b_lookup, k)) {
				int bi = i_of(b_lookup, k);
				double[] x = { b_tl[bi], b_tr[bi], b_tr[bi], b_tl[bi] };
				double[] y = { end.get(i), end.get(i), start.get(i), start.get(i) };
				g.setColor(histc.get(i));
				g.fillPolygon(sX(x), sY(y), 4);
			}
		}
	}
	
	private void draw_piano(Graphics2D g) {
		g.setColor(Color.WHITE);
		for (int i = 0; i < 52; i++) {
			double[] x = { w_tl[i], w_tr[i], w_br[i], w_bl[i] };	double[] y = { wty, wty, wby, wby };
			double[] u = { w_bl[i], w_br[i], w_br[i], w_bl[i] };	double[] v = { wby, wby, wly, wly };
			
			if (nb.is_held(w_index(i))) g.setColor(hold_wr);
			else if (synth.is_held(w_index(i), kkey)) g.setColor(harm_wr);
			else g.setColor(Color.WHITE);
			g.fillPolygon(sX(x), sY(y), 4);
			
			if (nb.is_held(w_index(i))) g.setColor(hold_wrb);
			else if (synth.is_held(w_index(i), kkey)) g.setColor(harm_wrb);
			else g.setColor(new Color(230, 230, 230));
			g.fillPolygon(sX(u), sY(v), 4);
			
			g.setColor(parent.bg_color);
			g.setStroke(new BasicStroke(sF(1.5f), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
			g.drawPolygon(sX(x), sY(y), 4);
			g.drawPolygon(sX(u), sY(v), 4);
		}
		
		for (int i = 0; i < 36; i++) {
			double[] x = { b_tl[i], b_tr[i], b_br[i], b_bl[i] };
			double[] y = { wty, wty, bly, bly };
			if (nb.is_held(b_index(i))) g.setColor(hold_br);
			else if (synth.is_held(b_index(i), kkey)) g.setColor(harm_br);
			else g.setColor(parent.bg_color);
			g.fillPolygon(sX(x), sY(y), 4);
			
			g.setColor(parent.bg_color);
			g.setStroke(new BasicStroke(sF(1.5f), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
			g.drawPolygon(sX(x), sY(y), 4);
		}
		
		g.setColor(p_color);
		g.fillRoundRect(sX(60), sY(974), sW(1800), sH(28), sH(5), sH(5));
	}
	
	private int b_index(int bi) { return b_lookup[bi]; }
	
	private int w_index(int wi) { return w_lookup[wi]; }
	
	private Color curr_color() { if (nb == null) return pedal_u; return (nb.damped ? pedal_d : pedal_u); }
		
	private void pedal_color() {
		int nR, nG, nB, ms = 3, ds = 3;
		if (!p_color.equals(curr_color())) {
			int dR = (curr_color().getRed()-p_color.getRed())/ds;
			if (dR < 0) {
				if (dR > -ms) dR = -ms;
				if (p_color.getRed() + dR < curr_color().getRed()) nR = curr_color().getRed(); else nR = p_color.getRed()+dR;
			} else {
				if (dR < ms) dR = ms;
				if (p_color.getRed() + dR > curr_color().getRed()) nR = curr_color().getRed(); else nR = p_color.getRed()+dR;
			}
			int dG = (curr_color().getGreen()-p_color.getGreen())/ds;
			if (dG < 0) {
				if (dG > -ms) dG = -ms;
				if (p_color.getGreen() + dG < curr_color().getGreen()) nG = curr_color().getGreen(); else nG = p_color.getGreen()+dG;
			} else {
				if (dG < ms) dG = ms;
				if (p_color.getGreen() + dG > curr_color().getGreen()) nG = curr_color().getGreen(); else nG = p_color.getGreen()+dG;
			}
			int dB = (curr_color().getBlue()-p_color.getBlue())/ds;
			if (dB < 0) {
				if (dB > -ms) dB = -ms;
				if (p_color.getBlue() + dB < curr_color().getBlue()) nB = curr_color().getBlue(); else nB = p_color.getBlue()+dB;
			} else {
				if (dB < ms) dB = ms;
				if (p_color.getBlue() + dB > curr_color().getBlue()) nB = curr_color().getBlue(); else nB = p_color.getBlue()+dB;
			}
			p_color = new Color(nR, nG, nB);
		}
	}
	
	public void handle(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE: case KeyEvent.VK_X: if (curr_state == 1) curr_state = 2; break;
			case KeyEvent.VK_R: nb.reinit(); init_values(); break;
			case KeyEvent.VK_SPACE: parent.reset(); break;
			case KeyEvent.VK_A: set_mood(Mood.M_CHAOTIC); break;
			case KeyEvent.VK_S: set_mood(Mood.M_DRAMATIC); break;
			case KeyEvent.VK_D: set_mood(Mood.M_HAPPY); break;
			case KeyEvent.VK_F: set_mood(Mood.M_SAD); break;
			case KeyEvent.VK_G: set_mood(Mood.M_TRANQUIL);  break;
			case KeyEvent.VK_H: set_mood(Mood.M_NEUTRAL); break;
		}
	}
	
	private void history_step() {
		for (int i = 0; i < start.size(); i++) {
			if (!(nb.is_held(key.get(i)) && start.get(i) == wty))
				if (!(synth.is_held(key.get(i), kkey) && start.get(i) == wty))
					start.set(i, start.get(i) - dL);
		}
		for (int i = 0; i < end.size(); i++) end.set(i, end.get(i) - dL);
		for (int i = 0; i < start.size(); i++) {
			if (start.get(i) < 0) {
				start.remove(i);	end.remove(i);
				histc.remove(i);	key.remove(i);
			}
		}
	}
	
	private void adjust_tempo() {
		bcount += 1;
		int bpm = parent.set.tempo;
		if (bpm == -1) bpm = nb.curr_tempo;
		double spb = 60.0/(double)bpm;
		double dpb = spb*1000/20;
		if (bcount > dpb) {	bcount = 0;	beat = (short)((beat+1)%num_beats); }
	}
	
	private void mellifluity() {
		if (nb.curr_chord != null && nb.curr_key != null) {
//			Set<Chord> tmp = parent.profile().next_chords(nb.curr_chord, 1).keySet();
//			Chord next = new Chord("1-100M");	for (Chord c : tmp) next = c;
			synth.match_melody_to(nb.curr_chord, new int[0]);
			int bpm = (parent.set.tempo == -1 ? nb.curr_tempo : parent.set.tempo);
			double dpb = (60.0/(double)bpm)*1000/20;
			double time = (beat+(double)bcount/dpb) * 12;
			if (harm) synth.play_melody(time, kkey);
		}
	}
	
	public void chord_changed() {
		if (bcount < 8 && (beat == 2 || beat == 0)) bcount = 0;
		else { beat = 0; bcount = 0; }
		next_chords = parent.profile().next_chords(nb.curr_chord, 4);
	}
	
	public void step() {
		adjust_tempo();
		pedal_color();
		kkey = (nb.curr_key == null? -999 : Music.getKey(nb.curr_key.key + "" + nb.curr_key.type));
		if (Analyzer.get_mood() != mood) { set_mood(Analyzer.get_mood()); }
		switch (curr_state) {
			case 0: // transition in;
				alpha2 = (short)(alpha2-dA < 0 ? 0 : alpha2-dA);
				if (delay <= 0) alpha = (short)(alpha-dA < 0 ? 0 : alpha-dA);
				else delay--;
				if (alpha == 0) curr_state = 1;
				break;
			case 1: // idle
				if (flag_analysis) alpha3 = (short)(alpha3-dA < 0 ? 0 : alpha3-dA);
				if (alpha_m < 255) alpha_m = (short)(alpha_m+dA > 255? 255: alpha_m+dA);
				if (c_temp < t_temp) c_temp = (c_temp + dTm > t_temp ? t_temp : c_temp + dTm);
				if (c_temp > t_temp) c_temp = (c_temp - dTm < t_temp ? t_temp : c_temp - dTm);
				if (c_fan < t_fan) c_fan = (c_fan + dF > t_fan ? t_fan : c_fan + dF);
				if (c_fan > t_fan) c_fan = (c_fan - dF < t_fan ? t_fan : c_fan - dF);
				mellifluity(); 	history_step();	
				break;
			case 2: // transition out
				mellifluity(); 	history_step();
				alpha = (short)(alpha+dA > 255 ? 255: alpha+dA);
				alpha2 = (short)(alpha2+dA > 255 ? 255: alpha2+dA);
				theta += dT;
				if (theta > 2*Math.PI) theta = (float)(-2*Math.PI);
				if (alpha == 255) { 
					parent.s_mn.re_init(0, 3); 
					parent.set_state(Display.State.MENU); 
					parent.play_bgm("menu_bgm.mp3"); 
					parent.profile().write_profile();
				} 
				break;
		}
	}
	
	private void set_mood(Mood m) {
		this.mood = m;
		this.ctype_p = color_type;
		this.micon_p = mood_icon;
		this.alpha_m = 0;
		switch (this.mood) {
		case M_NEUTRAL:
			this.t_temp = 70; this.t_fan = 10;
			this.mood_icon = "ICON_NA";
			this.color_type = "clear";
			break;
		case M_CHAOTIC:
			this.t_temp = (int)(Math.random()*10.0+74);
			this.t_fan = (int)(Math.random()*101);
			int tmp = (int)(Math.random()*4);
			this.mood_icon = (tmp == 0?"ICON_RN":(tmp == 1?"ICON_WD":(tmp == 2?"ICON_TD":"ICON_BD")));
			this.color_type = "rand";
			break;
		case M_HAPPY:
			this.t_temp = 74; this.t_fan = 85;
			this.mood_icon = "ICON_BD";
			this.color_type = "warm";
			break;
		case M_SAD:
			this.t_temp = 65; this.t_fan = 5;
			this.mood_icon = "ICON_RN";
			this.color_type = "cool";
			break;
		case M_TRANQUIL:
			this.t_temp = 68; this.t_fan = 30;
			this.color_type = "gentle";
			this.mood_icon = "ICON_WD";
			break;
		case M_DRAMATIC:
			this.t_temp = 71; this.t_fan = 100;
			this.mood_icon = "ICON_TD";
			this.color_type = "intense";
			break;
		}
		this.mood_name = get_mood_name();
	}
	
	public synchronized void note_pressed(byte id, byte vel, long timestamp) {
		if (vel > vel_thresh && curr_state > 0) {
			flag_analysis = true;
			start.add(wty); end.add(wty); key.add((int)id - 20);
			if (has(w_lookup, (int)id - 20)) histc.add(new Color(68, 104, 148));
			else histc.add(new Color(34, 70, 113));
		}
	}
	
	public synchronized void h_note_pressed(byte id, byte vel) {
		if (vel > vel_thresh && curr_state > 0) {
			start.add(wty); end.add(wty); key.add((int)id - 20);
			if (has(w_lookup, (int)id - 20)) histc.add(Color.LIGHT_GRAY);
			else histc.add(Color.DARK_GRAY);
		}
	}
	
	private boolean has(int[] arr, int val) { for (int v : arr) if (v == val) return true; return false; }
	private int i_of (int[] arr, int val) { 
		for (int i = 0; i < arr.length; i++) 
			if (arr[i] == val) return i; 
		return -1; 
	}
	
	private String get_mood_name() {
		String mname = "";
		switch (mood) {
		case M_SAD: return "melancholic";
		case M_HAPPY: return "jubliant";
		case M_TRANQUIL: return "tranquil";
		case M_CHAOTIC: return "chaotic";
		case M_DRAMATIC: return "dramatic";
		default: return "neutral";
		}
	}
	
	public void note_released(byte id, long timestamp) {}
	public void damp_pressed(long timestamp) {}
	public void damp_released(long timestamp) {}
	
	public void re_init() { init_values(); }
	public void set_buffer(NoteBuffer nb) { this.nb = nb; }
	
	private int sX (double x) { return parent.scaleX(x); }
	private int sY (double y) { return parent.scaleY(y); }
	private int sW (double w) { return parent.scaleW(w); }
	private int sH (double h) { return parent.scaleH(h); }
	private float sF (float f) { return parent.scaleF(f); }
	private int[] sX (double[] x) { return parent.scaleX(x); }
	private int[] sY (double[] y) { return parent.scaleY(y); }
}