import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

public class MidiHandler {
	private MidiDevice device;
	private MidiInputReceiver mir;
	private NoteBuffer buffer;
	private boolean damped;
	private boolean debug = false;

	public MidiHandler(NoteBuffer buffer) {
		this.buffer = buffer;
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		for (int i = 0; i < infos.length; i++) {
			try {
				device = MidiSystem.getMidiDevice(infos[i]);
				//add all transmitters to the device list
				System.err.println("Attempting to open " + infos[i]);

				//get all transmitters
				List<Transmitter> transmitters = device.getTransmitters();

				for(int j = 0; j<transmitters.size();j++) {
					//create a new receiver for each transmitter
					mir = new MidiInputReceiver(device.getDeviceInfo().toString());
					transmitters.get(j).setReceiver(mir);
				}

				Transmitter trans = device.getTransmitter();
				mir = new MidiInputReceiver(device.getDeviceInfo().toString());
				trans.setReceiver(mir);

				//open each device
				device.open();
				
				//if code gets this far without throwing an exception, print a success message
				System.err.println("Successfully opened " + device.getDeviceInfo() + "\n");
			} catch (MidiUnavailableException e) {
				System.err.println("Not the correct device.\n");
			}
		}
	}
	
	public void imNotUseless(){}
	
	public class MidiInputReceiver implements Receiver {
		public String name;
		public MidiInputReceiver(String name) {
			this.name = name;
		}
		
		/* 
		 * Useful reference codes:
		 * The MIDI message packet consists of three parts, which I have called STATUS, DATA1, and DATA2
		 * Relevant meanings for STATUS, when cast as a signed integer:
		 *  * -80: 	pedal up / down (DATA2 is 127 when down, 0 when up)
		 *  * -112:	note down
		 *  * -128: note up
		 * Relevant meanings for DATA1, when cast as a signed integer:
		 *  * usually refers to the value of the note, where A4 has value 49
		 *  * when pedal is down, this value is usually 64
		 *  * E1 has a value of 28, and A0 has a value of 21
		 * Relevant meanings for DATA2, when cast as a signed integer
		 *  * usually refers to the velocity of the note, when pressed
		 *  * when STATUS refers to the pedal, then 127 indicates the pedal is pressed
		 *  * when STATUS refers to the pedal, then 0 indicates the pedal is released
		 *  
		 * @see javax.sound.midi.Receiver#send(javax.sound.midi.MidiMessage, long)
		 */
		
		public void send(MidiMessage msg, long timeStamp) {
			byte message[] = new byte[3];
			message = msg.getMessage();
			
			// break down the raw byte data into the three components
			byte data1 = message[1], data2 = message[2];
			if (debug) System.out.println("STAT: " + message[0] + "\tDATA1: " + data1 + "\tDATA2: " + data2 + "\tTIME:" + timeStamp);
			
			boolean print = false;
			// contact note buffer with instructions based on what data is received
			switch (message[0]) {
			
				// damp or undamp pedal when pedal status is changed
				case -80: 
					damped = ((int)data2 == 127);
					if (!damped) buffer.undamp(timeStamp);
					else buffer.damp(timeStamp);
					print = false; break;
				// add note to buffer when new note is input
				case -112:
					int tempo = Analyzer.get_tempo(Analyzer.get_time_signature(buffer.history), buffer.tempo_buffer, 4.5);
					if (message[2] > 0) {
						System.out.println("Tempo: " + tempo + " BPM");
						buffer.add_note(data1, true, data2, timeStamp);
						print = true; break;
					} else {
						buffer.release_note(data1, damped, timeStamp);
						print = true; break;
					}
				// release note from buffer when note is released
				case -128:
					buffer.release_note(data1, damped, timeStamp);
					print = true; break;
				default:
			}
			
			// print debug information if necessary
			if (print) {
//				buffer.print_buffer();
//				buffer.print_holds();
//				buffer.print_history();
//				System.out.println("Current overtone: " + Music.getNoteName(buffer.dom()));
//				System.out.println();
//				String s= Analyzer.get_chord_context_free(buffer.hold_buffer, 0);
//				if (!s.equals("unknown")) System.out.println(s);
//				Analyzer.get_key_signature(buffer.key_analysis, buffer.curr_key);

			}
		}
		
		public void close() {}

	}
	
	/*
	 * Close the MidiHandler object
	 */
	public void close() {
		device.close();
		mir.close();
	}
}