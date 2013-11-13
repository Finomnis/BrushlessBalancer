package org.finomnis.brushlessbalancer.visualization;

import java.awt.Color;
import java.awt.Graphics2D;
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
	
	private long lastTime = System.currentTimeMillis();
	private float data_per_second = 0.0f;
	private long data_measured = 0;
	
	public WaveWindow(int size_x, int size_y,
			int num_data) {
		super(size_x, size_y, -512, 512, 3, num_data, "Oscilloscope");

		this.num_data = num_data;
		buffer = new ArrayList<Vector<Float>>(3);
		for(int i = 0; i < 3; i++)
			buffer.add(new Vector<Float>(num_data * 2));
	}

	@Override
	protected String getName(int graphId){
		switch(graphId)
		{
		case 0: return "X-Axis";
		case 1: return "Y-Axis";
		case 2: return "Z-Axis";
		default: return "Invalid";
		}
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
		
		long timeDelta = System.currentTimeMillis() - lastTime;
		data_measured += measurements.length;
		if(timeDelta > 500)
		{
			data_per_second = data_measured * 1000.0f / (float) timeDelta;
			lastTime += timeDelta;
			data_measured = 0;
		}
		
		this.repaint();
	}
	
	@Override
	protected void paintBuffer(Graphics2D g){
		
		g.setColor(Color.DARK_GRAY);
		
		String label = "Data Rate: " + (int)data_per_second + " Hz"; 
		
		g.drawChars(label.toCharArray(), 0, label.length(), 5, 60);
		
		super.paintBuffer(g);
	}

}
