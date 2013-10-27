package org.finomnis.brushlessbalancer;

import org.finomnis.brushlessbalancer.arduinointerface.ArduinoInterface;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

public class Main {

	/**
	 * @param args
	 * @throws PortInUseException 
	 * @throws NoSuchPortException 
	 * @throws UnsupportedCommOperationException 
	 */
	public static void main(String[] args) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException {
		String[] ports = ArduinoInterface.getPorts();
		if(ports.length < 1) throw new RuntimeException("No port available!");
		
		ArduinoInterface arduino = new ArduinoInterface(ports[0]);
		arduino.start();
		
		
	}

}
