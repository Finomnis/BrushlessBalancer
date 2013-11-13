package org.finomnis.brushlessbalancer.filter;

import org.finomnis.brushlessbalancer.arduinointerface.Measurement;

public class OffsetFilter extends Filter {

	private final float alpha;

	private Measurement offset = new Measurement(0.0f, 0.0f, 0.0f);
	
	public OffsetFilter(float alpha){
		this.alpha = alpha;
	}
	
	public void receiveData(Measurement[] measurements) {
		
		Measurement[] output = new Measurement[measurements.length];
		
		for(int i = 0; i < measurements.length; i++)
		{
			offset.x = measurements[i].x * alpha + offset.x * (1 - alpha);
			offset.y = measurements[i].y * alpha + offset.y * (1 - alpha);
			offset.z = measurements[i].z * alpha + offset.z * (1 - alpha);
			output[i] = measurements[i].clone();
			output[i].x -= offset.x;
			output[i].y -= offset.y;
			output[i].z -= offset.z;
		}
		
		sendData(output);
		
	}
	

}
