package org.finomnis.brushlessbalancer;

import javax.swing.JOptionPane;

import org.finomnis.brushlessbalancer.arduinointerface.ArduinoInterface;
//import org.finomnis.brushlessbalancer.filter.AlphaLowPass;
//import org.finomnis.brushlessbalancer.filter.AveragingLowPass;
import org.finomnis.brushlessbalancer.filter.Filter;
import org.finomnis.brushlessbalancer.filter.OffsetFilter;
import org.finomnis.brushlessbalancer.visualization.FourierWindow;
//import org.finomnis.brushlessbalancer.visualization.WaveWindow;


import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

public class Main {

	/**
	 * @param args
	 * @throws PortInUseException 
	 * @throws NoSuchPortException 
	 * @throws UnsupportedCommOperationException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, InterruptedException {
		String[] ports = ArduinoInterface.getPorts();
		if(ports.length < 1){
			JOptionPane.showMessageDialog(null, "Unable to connect to MotorBalancer!", "ERROR", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		//WaveWindow graphWindow = new WaveWindow(500, 500, 400);
		FourierWindow fftWindow = new FourierWindow(500, 500, 512, 256, 1/1600.0f, 10.0f, false);
		
		
		//Filter lowPass = new AveragingLowPass(10);
		Filter offset = new OffsetFilter(0.004f);
		
				
		ArduinoInterface arduino = new ArduinoInterface(ports[0]);
		arduino.addReciever(offset);
		//arduino.addReciever(graphWindow);
		offset.addReciever(fftWindow);
		//offset.addReciever(lowPass);
		//lowPass.addReciever(graphWindow);
		arduino.start();
		while(true)
		{
			long time = System.nanoTime();
			arduino.receive();
			while(System.nanoTime() - time < 15000000)
				Thread.sleep(1);
		}
		
	}

}
