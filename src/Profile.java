import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

public class Profile {
	protected HashMap<String, HashMap<String, Integer>> map;
	public String name;
	
	public Profile() {
		map = new HashMap<String, HashMap<String, Integer>>();
	}
	
	public Profile(String filename) {
		this.name = filename.substring(0, filename.indexOf(".dat"));
		ArrayList<String> lines = new ArrayList<String>();
		map = new HashMap<String, HashMap<String, Integer>>();
		try {
			Scanner read = new Scanner(new File(filename));
			while (read.hasNextLine()) {
				lines.add(read.nextLine());
			}
			parse_profile(lines);
			read.close();
		} catch (FileNotFoundException e) {
			System.err.println("No file found. Using blank profile.");
		}
	}
	
	public void add_chord(Chord in, Chord out) {
		System.err.println("ADDING TO PROFILE: " + in + " --> " + out);
		if (!map.containsKey(in.code())) {
			HashMap<String, Integer> nC = new HashMap<String, Integer>();
			nC.put(out.code(), 1);
			map.put(in.code(), nC);
		} else {
			if (!map.get(in.code()).containsKey(out.code())) {
				map.get(in.code()).put(out.code(), 1);
			} else {
				int before = map.get(in.code()).get(out.code());
				map.get(in.code()).replace(out.code(), before + 1);
			}
		}
	}
	
	public void write_profile () {
		String fname = name + ".dat";
		File profile = new File(fname);
		profile.delete();
		profile = new File(fname);
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(profile));
            for (String s : map.keySet()) {
            	output.write(s + "::");
            	for (String t : map.get(s).keySet()) {
            		output.write(t + ";");
            		output.write(map.get(s).get(t) + "|");
            	}
            	output.write("\n");
            }
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public HashMap<Chord, Double> next_chords(Chord in, int num) {
		if (!map.containsKey(in.code())) return null;
		HashMap<String, Integer> spec = map.get(in.code());
		if (spec.values().size() < num) num = spec.values().size();
		HashMap<Chord, Double> ret = new HashMap<Chord, Double>();
		int total = 0; int[] tracks = new int[num];
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		for (Integer i : spec.values()) {
			total += i;	tmp.add(i);
		}
		Collections.sort(tmp);
		ArrayList<String> marks = new ArrayList<String>();
		System.out.println("NUM:" + num);
		for (int i = 0; i < num; i++) if (i < tmp.size()) tracks[i] = tmp.get(tmp.size()-i-1);
		for (int i = 0; i < num; i++) {
			for (String s : spec.keySet()) {
				if (spec.get(s) == tracks[i] && !marks.contains(s)) {
					ret.put(new Chord(s), (double)tracks[i]/(double)total);
					marks.add(s);
				}
			}
		}
		return ret;
	}
	
	private void parse_profile (ArrayList<String> in) {
		for (String s : in) {
			HashMap<String, Integer> h = new HashMap<String, Integer>();
			String key1 = s.substring(0, s.indexOf("::"));
			s = s.substring(s.indexOf("::") + 2, s.length());
			while (s.contains("|")) {
				String key2 = s.substring(0, s.indexOf(";"));
				Integer val2 = Integer.parseInt(s.substring(s.indexOf(";") + 1, s.indexOf("|")));
				s = s.substring(s.indexOf("|") + 1, s.length());
				h.put(key2, val2);
			}
			map.put(key1, h);
		}
	}
	
	public static void main(String [] args) {
		Profile p1 = new Profile("profile\\profile1p.dat");
		System.out.println(p1.map.toString());
		System.out.println(p1.next_chords(new Chord("1-100M"), 3));
		p1.write_profile();
	}
	
}