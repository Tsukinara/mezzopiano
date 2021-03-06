import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Analyzer {
	protected final static int[] maj_arr = {2, 4, 5, 7, 9, 11};
	protected final static int[] min_arr = {2, 3, 5, 7, 8, 11};
	private final static int ret = 7;
	private final static double VELOCITY_LOOKBEHIND = 1.0;
	private final static int MODE_WINDOW = 8;
	private final static int TEMPO_MIN = 40;
	private final static int TEMPO_MAX = 160;
	private final static int TEMPO_MIN_INTERVALS = 1;
	private final static int TEMPO_CHORD_WEIGHT = 64;
	private final static int TEMPO_CLUSTER_RADIUS = 4;
	private final static double TEMPO_LOOKBEHIND = 4.5;
	private final static int TEMPO_CACHE_SIZE = 4;
	private final static int TEMPO_MIN_NOTE_RES = 75000; // 32nd notes in 4/4 @ 200 BPM
	
	public static AppCore.Mood get_mood(NoteBuffer nb){
		AppCore.Mood to_ret =  AppCore.Mood.M_NEUTRAL;
		int tempo = get_tempo(nb.tempo_buffer, nb.chord_history_t, nb.tempo_history);
		int mode = get_maj_min(nb.chord_history);
		int vel = get_mean_vel(nb.tempo_buffer);
		if (is_chaotic(nb.hold_buffer)){
			return AppCore.Mood.M_CHAOTIC;
		}
		System.out.println(tempo + "\t" + mode + "\t" + vel);
		switch (mode){
		case 0: //major
			if (vel > 55){
				if (tempo > 120 || tempo < 60){
					to_ret = AppCore.Mood.M_HAPPY;
				} else {
					to_ret = AppCore.Mood.M_NEUTRAL;
				}
			} else {
				to_ret = AppCore.Mood.M_TRANQUIL;
			}
			break;
		case 1: //minor
			if (vel > 85){
				to_ret = AppCore.Mood.M_DRAMATIC;
			} else if (vel > 75){
				if (tempo > 120 || tempo < 60){
					to_ret = AppCore.Mood.M_DRAMATIC;
				} else {
					to_ret = AppCore.Mood.M_NEUTRAL;
				}
			} else if (vel > 60){
				if (tempo > 80){
					to_ret = AppCore.Mood.M_NEUTRAL;
				} else {
					to_ret = AppCore.Mood.M_TRANQUIL;
				}
			} else {
				to_ret = AppCore.Mood.M_SAD;
			}
			break;
		default: to_ret = AppCore.Mood.M_NEUTRAL;
		}
		nb.curr_mood = to_ret;
		return to_ret;
	}
	

	private static int get_mean_vel(ArrayList<Note> note_history){
		double window_duration = 0.0, reftime = 0.0;
		int vel = 0, ct = 0;
		boolean first = true;
		for (int i = note_history.size() - 1; i >= 0
				&& window_duration < VELOCITY_LOOKBEHIND; i--){
			vel += note_history.get(i).vel();
			if (first){
				reftime = (double)note_history.get(i).get_start() / 1000000;
			} else {
				window_duration = reftime - ((double)note_history.get(i).get_start()/1000000);
			}
			ct++;
			first = false;
		}
		return (ct > 0)? (vel / ct) : 0;
	}
	
	private static boolean is_chaotic(ArrayList<Note> hold_buffer){
		return hold_buffer.size() > 9;
//		int octave_count[] = new int[12];
//		int sum = 0;
//		for (Note n:hold_buffer){
//			if (octave_count[n.key()] == 0){
//				octave_count[n.key()] = 1;
//			}
//		}
//		for (int i:octave_count){
//			sum += i;
//		}
//		return sum > 5;
	}
	
	public static KeySignature get_key_signature(ArrayList<Note> history, KeySignature curr) {
		int[] vals = new int[12];	
		int[] top = new int[ret];
		int[] hist = new int[history.size()];
		for (Note n : history) vals[n.key()] += 1;
		for (int i = 0; i < history.size(); i++) hist[i] = history.get(i).key();
		for (int i = 0; i < ret; i++) {
			int max = -1, mi = -1;
			for (int j = 0; j < 12; j++) if (vals[j] > max && vals[j] > 0) { max = vals[j]; mi = j; }
			if (mi >= 0) vals[mi] = -1;
			top[i] = mi;
		}
		Arrays.sort(top); if (top[1] == -1) return null;
		int base = -1; boolean maj = true;
		for (int i = 0; i < ret; i++) {
			int b = top[0]; boolean flag;
			if (b != -1) {
				flag = true;
				for (int j : maj_arr) { if (!has(top, (b+j)%12)) flag = false; }
				if (flag) { base = b; maj = true; } 
				else {
					flag = true;
					for (int j : min_arr) if (!has(top, (b+j)%12)) flag = false;
					if (flag) { base = b; maj = false; }
				}
			}
			top = circ_shift(top);
		}
		if (base == -1 && history.size() < 10) return null;
		if (curr != null) {
			int curr_base = Music.getKey(curr.key + "" + curr.type);
			if (curr_base == base + 7 && has(hist, (base+6)%12))
				if (last_index_of(hist, (base+6)%12) > last_index_of(hist, (base+5)%12)) return curr;
			if (curr_base == base + 6 && has(hist, (base+10)%12))
				if (last_index_of(hist, (base+10)%12) > last_index_of(hist, (base+11)%12)) return curr;
		}
		if (maj == false) { maj = true; base = (base+3)%12; }
		return new KeySignature(Music.getSimplestKey(base), maj);
	}
	
	public static int get_maj_min(ArrayList<Chord> cnc_history) {
		String prev, curr;
		int score_maj = 0, score_min = 0;
		int window = Math.min(cnc_history.size(), MODE_WINDOW);
		for (int i = cnc_history.size() - window; i < cnc_history.size(); i++){
			if (i > 0){
				prev = cnc_history.get(i-1).get_name_context_free(3);
			} else {
				prev = null;
			}
			curr = cnc_history.get(i).get_name_context_free(3);
			if (curr.startsWith("CM")){
				score_maj += 10;
			} else if (curr.startsWith("Am")){
				score_min += 10;
			} else if (curr.startsWith("GM")){
				score_maj += 6;
				if (prev != null && prev.startsWith("DM")){
					score_maj += 2;
				}
			} else if (curr.startsWith("EM")){
				score_min += 6;
				if (prev != null && prev.startsWith("BM")){
					score_min += 2;
				}
			}
		}
		return (score_maj > score_min)? 0 : 1;
	}
	
	public static TimeSignature get_time_signature(ArrayList<Note> note_history) {
		return null;
	}
	
	public static int get_tempo(ArrayList<Note> note_history, ArrayList<Long> chord_history_t,
			ArrayList<Integer> tempo_history) {
		ClusterList tempos;
		Note curr, prev;
		double lookbehind;
		long diff, carry;
		int weight, window = 0, tempo;
		tempos = new ClusterList();
		/*
		 * Pseudo k-means (because unbounded performance in real-time code is garbage)
		 * compute based on time between notes as well as how long notes are held
		 */
		carry = 0;
		lookbehind = TEMPO_LOOKBEHIND;
		for (int i = chord_history_t.size()-2; i >= 0 && lookbehind > 0; i--){
			diff = chord_history_t.get(i+1) - chord_history_t.get(i) + carry;
			weight = TEMPO_CHORD_WEIGHT;
			if (diff >= TEMPO_MIN_NOTE_RES){
				tempo_pow2_insert(tempos, diff, weight, TEMPO_CLUSTER_RADIUS, false);
				carry = 0;
				window++;
			} else {
				carry = diff / 2;
			}
			lookbehind -= (double)diff / 1000000;
		}
		carry = 0;
		lookbehind = TEMPO_LOOKBEHIND;
		for (int i = note_history.size()-2; i >= 0 && lookbehind > 0; i--){
			curr = note_history.get(i+1); prev = note_history.get(i);
			diff = curr.get_start() - prev.get_start() + carry;
			weight = curr.vel() / ((curr.octave() < 4)? 1 : 2);
			if (diff >= TEMPO_MIN_NOTE_RES){
				tempo_pow2_insert(tempos, diff, weight, TEMPO_CLUSTER_RADIUS, true);
				carry = 0;
				window++;
			} else {
				carry = diff / 2;
			}
			lookbehind -= (double)diff / 1000000;
		}
		if (window < TEMPO_MIN_INTERVALS){
			tempo = -1;
		} else {
			tempo = (int)(tempos.ml_aggregate(0.0).center());
		}
		return update_tempo_history(tempo_history, tempo);
	}
	
	/**
	 * Inserts the clustered time interval's tempo, as well as any power-of-2 images
	 * (the weighting of these images decay, to reduce the chance of aliasing)
	 * @param clusters
	 * @param time_length
	 * @param weight
	 * @param tolerance
	 */
	private static void tempo_pow2_insert(ClusterList clusters, long time_length,
			int weight, int tolerance, boolean apply_decay){
		long tempo = 60000000 / time_length; /* usecs to bpm */
		long tmp, decay;
		for (tmp = tempo, decay = 1; tmp < TEMPO_MIN; tmp *= 2, decay *= 2) { }
		for (; tmp <= TEMPO_MAX; tmp *= 2, decay *= 2){
			if (apply_decay){
				clusters.insert(new Cluster(tmp, (int)(weight/decay)), tolerance);
			} else {
				clusters.insert(new Cluster(tmp, (int)(weight)), tolerance);
			}
		}
		for (tmp = tempo/2, decay = 2; tmp > TEMPO_MAX; tmp /= 2, decay *= 2) { }
		for (; tmp >= TEMPO_MIN; tmp /= 2, decay *= 2){
			if (apply_decay){
				clusters.insert(new Cluster(tmp, (int)(weight/decay)), tolerance);
			} else {
				clusters.insert(new Cluster(tmp, (int)(weight)), tolerance);
			}
		}
	}
	
	/**
	 * Updates the moving window of previous tempos
	 * @param tempo_history the array storing the moving window
	 * @param new_tempo the new tempo
	 * @param curr_tempo the current average of the window
	 * @return the updated average, with aliasing effects taken into account
	 */
	private static int update_tempo_history(ArrayList<Integer> tempo_history, int new_tempo){
		int curr_low, curr, curr_hi;
		/* discard history if music stops */
		if (new_tempo == -1){
			tempo_history.clear();
			return -1;
		}
		if (tempo_history.isEmpty()){
			tempo_history.add(new_tempo);
			return new_tempo;
		}
		/* ideally would use moving average formula instead, but for now this will have to do */
		curr = 0;
		for (Integer i:tempo_history){
			curr += i;
		}
		curr /= tempo_history.size();
		if (tempo_history.size() == TEMPO_CACHE_SIZE){
			tempo_history.remove(0).intValue();
		}
		curr_low = curr / 2; curr_hi = curr * 2;
		/* ignore any obvious aliasing */
		if (Math.abs(new_tempo-curr_low) <= curr_low/32
				|| Math.abs(new_tempo-curr_hi) <= curr_hi/32){
			new_tempo = curr;
		}
		tempo_history.add(new_tempo);
		/* ideally would use moving average formula instead, but for now this will have to do */
		curr = 0;
		for (Integer i:tempo_history){
			curr += i;
		}
		return curr / tempo_history.size();
	}
	
	public static String get_chord_context_free (ArrayList<Note> notes, int min) {
		ArrayList<Integer> unique = new ArrayList<Integer>();
		for (Note n : notes) if (!unique.contains(n.key())) unique.add(n.key());
		if (unique.size() > 4) return "unknown";
		if (unique.size() < min) return "unknown";
		Integer [] uniq = unique.toArray(new Integer [unique.size()]);
		int triad = get_triad(uniq); String ret = "";
		if (triad < 0) {
			int seventh = get_seventh(uniq);
			if (seventh < 0) {
				int third = get_third(uniq);
				if (third < 0) return "unknown";
				ret += (third < 2 ? Music.getRoot((int)uniq[0]) : Music.getRoot((int)uniq[1]));
				return ret + (third%2 == 0 ? "maj" : "min");
			}
			switch (seventh) {
				case 0: case 1: case 2: case 3: case 100: case 101: case 102: case 103:
					ret += Music.getRoot((int)uniq[0]); break;
				case 4: case 5: case 6: case 7: case 104: case 105: case 106: case 107:
					ret += Music.getRoot((int)uniq[1]); break;
				case 8: case 9: case 10: case 11: case 108: case 109: case 110: case 111:
					ret += Music.getRoot((int)uniq[2]); break;
				case 112: case 113: case 114: case 115:
					ret += Music.getRoot((int)uniq[3]); break;
			}
			if (seventh%4 == 0) return ret + "dom7";
			if (seventh%4 == 1) return ret + "maj7";
			if (seventh%4 == 2) return ret + "min7";
			return ret + "dim7";
		} else {
			switch (triad) {
				case 0: case 1: case 2: ret += Music.getRoot((int)uniq[0]); break;
				case 3: case 4: case 5: ret += Music.getRoot((int)uniq[1]); break;
				case 6: case 7: case 8: ret += Music.getRoot((int)uniq[2]); break;
			}
			if (triad%3 == 0) return ret + "maj";
			if (triad%3 == 1) return ret + "min";
			return ret + "dim";
		}
	}
	
	private static int get_triad(Integer[] nk) {
		if (nk.length != 3) return -1;
		for (int i = 0; i < 3; i++) {
			if (has(nk, (nk[0]+4)%12) && has(nk, (nk[0]+7)%12)) return i*3 + 0;
			if (has(nk, (nk[0]+3)%12) && has(nk, (nk[0]+7)%12)) return i*3 + 1;
			if (has(nk, (nk[0]+3)%12) && has(nk, (nk[0]+6)%12)) return i*3 + 2;
			nk = circ_shift(nk);
		}
		return -1;
	}
	
	private static int get_third(Integer[] nk) {
		if (nk.length != 2) return -1;
		if ((nk[0]+4)%12 == nk[1]) return 0;
		if ((nk[0]+3)%12 == nk[1]) return 1;
		if ((nk[1]+4)%12 == nk[0]) return 2;
		if ((nk[1]+3)%12 == nk[0]) return 3;
		return -1;
	}
	
	private static int get_seventh(Integer[] nk) {
		int type = -20; //0-dom, 1-maj, 2-min, 3-dim 
		int inv = 0;
		if (nk.length != 3 && nk.length != 4) return -1;
		if (nk.length == 3) {
			for (int i = 0; i < 3; i++) {
				if (has(nk, (nk[0]+4)%12) && has(nk, (nk[0]+10)%12)) { inv = i*4; type = 0; }
				if (has(nk, (nk[0]+4)%12) && has(nk, (nk[0]+11)%12)) { inv = i*4; type = 1; }
				if (has(nk, (nk[0]+3)%12) && has(nk, (nk[0]+10)%12)) { inv = i*4; type = 2; }
				if (has(nk, (nk[0]+3)%12) && has(nk, (nk[0]+9 )%12)) { inv = i*4; type = 3; }
				nk = circ_shift(nk);
			}
			if  (type > -1) return type+inv;
			return -1;
		} else {
			for (int i = 0; i < 4; i++) {
				if (has(nk, (nk[0]+4)%12) && has(nk, (nk[0]+10)%12) && has(nk, (nk[0]+7)%12)) { inv = i*4; type = 0; }
				if (has(nk, (nk[0]+4)%12) && has(nk, (nk[0]+11)%12) && has(nk, (nk[0]+7)%12)) { inv = i*4; type = 1; }
				if (has(nk, (nk[0]+3)%12) && has(nk, (nk[0]+10)%12) && has(nk, (nk[0]+7)%12)) { inv = i*4; type = 2; }
				if (has(nk, (nk[0]+3)%12) && has(nk, (nk[0]+ 9)%12) && has(nk, (nk[0]+6)%12)) { inv = i*4; type = 3; }
				nk = circ_shift(nk);
			}
			if (type > -1) return type+inv+100;
			return -1;
		}
	}
		
	private static boolean has(Integer[]in, int k) { return Arrays.asList(in).contains(k); }
	private static boolean has(int[] in, int k) { boolean f = false; for (int i : in) if (i == k) f = true; return f; }
	private static int last_index_of(int[] in, int k) {
		int ind = -1;
		for (int i = in.length-1; i >= 0; i--) if (in[i] == k) { ind = i; break; }
		return ind;
	}
	
	private static Integer[] circ_shift(Integer[] in) {
		Integer[] out = new Integer[in.length];
		for (int i = 1; i < in.length; i++) out[i-1] = in[i];
		out[in.length-1] = in[0]; return out;
	}
	
	private static int[] circ_shift(int[] in) {
		int[] out = new int[in.length];
		for (int i = 1; i < in.length; i++) out[i-1] = in[i];
		out[in.length-1] = in[0]; return out;
	}
	
	public static int get_tempo(ArrayList<Long> chord_history_t, int num_beats, int curr_tempo) {
		if (chord_history_t.size() < 3) return -1;
		int avg_of = 3;
		ArrayList<Long> rel_t = new ArrayList<Long>();
		if (chord_history_t.size() > avg_of)
			for (int i = chord_history_t.size() - avg_of; i < chord_history_t.size(); i++)
				rel_t.add(chord_history_t.get(i));
		else for (int i = 0; i < chord_history_t.size(); i++)
			rel_t.add(chord_history_t.get(i));
		
		ArrayList<Long> times = new ArrayList<Long>();
		for (int i = 1; i < rel_t.size() - 1; i++)
			times.add(rel_t.get(i) - rel_t.get(i-1));
		Collections.sort(times);
		return 140;
	}
	
	public static Chord get_chord(ArrayList<Note> rel, ArrayList<Note> all, byte dom, KeySignature key, Chord curr) {
		if (rel.size() == 0) return null;
		if (key == null) return null;
		if (key.major) return get_major_chord(rel, all, dom, key, curr);
		else return get_minor_chord(rel, all, dom, key, curr);
	}
	
	private static Chord get_major_chord(ArrayList<Note> rel, ArrayList<Note> all, byte dom, KeySignature k, Chord curr) {
		int kkey = Music.getKey(k.key + "" + k.type);
		int d = (Music.keyOf(dom) - kkey + 12)%12;
		ArrayList<Integer> keys = new ArrayList<Integer>();
		ArrayList<Integer> alls = new ArrayList<Integer>();
		for (Note n : all) alls.add((n.key() - kkey + 12)%12);
		for (Note n : rel) keys.add((n.key() - kkey + 12)%12);
		String c = "";
		switch (d) {
			case 0: // C
				if (alls.contains(8)) c = "4-120m";
				else if (keys.contains(2) && (keys.contains(4) || keys.contains(7)) && super_relevant(rel, 2, kkey)) c = "S1-20M";
				else if (keys.contains(5) && super_relevant(rel, 5, kkey)) c = "S1-40M";
				else if (keys.contains(4) && alls.contains(10)) c = "F4-07M";
				else if (alls.contains(6)) c = "F5-37M";
				else if (keys.contains(2) && keys.contains(5)) c = "2-137m";
				else if (keys.contains(5) && keys.contains(9)) if (keys.contains(4)) c = "4-127M"; else c = "4-120M";
				else if (keys.contains(9) && keys.contains(7)) c = "6-117m";
				else if (keys.contains(9) && keys.contains(4)) c = "6-110m";
				else c = "1-100M";
				break;
			case 1: // C#
				if (keys.contains(7)) c = "F2-17m";	else c = "F2-10m"; break;
			case 2: // D
				if (alls.contains(6)) { if (keys.contains(0)) c = "F5-07M"; else c = "F5-00M"; }
				else if (keys.contains(7) && keys.contains(11) && super_relevant(rel, 7, kkey)) c = "CAD64-5M";
				else if (keys.contains(7) && keys.contains(11) && curr.base == 6) c = "5-120M";
				else if (keys.contains(11) && keys.contains(5)) { if (alls.contains(8)) c = "7-117d"; else c = "7-110d"; }
				else if (alls.contains(8) && keys.contains(5)) c = "2-100d";
				else if (alls.contains(8) && keys.contains(4)) c = "F6-37m";
				else if (keys.contains(11) && keys.contains(5) && keys.contains(7)) c = "5-127M";
				else if (keys.contains(0)) c = "2-107m"; else c = "2-100m";
				break;
			case 3: // Eb / D#
				if (keys.contains(11) || alls.contains(6)) {
					if (keys.contains(9)) c = "F3-17m"; else c = "F3-10m";
				} else c = "3-000M";
				break;
			case 4: // E
				if (keys.contains(9) && keys.contains(0) && super_relevant(rel, 9, kkey)) c = "CAD64-6m";
				else if (keys.contains(9) && keys.contains(0) && curr.base == 2) c = "6-120m";
				else if (keys.contains(9) && keys.contains(7)) c = "6-127m";
				else if (keys.contains(0)) c = "1-110M";
				else if (alls.contains(8)) { if (keys.contains(2)) c = "F6-07m"; else c = "F6-00m"; }
				else if (alls.contains(1)) { if (keys.contains(7)) c = "F2-27m"; else c = "F2-20m"; }
				else if (alls.contains(10)) c = "F4-17M";
				else if (keys.contains(5)) c = "4-137M";
				else if (keys.contains(7) && keys.contains(2)) c = "3-107m";
				else c = "3-100m";
				break;
			case 5: // F
				if (keys.contains(7) && keys.contains(11)) c = "5-137M";
				else if (keys.contains(11) && !keys.contains(0)) { if (alls.contains(8)) c = "7-127d"; else c = "7-120d"; }
				else if (keys.contains(8) && keys.contains(2) && !keys.contains(0)) c = "2-107d";
				else if (alls.contains(8)) { if (keys.contains(4)) c = "4-107m"; else c = "4-100m"; }
				else if (keys.contains(11) && super_relevant(rel, 11, kkey)) { if (keys.contains(4)) c = "S4-47M"; else c = "S4-40M"; }
				else if (keys.contains(7) && super_relevant(rel, 7, kkey)) { if (keys.contains(4)) c = "S4-27M"; else c = "S4-20M"; }
				else if (keys.contains(2) && super_relevant(rel, 2, kkey)) { if (keys.contains(0)) c = "2-117m"; else c = "2-110m"; }
				else if (keys.contains(4)) c = "4-107M";
				else c = "4-100M";
				break;
			case 6: // F#
				if ((keys.contains(11) || alls.contains(3)) && alls.contains(9)) c = "F3-27m";
				else if (keys.contains(0)) c = "F5-17M"; else c = "F5-10M";
				break;
			case 7: // G
				if (keys.contains(0) && keys.contains(4) && (curr.base == 6 || curr.code.equals("1-120m"))) c = "1-120M";
				else if (keys.contains(0) && (keys.contains(4) || keys.contains(9)) && (curr.base == 6 || curr.code.equals("6-137m"))) c = "6-137m";
				else if (keys.contains(0) && keys.contains(4) && super_relevant(rel, 0, kkey) && super_relevant(rel, 4, kkey) && !keys.contains(11)) c = "CAD64-1M";
				else if (keys.contains(0) && super_relevant(rel, 0, kkey)) { if (keys.contains(5)) c = "S5-47M"; else c = "S5-40M"; }
				else if (keys.contains(9) && super_relevant(rel, 9, kkey)) { if (keys.contains(5)) c = "S5-27M"; else c = "S5-20M"; }
				else if (alls.contains(10) && keys.contains(0)) c = "F4-27M";
				else if (alls.contains(10)) c = "5-100m";
				else if (alls.contains(1)) c = "F2-37m";
				else if (keys.contains(5)) c = "5-107M"; 
				else if (keys.contains(4) && super_relevant(rel, 4, kkey)) { if (keys.contains(2)) c = "3-117m"; else c = "3-110m"; }
				else if (keys.contains(9) && keys.contains(0) && super_relevant(rel, 9, kkey)) c = "6-137m";
				else c = "5-100M";
				break;
			case 8: // G# / Ab
				if (keys.contains(5) && keys.contains(0)) { if (keys.contains(4)) c = "4-117m"; else c = "4-110m"; }
				else if (alls.contains(3)) { if (keys.contains(7)) c = "6-007M"; else c = "6-000M"; }
				else if (keys.contains(11) && keys.contains(5)) c = "7-137d";
				else if (keys.contains(4) && keys.contains(2)) c = "F6-17m"; 
				else if (keys.contains(0)) { if (keys.contains(4)) c = "4-117m"; else c = "4-110m"; }
				else c = "F6-10m";
				break;
			case 9: // A
				if (keys.contains(2) && keys.contains(5) && (curr.is_chord_tone(10) || curr.code().equals("CAD64-2m"))) c = "CAD64-2m";
				else if (keys.contains(2) && keys.contains(0) && keys.contains(5) && super_relevant(rel, 2, kkey)) c = "2-127m";
				else if (keys.contains(2) && keys.contains(5) && super_relevant(rel, 2, kkey)) c = "2-120m";
				else if (keys.contains(11) && super_relevant(rel, 11, kkey) && !keys.contains(5)) { if (keys.contains(7)) c = "S6-27m"; else c = "S6-20m"; }
				else if (keys.contains(2) && super_relevant(rel, 2, kkey) && !keys.contains(5)) { if (keys.contains(7)) c = "S6-47m"; else c = "S6-40m"; }
				else if (alls.contains(1)) { if (keys.contains(7)) c = "F2-07m"; else c = "F2-00m"; }
				else if (alls.contains(6)) { if (keys.contains(0)) c = "F5-27M"; else c = "F5-20M"; }
				else if (keys.contains(5)) { if (keys.contains(4)) c = "4-117M"; else c = "4-110M"; }
				else if (keys.contains(7)) c = "6-107m"; else c = "6-100m";
				break;
			case 10: // Bb
				if (alls.contains(0) && keys.contains(4)) c = "F4-37M"; else c = "7-000M"; break;
			case 11: // B
				if (keys.contains(7) && keys.contains(5)) { c = "5-117M"; break; }
				if (keys.contains(4) && keys.contains(7)) { 
					if (keys.contains(2)) { c = "3-127m"; break; }
					else {
						Note tmp = null, bass = rel.get(0); 
						for (Note n : rel) { if (n.key() == (kkey+4)%12) tmp = n; }
						if (tmp != null && Math.abs(tmp.get_start() - bass.get_start()) < NoteBuffer.same_thresh) { c = "3-120m"; break; }
					}
				}
				if (keys.contains(2) && keys.contains(5)) { if (keys.contains(8)) c = "7-107d"; else c = "7-100d"; }
				else if (alls.contains(8) && keys.contains(4)) { if (keys.contains(2)) c = "F6-27m"; else c = "F6-20m"; }
				else if (keys.contains(8)) c = "7-107d";
				else if (alls.contains(3)) { if (keys.contains(9)) c = "F3-07m"; else c = "F3-00m"; }
				else c = "5-110M";
				break;
		}
		return new Chord(c);
	}
	

	
	private static Chord get_minor_chord(ArrayList<Note> rel, ArrayList<Note> all, byte dom, KeySignature k, Chord curr) {
		int d = Music.keyOf(dom);
		ArrayList<Integer> keys = new ArrayList<Integer>();
		ArrayList<Integer> alls = new ArrayList<Integer>();
		for (Note n : all) alls.add(n.key());
		for (Note n : rel) keys.add(n.key());
		String c = "";
		switch (d) {
			case 0: 
				if (keys.contains(4)) c = "F2-0" + (keys.contains(10)? "7" : "0") + "m";
				else c = "6-10" + (keys.contains(10)? "7" : "0") + "m";
				break;
			case 1:
				c = "7-000M";
				break;
			case 2:
				if (keys.contains(10)) c = "5-110M";
				else c = "7-100d";
				break;
			case 3:
				if (keys.contains(7) && keys.contains(8)) c = "S1-40M";
				else if (keys.contains(0)) c = "6-110m";
				else c = "1-100M";
				break;
			case 4:
				c = "F2-10m";
				break;
			case 5:
				c = "2-100m";
				break;
			case 6:
				c = "4-137M";
				break;
			case 7:
				if (keys.contains(0)) c = "CAD64-6";
				else if (keys.contains(11)) c = "F6-00m";
				else c = "3-100m";
				break;
			case 8:
				if (keys.contains(5)) c = "2-110m";
				else c = "4-100M";
				break;
			case 9:
				c = "F5-10M";
				break;
			case 10:
				if (keys.contains(3) && keys.contains(5)) c = "S5-40M";
				else if (keys.contains(3)) c = "CAD64-1";
				else if (keys.contains(8)) c = "5-107M";
				else c = "5-100M";
				break;
			case 11:
				if (keys.contains(6)) c = "6-000M";
				else if (keys.contains(8)) c = "4-100m";
				else c = "F6-10m";
				break;
		}
		return new Chord(c);
	}
	
	private static boolean super_relevant(ArrayList<Note> rels, int ind, int kkey) {
		Note bass = rels.get(0);
		ArrayList<Note> tmp = new ArrayList<Note>();
		for (Note n : rels) { if (n.key() == (kkey + ind)%12) tmp.add(n); }
		boolean flag = false;
		for (Note n : tmp) if (Math.abs(n.get_start() - bass.get_start()) < NoteBuffer.same_thresh) flag = true;
		return flag;
	}
	
	
	public static void main(String [] args) {
		NoteBuffer nb = new NoteBuffer(null);
		MidiHandler mh = new MidiHandler(nb);
		mh.imNotUseless();
	}
}