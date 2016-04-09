import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

public class ProfileSelect {
	private static final short dA = 10, dL1 = 15, dA2 = 5, fin_barh = 115;
	private static final float dT = 0.05f;
	private static final int max_len = 445;
	private static final int proXL = 694, proYB = 944, proYT = 136;
	private static final int origX = 960;
	private static final int finXL = 760, finYB = 849, finYT = 553;
	private static final int box_y = 571, box_w = 404, box_h = 253;
	private static final int new_w = 750, new_h = 200, new_x = 581;
	private static final int sel_h = 70, nti_y = 365, new_y = 341;
	private static final int proTT = 185;
	private static final int l_sp = 60, max_opt = 9;
	private static final int opt_h = 54;
	private static final Color main = new Color(26, 47, 72);
	private static final Color shim = new Color(106, 143, 165);
	
	private Graphics2D tmp;
	private ArrayList<String> p;
	private Display parent;	
	private short curr_state, curr_opt, bar_h1, bar_h2, alpha, alpha2, alpha3;
	private float theta, theta2;
	private int sel_y, bar_yc, bar_yc2, bar_xc, tit_y;
	private Font pl_base, opt_base;
	private String npn;
	
	public ProfileSelect(Display parent) {
		this.parent = parent;
		init_values();
	}
	
	private void init_values() {
		this.curr_state = 0;
		this.npn = "(enter profile name)";
		this.p = parent.profiles;
		this.pl_base = new Font("Plantin MT Std", Font.PLAIN, sH(48));
		this.opt_base = new Font("Plantin MT Std", Font.PLAIN, sH(38));
		this.tit_y = proTT; this.bar_yc = 250;
		this.theta = (float)Math.PI; this.theta2 = (float)Math.PI;
		this.alpha = 0; this.alpha2 = 0; this.alpha3 = 0;
		this.bar_h1 = 50; this.bar_h2 = 50; this.sel_y = get_opt_y(0);
		this.bar_xc = proXL; this.bar_yc2 = proYT;
	}
	
	public void render(Graphics2D g) {
		tmp = g;
		draw_primary(g);
		switch (curr_state) {
			case 0: transition_in(g); break;
			case 1: case 2: pro_select(g); break;
			case 3: transition_menu(g); break;
			case 4: transition_main(g);
		}
	}
	
	private void draw_primary (Graphics2D g) {
		g.drawImage(parent.get_images().get("MENU_BG"), sX(0), sY(0), sW(1920), sH(1080), null);
		g.setColor(Color.WHITE);
		for (Snow s : parent.snow) g.fillOval(sX(s.x), sY(s.y), sW(s.dia), sH(s.dia));
		
		g.setColor(parent.bg_color);
		g.fillRect(sX(0), sY(0), sW(1920), sH(bar_h1));
		g.fillRect(sX(0), sY(1080-bar_h2+1), sW(1920), sH(bar_h2));	
	}
	
	private void draw_menu (Graphics2D g) {
		int menu_h = (proYB - proYT) - (finYB - finYT - box_h);
		int menu_y = proYT + (proYB - proYT - menu_h)/2;
			
		g.setColor(new Color(222, 238, 246, 200));
		g.fillRect(sX(proXL), sY(menu_y), sW(2*(origX-proXL)), sH(menu_h));
		
		g.setColor(Color.WHITE);
		g.fillRect(sX(proXL), sY(tit_y), sW(2*(origX-proXL)), sH(sel_h));
		
		int alpha = (bar_yc < sel_y ? 0 : sH(bar_yc - sel_y));
		if (alpha > 255) alpha = 255;
		g.setColor(new Color(152, 189, 209, alpha));
		g.fillRect(sX(proXL), sY(sel_y), sW(1920-(2*proXL)), sH(opt_h));
		
		g.setColor(parent.bg_color);
		g.setStroke(new BasicStroke(sW(8), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.drawLine(sX(proXL), sY(proYB), sX(proXL), sY(proYT));
		g.drawLine(sX(1920-proXL), sY(proYB), sX(1920-proXL), sY(proYT));
		
		g.setFont(pl_base);
		int fw = g.getFontMetrics().stringWidth("select profile");
		g.drawString("select profile", (sX(1920)-fw)/2, sY(tit_y + 48));
	}
	
	private void transition_in (Graphics2D g) {
		draw_menu(g);
		int alpha, fw;
		g.setFont(opt_base);
		g.setColor(main);
		for (int i = 0; i < p.size(); i++) {
			if (get_opt_y(i) > bar_yc) alpha = 0;
			else alpha = (sH(bar_yc - get_opt_y(i)) > 255 ? 255 : sH(bar_yc - get_opt_y(i)));
			g.setComposite(AlphaComposite.SrcOver.derive(alpha/255f));
			fw = g.getFontMetrics().stringWidth(p.get(i));
			g.drawString(p.get(i), (sX(1920)-fw)/2, sY(l_sp*i + 310));
		}
		
		g.setColor(parent.bg_color);
		fw = g.getFontMetrics().stringWidth("use temp profile");
		g.drawString("use temp profile", (sX(1920)-fw)/2, sY(827));
		if (p.size() >= 8) g.setColor(shim);
		fw = g.getFontMetrics().stringWidth("new profile");
		g.drawString("new profile", (sX(1920)-fw)/2, sY(887));
	}
	
	private void pro_select(Graphics2D g) { 
		transition_in(g); 
		draw_new(g);
	}
	
	private void draw_new(Graphics2D g) {
		g.setComposite(AlphaComposite.SrcOver.derive(alpha/255f));
		g.setColor(new Color(83, 102, 112, 200));
		g.fillRect(sX(0), sY(bar_h1), sW(1920), sH(980));
		
		g.setColor(new Color(173, 208, 226, 225));
		g.fillRect(sX(new_x), sY(new_y), sW(new_w), sH(new_h));
		
		g.setColor(Color.WHITE);
		g.fillRect(sX(new_x), sY(365), sW(new_w), sH(sel_h));
		
		if (is_valid()) g.setColor(new Color(136, 171, 194, 255));
		else g.setColor(new Color(243, 183, 199, 255));
		g.fillRect(sX(737), sY(454), sW(max_len), sH(54));
		
		g.setColor(parent.bg_color);	g.setFont(pl_base);
		int fw = g.getFontMetrics().stringWidth("new profile");
		g.drawString("new profile", (sX(1920)-fw)/2, sY(nti_y + 50));
		
		g.setStroke(new BasicStroke(sF(8), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.drawLine(sX(new_x), sY(new_y-32), sX(new_x), sY(new_y+new_h+32));
		g.drawLine(sX(1920-new_x), sY(new_y-32), sX(1920-new_x), sY(new_y+new_h+32));
		
		g.setFont(opt_base);
		if (is_valid()) g.setColor(main);
		else g.setColor(new Color(118, 14, 28));
		fw = g.getFontMetrics().stringWidth(npn);
		g.drawString(npn, (sX(1920)-fw)/2, sY(491));
	}
	
	private void transition_menu(Graphics2D g) {
		// calculate differences
		if (bar_yc > finYB) bar_yc = proYB - 2 - (int)((proYB-finYB)/2 * (1+Math.cos(theta)));
		else bar_yc = finYB;
		if (bar_yc2 < finYT) bar_yc2 = proYT + 2 + (int)((finYT-proYT)/2 * (1+Math.cos(theta)));
		else bar_yc2 = finYT;
		if (bar_xc < finXL) bar_xc = proXL + 2 + (int)((finXL-proXL)/2 * (1+Math.cos(theta)));
		else bar_xc = finXL;
		if (tit_y < parent.s_mn.get_opt_y(0)) tit_y = proTT + 2 + (int)((parent.s_mn.get_opt_y(0) - proTT)/2 * (1+Math.cos(theta)));
		else tit_y = parent.s_mn.get_opt_y(0);
		
		int menu_h = (bar_yc - bar_yc2) - (finYB - finYT - box_h);
		int menu_y = bar_yc2 + (bar_yc - bar_yc2 - menu_h)/2;
		
		// draw background box
		g.setColor(new Color(222, 238, 246, 200));
		if (Math.abs(menu_y - box_y) < 8) g.fillRect(sX(finXL), sY(box_y), sW(box_w), sH(box_h));
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
		g.setComposite(AlphaComposite.SrcOver.derive(alpha/255f));
		int fw = fm.stringWidth("select profile");
		g.drawString("select profile", (sX(1920)-fw)/2, sY(tit_y + 48));
		
		g.setComposite(AlphaComposite.SrcOver.derive(1f - alpha/255f));
		fw = fm.stringWidth("start");
		g.drawString("start", (sX(1920)-fw)/2, sY(tit_y + 48));
		
		// fade out profiles
		g.setFont(opt_base);
		g.setColor(main);
		for (int i = 0; i < p.size(); i++) {
			g.setComposite(AlphaComposite.SrcOver.derive(alpha/255f));
			fw = g.getFontMetrics().stringWidth(p.get(i));
			g.drawString(p.get(i), (sX(1920)-fw)/2, sY(l_sp*i + 310));
		}
		
		g.setColor(parent.bg_color);
		fw = g.getFontMetrics().stringWidth("use temp profile");
		g.drawString("use temp profile", (sX(1920)-fw)/2, sY(827));
		if (p.size() >= 8) g.setColor(shim);
		fw = g.getFontMetrics().stringWidth("new profile");
		g.drawString("new profile", (sX(1920)-fw)/2, sY(887));
		
		// fade in new menu
		g.setFont(pl_base);
		g.setComposite(AlphaComposite.SrcOver.derive((float)(alpha2/255f)));
		fw = fm.stringWidth("settings");
		g.drawString("settings", (sX(1920)-fw)/2, sY(714));
		fw = fm.stringWidth("exit");
		g.drawString("exit", (sX(1920)-fw)/2, sY(787));
		g.drawImage(parent.get_images().get("LOGO_BK"), sX(571), sY(200), sW(777), sH(258), null);
	}
	
	private void transition_main(Graphics2D g) {
		if (bar_h1 < fin_barh) bar_h1 = (short)(bar_h2 + 2 + (int)((fin_barh-bar_h2)/2 * (1+Math.cos(theta2))));
		else bar_h1 = fin_barh;
		
		transition_in(g);
		g.setColor(Color.WHITE);
		g.setComposite(AlphaComposite.SrcOver.derive(alpha3/255f));
		g.fillRect(sX(0), sY(bar_h1), sW(1920), sH(1080-bar_h2-bar_h1));
		
		g.setColor(parent.bg_color);
		g.fillRect(sX(0), sY(0), sW(1920), sH(bar_h1));
	}
	
	public void re_init() {
		init_values();
	}
	
	private boolean is_valid() {
		tmp.setFont(opt_base);
		int fw = tmp.getFontMetrics().stringWidth(npn);
		if (npn.trim().equals("")) return false;
		if (p.contains(npn)) return false;
		if (fw + sW(20) > sW(max_len)) return false;
		if (npn.equalsIgnoreCase("settings")) return false;
		return true;
	}
	
	public void handle(KeyEvent e) {
		if (curr_state == 2) {
			char nxt = e.getKeyChar();
			if ((nxt>31 && nxt<40) || (nxt>47 && nxt<58) || (nxt>63 && nxt<91) || (nxt>93 && nxt<123)) {
				if (npn.equals("(enter profile name)")) npn = nxt + "";
				else if (npn.length() < max_len - 1) npn += (nxt + "");
				if(tmp.getFontMetrics().stringWidth(npn) > max_len) npn = npn.substring(0, Math.max(0, npn.length()-1));
			}
		}
		
		switch (e.getKeyCode()) {
			case KeyEvent.VK_UP: 
				if (curr_state < 2) {
					if (curr_opt == 8) curr_opt = (short)(p.size() - 1);
					else curr_opt = (short)(curr_opt - 1 < 0 ? 0 : curr_opt - 1); 	
				}
				break;
			case KeyEvent.VK_DOWN: 
				if (curr_state < 2) {
					if (curr_opt == p.size() - 1) curr_opt = (short)8;
					else curr_opt = (short)(curr_opt + 1 > max_opt? max_opt: curr_opt + 1);
				}
				break;
			case KeyEvent.VK_R: if (curr_state != 2) init_values(); break;
			case KeyEvent.VK_ENTER:
				if (curr_state == 2) {
					if (is_valid()) { p.add(npn);	Collections.sort(p); }
					try {
						PrintWriter writer = new PrintWriter(Display.prof_path + "" + npn + ".dat", "UTF-8");
						writer.write(""); writer.close();
					} catch (Exception ex) {ex.printStackTrace();}
					npn = "(enter profile name)";
					curr_state = 1;
				}
				break;
			case KeyEvent.VK_Z:
				if (curr_state == 1 && alpha < 100) {
					switch (curr_opt) {
						case 9: 
							if (p.size() < 8) curr_state = 2; 
							break;
						case 8: 
							parent.set_profile("");	curr_state = 4;
							break;
						default:
							parent.set_profile(p.get(curr_opt)); curr_state = 4;
							break;
					}
					break;
				}
			case KeyEvent.VK_ESCAPE: case KeyEvent.VK_X:
				if (curr_state == 2 && e.getKeyCode() == KeyEvent.VK_ESCAPE) curr_state = 1;
				else if (curr_state == 1) {
					curr_state = 3;
					bar_xc = proXL; bar_yc = proYB; bar_yc2 = proYT; alpha = 255; theta = (float)(Math.PI);
				}
				break;
			case KeyEvent.VK_BACK_SPACE:
				if (curr_state == 2) npn = npn.substring(0, Math.max(0, npn.length()-1)); break;
		}
	}
	
	private int get_opt_y() {
		if (curr_opt < 8) return l_sp*curr_opt + 273;
		return l_sp*curr_opt + 310;
	}
	
	private int get_opt_y(int num) { 
		if (curr_opt < 8) return l_sp*num + 273;
		return l_sp*num + 310;
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
			case 0: // transition in
				bar_yc += dL1;
				if (bar_yc > 900) curr_state = 1;
				break;
			case 1: // idle
				if (bar_yc < 1280) bar_yc += dL1;
				if (alpha > 0) alpha -= dA;
				if (alpha < 0) alpha = 0;
				break;
			case 2: // new entry
				if (alpha < 255) alpha += dA;
				if (alpha > 255) alpha = 255;
				break;
			case 3: // transition back
				if (alpha > 0) alpha -= dA2;
				if (alpha < 0) alpha = 0;
				if (alpha < 2) alpha2 += dA2;
				if (alpha2 > 255) alpha2 = 255;
				theta += dT;
				if (theta > 2*Math.PI) theta = (float)(-2*Math.PI);
				if (alpha2 == 255) {
					parent.s_mn.re_init(0, 1);
					parent.set_state(Display.State.MENU);
				}
							
				break;
			case 4: // transition forwards
				theta2 += dT;
				if (theta > 2*Math.PI) theta = (float)(-2*Math.PI);
				if (alpha3 < 255) alpha3 += dA2;
				if (alpha3 >= 255) { 
					alpha3 = 255; 
					parent.stop_bgm(); parent.s_ac.re_init();
					parent.set_state(Display.State.MAIN); 
				}
		}
	}
	
	public void note_pressed(byte id, byte vel, long timestamp) {

	}
	
	public void note_released(byte id, long timestamp) {}
	public void damp_pressed(long timestamp) {}
	public void damp_released(long timestamp) {}
	
	private int sX (int x) { return parent.scaleX(x); }
	private int sY (int y) { return parent.scaleY(y); }
	private int sW (int w) { return parent.scaleW(w); }
	private int sH (int h) { return parent.scaleH(h); }
	private float sF (float f) { return parent.scaleF(f); }
}