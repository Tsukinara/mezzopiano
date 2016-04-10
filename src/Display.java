import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Display extends JFrame implements Runnable {
	private static final long serialVersionUID = 4767630629171590730L;
	private static final String DEFAULT_TITLE = "Project \u00c1deux";
	private static final int s_width = Toolkit.getDefaultToolkit().getScreenSize().width;
	private static final int s_height = Toolkit.getDefaultToolkit().getScreenSize().height;
	private static final int num_snowflakes = 150;
	public static final String font_path = "resources\\fonts\\";
	public static final String img_path = "resources\\images\\";
	public static final String musc_path = "resources\\music\\";
	public static final String prof_path = "profile\\";
	public static final String settings = prof_path + "settings.dat";
	public final Color bg_color = new Color(26, 26, 26, 255);
	
	public int width, height;
	private int draw_width, draw_height, offset_y, offset_x;
	private boolean windowed;
	
	private HashMap<String, BufferedImage> images;
	private MPlayer sfxplayer, mscplayer;
	protected LoadingScreen s_ls;
	protected Menu s_mn;
	protected ProfileSelect s_ps;
	protected AppCore s_ac;
	protected SettingsMenu s_sm;
	protected Settings set;
	protected Snow[] snow;
	protected ArrayList<String> profiles;
	protected HardwareInterface hint;
	
	private Profile prof;
	protected NoteBuffer buffer;
	private Thread curr;
	public State state;
	

	public enum State {
		LOADING, MENU, MAIN, SETTINGS, PROFILE
	}

	public Display() {
		super(DEFAULT_TITLE);
		this.state = State.LOADING;
		this.sfxplayer = null; this.mscplayer = null;
		this.width = s_width; this.height = s_height;
		this.set = new Settings(new File(settings));
		
		if (set.window_size == -1) {
			windowed = false; width = s_width; height = s_height;
		} else { 
			windowed = true; width = set.window_size; height = set.window_size*9/16;
		}
		
		setUndecorated(!windowed);
		setSize(width, height);	

		setLocationRelativeTo(null);
		setResizable(false);
		initialize_basics();

		s_ls = new LoadingScreen(this);
		s_mn = new Menu(this);
		s_ac = new AppCore(this);
		s_sm = new SettingsMenu(this);
		s_ps = new ProfileSelect(this);

		setIconImage(images.get("FR_ICON"));
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "Invisimouse"); 
		setCursor(blankCursor);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setBackground(Color.BLACK);

		this.curr = new Thread(this);
		this.curr.start();
		startHardwareInterface();

		addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				switch (state) {
				case LOADING: s_ls.handle(e); break;
				case MENU: s_mn.handle(e); break;
				case MAIN: s_ac.handle(e); break;
				case SETTINGS: s_sm.handle(e); break;
				case PROFILE: s_ps.handle(e); break;
				}
			}

			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		
		//TODO: REMOVE THIS
		addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {}
			public void focusLost(FocusEvent e) {
				//System.exit(0);
			}
		});
	}
	
	public void reset() {
		s_ls = new LoadingScreen(this);
		s_mn = new Menu(this);
		s_ac = new AppCore(this);
		s_sm = new SettingsMenu(this);
		s_ps = new ProfileSelect(this);
		
		this.state = State.LOADING;
		this.sfxplayer = null; this.mscplayer = null;
		this.width = s_width; this.height = s_height;
	}
	
	private void calculate_offsets() {
		this.draw_width = width;
		this.draw_height = width*9/16;
		this.offset_y = (height - draw_height) / 2; this.offset_x = 0;
		if (windowed) {
			this.offset_x += (getWidth() - getContentPane().getWidth())/2;
			this.offset_y += (getHeight() - getContentPane().getHeight()) - offset_x;
		}
	}
	
	public void begin() {
		setVisible(true); 
		getContentPane().setPreferredSize(new Dimension(width, height)); pack();
		calculate_offsets();
	}
	
	public void note_pressed(byte id, byte vel, long timestamp) {
		switch (state) {
			case LOADING: s_ls.note_pressed(id, vel, timestamp); break;
			case MENU: s_mn.note_pressed(id, vel, timestamp); break;
			case MAIN: s_ac.note_pressed(id, vel, timestamp); break;
			case SETTINGS: s_sm.note_pressed(id, vel, timestamp); break;
			case PROFILE: s_ps.note_pressed(id, vel, timestamp); break;
		}
	}
	
	public void note_released(byte id, long timestamp) {
		switch (state) {
			case LOADING: s_ls.note_released(id, timestamp); break;
			case MENU: s_mn.note_released(id, timestamp); break;
			case MAIN: s_ac.note_released(id, timestamp); break;
			case SETTINGS: s_sm.note_released(id, timestamp); break;
			case PROFILE: s_ps.note_released(id, timestamp); break;
		}
	}
	
	public void damp_pressed(long timestamp) {
		switch (state) {
			case LOADING: s_ls.damp_pressed(timestamp); break;
			case MENU: s_mn.damp_pressed(timestamp); break;
			case MAIN: s_ac.damp_pressed(timestamp); break;
			case SETTINGS: s_sm.damp_pressed(timestamp); break;
			case PROFILE: s_ps.damp_pressed(timestamp); break;
		}		
	}
	
	private void startHardwareInterface() {
		Thread t = new Thread() {
			public void run() {
				hint = new HardwareInterface();
				hint.initialize();
			}
		};
		t.run();
	}
	
	public void damp_released(long timestamp) {
		switch (state) {
			case LOADING: s_ls.damp_released(timestamp); break;
			case MENU: s_mn.damp_released(timestamp); break;
			case MAIN: s_ac.damp_released(timestamp); break;
			case SETTINGS: s_sm.damp_released(timestamp); break;
			case PROFILE: s_ps.damp_released(timestamp); break;
		}		
	}

	private void initialize_basics() {
		calculate_offsets();
		this.images = new HashMap<String, BufferedImage>();
		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(font_path + "PlantinMTStd-Bold.otf")));
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(font_path + "PlantinMTStd-BoldItalic.otf")));
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(font_path + "PlantinMTStd-Italic.otf")));
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(font_path + "PlantinMTStd-Regular.otf")));
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(font_path + "Tangerine_Bold.ttf")));
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(font_path + "Tangerine_Regular.ttf")));
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(font_path + "OpusTextStd.otf")));
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(font_path + "OpusChordsStd.otf")));

			images.put("LOAD_BG", ImageIO.read(new File(img_path + "load_bg.png")));
			images.put("LOGO_BK", ImageIO.read(new File(img_path + "logo_bk.png")));
			images.put("LOGO_WH", ImageIO.read(new File(img_path + "logo_wh.png")));
			images.put("ICON_TM", ImageIO.read(new File(img_path + "icon_tm.png")));
			images.put("ICON_LT", ImageIO.read(new File(img_path + "icon_lt.png")));
			images.put("ICON_FN", ImageIO.read(new File(img_path + "icon_fn.png")));
			images.put("ICON_NS", ImageIO.read(new File(img_path + "icon_ns.png")));
			images.put("ICON_RN", ImageIO.read(new File(img_path + "icon_rn.png")));
			images.put("ICON_TD", ImageIO.read(new File(img_path + "icon_td.png")));
			images.put("ICON_WD", ImageIO.read(new File(img_path + "icon_wd.png")));
			images.put("ICON_BD", ImageIO.read(new File(img_path + "icon_bd.png")));
			images.put("ICON_NA", ImageIO.read(new File(img_path + "icon_na.png")));
			
			images.put("FR_ICON", ImageIO.read(new File("resources\\icons\\icon.png")));

		} catch (IOException|FontFormatException e) {
			e.printStackTrace();
		}
	}

	public void load_all_resources() {
		snow = new Snow[num_snowflakes];
		for (int i = 0; i < num_snowflakes; i++)
			snow[i] = new Snow(
					Math.random()*scaleX(1920), Math.random()*scaleY(1080), 
					Math.random()*scaleW(8)+1, Math.random()*scaleH(6)+scaleH(4), 
					Math.random()*scaleW(6)+scaleW(4));
		prof = new Profile();
		File dir = new File("profile");
		profiles = new ArrayList<String>();
		File[] list = dir.listFiles();
		for (int i = 0; i < list.length; i++) {
			profiles.add(list[i].getName());
			profiles.set(i, profiles.get(i).substring(0, profiles.get(i).length()-4));
		}
		profiles.remove("settings");
		try {
			images.put("MENU_BG", ImageIO.read(new File(img_path + "menu_bg.png")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void set_profile(String profilename) {
		System.err.println("Setting profile to: " + profilename);
		if (profilename.equals("")) prof = new Profile();
		else prof = new Profile(prof_path + "" + profilename + ".dat");
	}
	
	public Profile profile() { return prof; }

	public void paint(Graphics g) {
		Image i=createImage(getWidth(), getHeight()); 
		render((i.getGraphics()));
		g.drawImage(i,0,0,this);
	}
	
	public void play_clip(String filename) {
		if (sfxplayer != null) sfxplayer.close();
		sfxplayer = new MPlayer(musc_path + filename);
		sfxplayer.play();
	}
	
	public NoteBuffer buffer() { return this.buffer; }
	public void set_buffer(NoteBuffer buffer) { this.buffer = buffer;  s_ac.set_buffer(buffer); }
	
	public void play_bgm(String filename) {
		if (mscplayer != null) mscplayer.close();
		mscplayer = new MPlayer(musc_path + filename);
		mscplayer.loop();
	}
	public void stop_bgm() { if (mscplayer != null) mscplayer.close(); }

	public void render(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
		g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

		switch (this.state) {
		case LOADING:	s_ls.render(g2d);	break;
		case MENU:		s_mn.render(g2d);	break;
		case MAIN:		s_ac.render(g2d);	break;
		case SETTINGS:	s_sm.render(g2d);	break;
		case PROFILE:	s_ps.render(g2d);	break;
		}
	}
	
	public void set_state(State s) { 
		System.err.println("Changing state to: " + s.name()); 
		System.err.flush();
		this.state = s; 
	}

	public HashMap<String, BufferedImage> get_images() { return this.images; }

	public void run() {
		while (Thread.currentThread() == curr) {
			repaint();
			switch (this.state) {
				case LOADING: s_ls.step(); break;
				case MENU: s_mn.step(); break;
				case MAIN: s_ac.step(); break;
				case SETTINGS: s_sm.step(); break;
				case PROFILE: s_ps.step(); break;
			}
			try { Thread.sleep(20); } 
			catch (Exception e) { e.printStackTrace(); }
		}
	}
	
	public int scaleX (double x_old) { return ((int) (x_old / 1920.0 * (double)this.draw_width)) + offset_x; }	
	public int scaleW (double w_old) { return (int) (w_old / 1920.0 * (double)this.draw_width); }
	public float scaleF (float f_old) { return (int) (f_old / 1920.0 * (double)this.draw_width); }
	public int scaleY (double y_old) { return ((int) (y_old / 1080.0 * (double)this.draw_height)) + offset_y; }
	public int scaleH (double h_old) { return (int) (h_old / 1080.0 * (double)this.draw_height); }
	
	public int[] scaleX (double[] x_old) {
		int[] x_new = new int[x_old.length];
		for(int i = 0; i < x_old.length; i++) {
			x_new[i] = scaleX(x_old[i]);
		}
		return x_new;
	}

	public int[] scaleY (double[] y_old) {
		int[] y_new = new int[y_old.length];
		for(int i = 0; i < y_old.length; i++) {
			y_new[i] = scaleY(y_old[i]);
		}
		return y_new;
	}
	
	public int[] scaleH (double[] h_old) {
		int[] h_new = new int[h_old.length];
		for(int i = 0; i < h_old.length; i++) {
			h_new[i] = scaleH(h_old[i]);
		}
		return h_new;
	}
}