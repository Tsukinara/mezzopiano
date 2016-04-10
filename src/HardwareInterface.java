import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;

public class HardwareInterface implements SerialPortEventListener {
	SerialPort serialPort;
	/** The port we're normally going to use. */
	private static final String PORT_NAMES[] = { 
		"/dev/tty.usbserial-A9007UX1", // Mac OS X
		"/dev/ttyACM0", // Raspberry Pi
		"/dev/ttyUSB0", // Linux
		"COM6", // Windows
	};
	/**
	 * A BufferedReader which will be fed by a InputStreamReader 
	 * converting the bytes into characters 
	 * making the displayed results codepage independent
	 */
	private BufferedReader input;
	/** The output stream to the port */
	private OutputStream output;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;

	public void initialize() {
		// the next line is for Raspberry Pi and 
		// gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
		//System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");

		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		//First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			for (String portName : PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}
		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				String inputLine=input.readLine();
				System.out.println(inputLine);
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}
	
	public void sendColor(Color c) {
		byte r = (byte)(c.getRed()/2);
		byte g = (byte)(c.getGreen()/2);
		byte b = (byte)(c.getBlue()/2);
		try { output.write(new byte[]{'B', r, g, b}); }
		catch (IOException e) { e.printStackTrace(); }
	}
	
	public void sendFan(int percent) {
		if (percent > 100) percent = 100;
		if (percent < 0) percent = 0;
		byte s = (byte)(percent * (127.0 / 100.0));
		try { output.write(new byte[]{'C', s}); }
		catch (IOException e) { e.printStackTrace(); }	
	}
	
	public void sendTemp(int temp) {
		if (temp > 74) temp = 74;
		if (temp < 64) temp = 64;
		byte t = (byte)((temp - 64)*9);
		try { output.write(new byte[]{'A', t}); }
		catch (IOException e) { e.printStackTrace(); }	
	}
	
//	public static void main(String [] args) {
//		HardwareInterface h = new HardwareInterface();
//		h.initialize();
//		while (true) {
//			h.sendColor(Color.CYAN);
//			h.sendFan(100);
//			h.sendTemp(74);
//			try { Thread.sleep(200); } catch (Exception e) {}
//			h.sendColor(Color.WHITE);
//			h.sendFan(0);
//			h.sendTemp(64);
//			try { Thread.sleep(200); } catch (Exception e) {}
//		}
//	}
}