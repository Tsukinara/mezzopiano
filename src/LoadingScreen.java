import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class LoadingScreen {
	private final short dA = -5, dL = 10;
	private final float diA = 0.01f, dS = 0.05f;
	private final int staff_xs = 195, staff_xe = 1834;
	private final int[] staff_ys = {525, 537, 549, 561, 573, 640, 652, 664, 676, 688, 729, 741, 753, 765, 777};
	private final int bar_ys = 524, bar_ye = 778;
	private final int[] bar_xs = {195, 427, 660, 892, 1125, 1357, 1589, 1822, 1834};
	private final String[] imgs = {
			"TREBC", "TREBC", "BASSC", "ENOTE", "BRACK", 
			"QREST", "QREST", "QREST", "KYSIG", "KYSIG", "KYSIG", "TMSIG", "TMSIG", "TMSIG"};
	private final int[] imgx = {207, 207, 207, 244, 177, 358, 358, 358, 251, 251, 251, 290, 290, 290};
	private final int[] imgw = {29, 29, 35, 18, 17, 14, 14, 14, 28, 28, 28, 20, 20, 20};
	private final int[] imgy = {513, 629, 728, 469, 639, 529, 644, 733, 516, 631, 733, 525, 640, 729};
	private final int[] imgh = {77, 77, 43, 28, 139, 40, 40, 40, 45, 45, 45, 48, 48, 48};
	private final String[] txts = {"=180", "computer", "piano"};
	private final int[] txtx = {262, 51, 95}, txty = {497, 561, 717};
	private final int nb_height = 50;
	
	private Display parent;
	
	private Font pl_base;
	private short alpha, bar_height, curr_state, delay;
	private short staff_xc, bar_yc, img_xc, img_yc;
	private int[] txt_alpha, txt_da;
	private float rtheta, btheta;
	private float[] img_alpha, img_da;
	private boolean anim_done, bgm_flag;
	
	private HashMap<String, BufferedImage> images;
	private BufferedImage highlight;
	
	public LoadingScreen (Display par) {
		this.parent = par;
		init_values();
		init_images();
		new Thread() { public void run(){
			parent.load_all_resources();
		}}.start();
	}
	
	private void init_values() {
		this.curr_state = 0; this.bar_height = 101;
		this.alpha = 255; 
		this.img_alpha = new float[imgs.length]; this.img_da = new float[imgs.length];
		this.txt_alpha = new int[txts.length]; this.txt_da = new int[txts.length];
		this.delay = 500 / 20;
		this.staff_xc = 194; this.bar_yc = 450;
		this.img_xc = 194; this.img_yc = 450;
		this.rtheta = (float)(Math.PI);
		this.pl_base = new Font("Plantin MT Std", Font.ITALIC, sH(44));
		this.anim_done = false; this.bgm_flag = false;
		this.btheta = (float)(Math.PI);
	}
	
	private void init_images() {
		images = new HashMap<String, BufferedImage>();
		try {
			highlight = ImageIO.read(new File(Display.img_path + "loading\\highlight.png"));
			for (String s : imgs) {
				if (!images.containsKey(s))
					images.put(s, ImageIO.read(new File(Display.img_path + "loading\\" + s.toLowerCase() + ".png")));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void render (Graphics2D g) {
		if (curr_state < 4) draw_primary(g);
		
		switch (curr_state) {
			case 0: transition_in(g); break;
			case 1:	load_anim(g); break;
			case 2: trans_idle(g); break;
			case 3: draw_idle(g); break;
			case 4: transition_out(g); break;
		}
	}
	
	private void draw_primary (Graphics2D g) {
		g.setColor(parent.bg_color);
		g.drawImage(parent.get_images().get("LOAD_BG"), sX(0), sY(0), sW(1920), sH(1080), null);
		g.drawImage(parent.get_images().get("LOGO_BK"), sX(720), sY(253), sW(477), sH(163), null);
		g.fillRect(sX(0), sY(0), sW(1920), sH(this.bar_height));
		g.fillRect(sX(0), sY(980), sW(1920), sH(this.bar_height));
	}
	
	private void transition_in (Graphics2D g) {
		g.setColor(new Color(0, 0, 0, alpha));
		g.fillRect(sX(0), sY(0), sW(1920), sH(1080));
	}
	
	private void load_anim (Graphics2D g) {
		g.setColor(new Color(26, 26, 26, alpha));
		g.setFont(pl_base);
		g.drawString("now loading", sX(844), sY(901));
		double[] x1 = {673, 735, 735}, ys = {889, 885, 893}; g.fillPolygon(sX(x1), sY(ys), 3);
		double[] x2 = {1247, 1185, 1185}; g.fillPolygon(sX(x2), sY(ys), 3);
		
		g.setStroke(new BasicStroke(sF(1.5f), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		for (int i = 0; i < staff_ys.length; i++) {
			int y = staff_ys[i];
			int ix = staff_xc + 3*(520-y);
			if (ix > staff_xs) g.drawLine(sX(staff_xs), sY(y), sX(ix < staff_xe? ix: staff_xe), sY(y));
		}	
		g.setStroke(new BasicStroke(sF(3f), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		for (int i = 0; i < bar_xs.length; i++) {
			int x = bar_xs[i];
			int iy = bar_yc + (int)(1.25*(190-x));
			if (i == bar_xs.length - 1) g.setStroke(new BasicStroke(8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
			if (iy > bar_ys) g.drawLine(sX(x), sY(bar_ys), sX(x), sY(iy < bar_ye? iy: bar_ye));
		}
		for (int i = 0; i < imgs.length; i++) {
			if (imgx[i] < img_xc && imgy[i] < img_yc) {
				img_da[i] = diA;
				g.setComposite(AlphaComposite.SrcOver.derive(img_alpha[i]));
				g.drawImage(images.get(imgs[i]), sX(imgx[i]), sY(imgy[i]), sW(imgw[i]), sH(imgh[i]), null);
			}
		}
		for (int i = 0; i < txts.length; i++) {
			if (txtx[i] < img_xc && txty[i] < img_yc) {
				txt_da[i] = -dA/2;
				switch (i) {
					case 0: g.setFont(new Font("Tangerine", Font.BOLD, sH(48))); break;
					case 1: case 2: g.setFont(new Font("Tangerine", Font.PLAIN, sH(60))); break;
				}
				g.setComposite(AlphaComposite.SrcOver.derive((float)txt_alpha[i]/255f));
				g.drawString(txts[i], sX(txtx[i]), sY(txty[i]));
			}
		}
		g.setColor(parent.bg_color);
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 3; j++) {
				int x = 232*i + 534;
				int y = (j == 0 ? 537 : (j == 1 ? 652 : 741));
				if ((double)(bar_yc-y)/(double)(bar_yc-450)*(double)(staff_xc-194) > (x-194)) {
					int a = Math.abs((bar_yc-450)*x - (194-staff_xc)*y + 194*450 - bar_yc*staff_xc);
					a = (int)(a/Math.sqrt(Math.pow(bar_yc-450, 2) + Math.pow(194-staff_xc, 2)));
					a = (a/2-100 > 255 ? 255 : a/2-100);
					if (a > 0) {
						g.setComposite(AlphaComposite.SrcOver.derive(a/255f));
						g.fillRect(sX(x), sY(y), sW(20), sH(6));
					}
					if (a == 255 && i == 5 && j == 2) anim_done = true;
				}
			}
		}
	}
	
	private void trans_idle (Graphics2D g) {
		draw_staff(g);

		g.setColor(new Color(26, 26, 26, alpha));
		g.setFont(pl_base);
		g.drawString("now loading", sX(844), sY(901));
		
		g.setColor(new Color(26, 26, 26, 255-alpha));
		g.drawString("play any note to start", sX(767), sY(901));
		
		
		g.setColor(parent.bg_color);
		double[] x1 = {673, 735, 735}, ys = {889, 885, 893}; g.fillPolygon(sX(x1), sY(ys), 3);
		double[] x2 = {1247, 1185, 1185}; g.fillPolygon(sX(x2), sY(ys), 3);
	}
	
	private void draw_idle (Graphics2D g) {
		draw_staff(g);
		
		g.setComposite(AlphaComposite.SrcOver.derive((1 + (float)Math.cos(rtheta))/2));
		g.drawImage(highlight, sX(553), sY(835), sW(804), sH(115), null);
		
		g.setComposite(AlphaComposite.SrcOver.derive(1f));
		g.setColor(parent.bg_color);
		g.setFont(pl_base);
		g.drawString("play any note to start", sX(767), sY(901));
		
		double[] x1 = {673, 735, 735}, ys = {889, 885, 893}; g.fillPolygon(sX(x1), sY(ys), 3);
		double[] x2 = {1247, 1185, 1185}; g.fillPolygon(sX(x2), sY(ys), 3);
	}
	
	private void transition_out (Graphics2D g) {
		g.drawImage(parent.get_images().get("LOAD_BG"), sX(0), sY(0), sW(1920), sH(1080), null);
		draw_staff(g);
		g.drawImage(highlight, sX(553), sY(835), sW(804), sH(115), null);
		g.setColor(parent.bg_color);
		g.setFont(pl_base);
		g.drawString("play any note to start", sX(767), sY(901));
		
		double[] x1 = {673, 735, 735}, ys = {889, 885, 893}; g.fillPolygon(sX(x1), sY(ys), 3);
		double[] x2 = {1247, 1185, 1185}; g.fillPolygon(sX(x2), sY(ys), 3);
		
		g.setColor(new Color(255, 255, 255, alpha));
		g.fillRect(sX(0), sY(0), sW(1920), sH(1080));
		
		g.drawImage(parent.get_images().get("LOGO_BK"), sX(720), sY(253), sW(477), sH(163), null);
		
		g.setColor(parent.bg_color);
		bar_height = (short)(bar_height - sH(25)*(Math.cos(btheta)+1));
		if (bar_height < nb_height) bar_height = nb_height;
		g.fillRect(sX(0), sY(0), sW(1920), sH(this.bar_height));
		g.fillRect(sX(0), sY(1080-bar_height+1), sW(1920), sH(this.bar_height));
	}
	
	private void draw_staff (Graphics2D g) {
		g.setStroke(new BasicStroke(sF(1.5f), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		for (int i = 0; i < staff_ys.length; i++) {
			int y = staff_ys[i];
			g.drawLine(sX(staff_xs), sY(y), sX(staff_xe), sY(y));
		}
		g.setStroke(new BasicStroke(sF(3f), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		for (int i = 0; i < bar_xs.length; i++) {
			int x = bar_xs[i];
			if (i == bar_xs.length - 1) g.setStroke(new BasicStroke(8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
			g.drawLine(sX(x), sY(bar_ys), sX(x), sY(bar_ye));
		}
		for (int i = 0; i < imgs.length; i++) g.drawImage(images.get(imgs[i]), sX(imgx[i]), sY(imgy[i]), sW(imgw[i]), sH(imgh[i]), null);
		for (int i = 0; i < txts.length; i++) {
			switch (i) {
				case 0: g.setFont(new Font("Tangerine", Font.BOLD, sH(48))); break;
				case 1: case 2: g.setFont(new Font("Tangerine", Font.PLAIN, sH(60))); break;
			}
			g.drawString(txts[i], sX(txtx[i]), sY(txty[i]));
		}
		
		g.setColor(parent.bg_color);
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 3; j++) {
				int x = 232*i + 534;
				int y = (j == 0 ? 537 : (j == 1 ? 652 : 741));
				g.fillRect(sX(x), sY(y), sW(20), sH(6));
			}
		}
	}
	
	public void handle(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE: System.exit(0); break;
			case KeyEvent.VK_R: init_values(); break;
			default: 
				if (curr_state == 3) curr_state++;
				//TODO: play confirmation sound
		}
	}
	
	public void step () {
		switch (curr_state) {
		case 0: // transition in
			if (delay <= 0) this.alpha = (short)(alpha + dA < 0 ? 0 : alpha + dA);
			else delay--;
			
			if (alpha <= 0) this.curr_state++;
			break;
		case 1: // loading animation
			alpha = (short)(alpha - dA > 255 ? 255 : alpha - dA);
			staff_xc += dL; bar_yc += dL;
			img_xc += dL/3; img_yc += dL/3;
			
			for (int i = 0; i < imgs.length; i++) {
				img_alpha[i] += img_da[i];
				if (img_alpha[i] > 1f) img_alpha[i] = 1f;
			}
			for (int i = 0; i < txts.length; i++) {
				txt_alpha[i] += txt_da[i];
				if (txt_alpha[i] > 255) txt_alpha[i] = 255;
			}
			
			if (anim_done) curr_state++;
			break;
		case 2: // transition to idle
			alpha = (short)(alpha + dA < 0 ? 0 : alpha + dA);
			if (alpha <= 0) this.curr_state++;
			break;
		case 3: // idle animation
			rtheta += dS;
			if (rtheta >= 2*Math.PI) rtheta = (float)(-2*Math.PI);
			break;
		case 4: // transition out
			if (!bgm_flag) { parent.play_bgm("menu_bgm.mp3"); bgm_flag = true; }
		//	if (!bgm_flag) { parent.play_bgm("nggyu.mp3"); bgm_flag = true; }
			alpha = (short)(alpha - dA > 255 ? 255 : alpha - dA);
			if (bar_height != nb_height) btheta += dS/3;
			else parent.set_state(Display.State.MENU);
			break;
		}
	}
	
	public void note_pressed(byte id, byte vel, long timestamp) {
		if (curr_state == 3) curr_state++;
	}
	
	public void note_released(byte id, long timestamp) {}
	public void damp_pressed(long timestamp) {}
	public void damp_released(long timestamp) {}
	
	private int sX (double x) { return parent.scaleX(x); }
	private int sY (double y) { return parent.scaleY(y); }
	private int sH (double h) { return parent.scaleH(h); }
	private int sW (double w) { return parent.scaleW(w); }
	private float sF (float f) { return parent.scaleF(f); }
	private int[] sX (double[] x) { return parent.scaleX(x); }
	private int[] sY (double[] y) { return parent.scaleY(y); }
	
}