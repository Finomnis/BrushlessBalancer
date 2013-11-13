package org.finomnis.brushlessbalancer.filter;

import org.finomnis.brushlessbalancer.arduinointerface.Measurement;

public class AlphaLowPass extends Filter {

	private final float alpha;

	private Measurement previous = new Measurement();
	private boolean initialized = false;
	
	public AlphaLowPass(float alpha){
		this.alpha = alpha;
	}
	
	public void receiveData(Measurement[] measurements) {
		
		Measurement[] output = new Measurement[measurements.length];
		
		if(!initialized)
		{
			initialized = true;
			previous.x = measurements[0].x;
			previous.y = measurements[0].y;
			previous.z = measurements[0].z;
		}
		
		for(int i = 0; i < measurements.length; i++)
		{
			previous.x = measurements[i].x * alpha + previous.x * (1 - alpha);
			previous.y = measurements[i].y * alpha + previous.y * (1 - alpha);
			previous.z = measurements[i].z * alpha + previous.z * (1 - alpha);
			output[i] = previous.clone();
		}
		
		sendData(output);
		
	}

}
