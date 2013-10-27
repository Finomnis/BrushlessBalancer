package org.finomnis.brushlessbalancer.visualization;

import java.util.ArrayList;
import java.util.Vector;

import org.finomnis.brushlessbalancer.arduinointerface.DataReceiver;
import org.finomnis.brushlessbalancer.arduinointerface.Measurement;

public class WaveWindow extends GraphWindow implements DataReceiver {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ArrayList<Vector<Float>> buffer;
	
	private final int num_data;
	
	
	public WaveWindow(int size_x, int size_y,
			int num_data) {
		super(size_x, size_y, -512, 512, 3, num_data, "Oscilloscope");

		this.num_data = num_data;
		buffer = new ArrayList<Vector<Float>>(3);
		for(int i = 0; i < 3; i++)
			buffer.add(new Vector<Float>(num_data * 2));
	}

	@Override
	public void receiveData(Measurement[] measurements) {
		//System.out.println("Data recieved");
		for(int i = 0; i < measurements.length; i++)
		{
			buffer.get(0).add((Float)(float)measurements[i].x);
			buffer.get(1).add((Float)(float)measurements[i].y);
			buffer.get(2).add((Float)(float)measurements[i].z);
			
		}
		
		for(int i = 0; i < 3; i++)
		{
			Vector<Float> buf = buffer.get(i);
			if(buf.size() > num_data){
				setData(i, buf.subList(buf.size() - num_data, buf.size()));
				buf.subList(0, num_data).clear();
			}
		}
		
		this.repaint();
	}

}
